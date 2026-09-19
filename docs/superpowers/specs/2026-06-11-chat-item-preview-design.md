# Aperçu d'item dans le chat (`[i]` / `[item]`)

**Date :** 2026-06-11
**Statut :** Design validé

## Objectif

Permettre aux joueurs de taper `[i]` ou `[item]` dans le chat global. Le token est
remplacé par le nom de l'item tenu en **main principale**, affiché entre crochets et
coloré, que les autres joueurs peuvent survoler (hover) pour voir le tooltip natif
(nom, lore, enchantements, durabilité, attributs) — exactement comme le rendu vanilla
obtenu en shift-cliquant un item dans le chat.

La difficulté est le transfert : l'item se trouve sur le serveur backend (Paper), mais
le formatage et le broadcast du chat ont lieu sur le proxy (Velocity).

## Décisions de périmètre

| Décision | Choix |
|----------|-------|
| Source de l'item | Main principale uniquement |
| Type de hover | Tooltip natif vanilla (`showItem`) |
| Accès | Permission dédiée `servercore.chat.item` (codée en dur) |
| Config `config.yml` | **Aucune** (permission + constantes en dur) |

## Architecture existante (rappel)

Flux du chat global aujourd'hui :

1. **Paper** `AsyncChatListener` capture le message et le sérialise en string legacy.
2. **Paper** `PluginMessageHelper.sendGlobalMessage` envoie via le canal plugin message
   `proxy:chat` : `writeUTF("Global")`, uuid, message.
3. **Velocity** `PluginMessageListener.handleChatMessage` lit le paquet et appelle
   `ChatManager.sendGlobalMessage(uuid, message)`.
4. **Velocity** `ChatManager` applique le formatage (préfixe MiniMessage, placeholders
   PAPI, mentions/pings, options joueur, ignore) et broadcast à tous les joueurs en ligne.

Faisabilité : Paper 1.21.1 et Velocity 3.4.0 embarquent tous deux Adventure avec
`GsonComponentSerializer`. L'API Paper `ItemStack.displayName()` retourne déjà un
Component `[Nom]` coloré selon la rareté, **avec un hover `showItem` natif attaché**.

## Approche retenue : plugin message étendu + marqueur sentinelle

Rejetées :
- **Message item séparé keyé par id** (`[item:<id>]`) : sujet aux races, inutile pour
  un seul item.
- **Tout pré-rendre côté Paper** : le formatage se fait sur Velocity à partir d'une
  string legacy ; injecter un Component riche au milieu obligerait à réécrire le pipeline.

### Pourquoi une sentinelle plutôt que le token littéral

Si l'expéditeur a `servercore.colors`, le message passe par MineDown sur Velocity
(`ChatManager:59-61`), dont la syntaxe d'événements utilise `[texte](...)`. Un `[i]`
littéral risquerait d'être mal interprété. En remplaçant le token par un caractère de
zone privée (`\uE000`) **côté Paper**, on traverse MineDown, les mentions et le legacy
sans collision, et on ne réinjecte l'item qu'à la toute fin (sur le Component final).

La constante sentinelle vit dans `Core-common` (module partagé par Paper et Velocity).

## Composants

### Core-common

- Nouvelle classe de constantes (ex. `fr.iban.common.chat.ChatItemConstants`) :
  - `ITEM_PLACEHOLDER = "\uE000"` (marqueur sentinelle, zone privée Unicode)
  - `PERMISSION = "servercore.chat.item"`
  - `MAX_ITEM_BYTES = 25000` (garde-fou : `writeUTF` plafonne à 65535 octets ; un
    shulker rempli ou un livre écrit peut exploser cette limite)

### Côté Paper

**`AsyncChatListener` + nouveau helper `ChatItemHelper`**

- Regex `\[(i|item)\]` sur le message.
  - Aucun match → flux `"Global"` actuel **inchangé**.
  - Match mais permission absente → token laissé littéral, flux `"Global"` normal.
  - Match + permission OK :
    - Capturer l'item en main principale.
    - Main vide → token laissé littéral, flux `"Global"` normal.
    - Item présent → `Component name = item.displayName()`, sérialisé via
      `GsonComponentSerializer.gson().serialize(name)`. Remplacer tous les tokens
      `[i]`/`[item]` par `ITEM_PLACEHOLDER`.
    - **Garde-fou taille** : si le JSON dépasse `MAX_ITEM_BYTES`, fallback sur le nom
      seul sans hover (dégradation gracieuse).

**`PluginMessageHelper.sendGlobalItemMessage(player, message, itemJson)`**

- Nouveau sous-canal `"GlobalItem"` sur `proxy:chat` :
  `writeUTF("GlobalItem")`, uuid, message (avec sentinelle), itemJson.

### Côté Velocity

**`PluginMessageListener.handleChatMessage`**

- Gérer le nouveau sous-type `"GlobalItem"` → lit uuid, message, itemJson →
  `ChatManager.sendGlobalMessage(uuid, message, itemJson)`.

**`ChatManager`**

- Surcharge `sendGlobalMessage(uuid, message, @Nullable String itemJson)`.
- Désérialise une fois le Component de l'item (`GsonComponentSerializer.gson().deserialize`).
- Tout le formatage existant reste identique (préfixe, mentions, ignore, options).
- Juste avant le `sendMessage` final, appliquer
  `replaceText(ITEM_PLACEHOLDER → itemComponent)` sur le Component du message.
- L'ancienne signature `sendGlobalMessage(uuid, message)` délègue avec `itemJson = null`.

## Cas limites

- Plusieurs `[i]` dans un message → tous pointent sur le même item (main principale),
  tous remplacés.
- `[item]` ne contient pas `[i]` comme sous-chaîne → pas de chevauchement de tokens.
- Item trop volumineux (> `MAX_ITEM_BYTES`) → nom coloré sans hover.
- Pas de permission / main vide → le token reste du texte normal.

## Testing

Le projet n'a aucune infrastructure de test (pas de `src/test`, pas de JUnit/MockBukkit).

- Ajouter un harness léger (JUnit5 + Adventure, **sans Bukkit**) pour la **logique pure** :
  - détection/substitution des tokens (string → string, côté Paper) ;
  - injection sentinelle → Component (côté Velocity) : vérifier que le hover `showItem`
    est présent dans le Component final et que la sentinelle a disparu.
- La capture `ItemStack.displayName()` dépend du runtime Bukkit → **vérification manuelle
  en jeu**.

## Risque résiduel à valider

Compatibilité du format JSON `showItem` entre l'Adventure de Paper 1.21.1 et celle de
Velocity 3.4.0 lors du round-trip (Paper sérialise → Velocity désérialise → re-sérialise
vers le client). Les deux sont récents et Velocity traite les data-components de façon
opaque. À confirmer en jeu.
