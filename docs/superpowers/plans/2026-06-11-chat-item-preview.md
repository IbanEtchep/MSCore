# Aperçu d'item dans le chat (`[i]` / `[item]`) — Plan d'implémentation

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Permettre à un joueur de taper `[i]`/`[item]` dans le chat global ; le token devient le nom coloré de l'item en main, survolable pour afficher le tooltip natif (showItem), le tout propagé du backend Paper vers le proxy Velocity.

**Architecture:** Paper détecte le token, sérialise `ItemStack.displayName()` (hover natif inclus) en JSON, remplace le token par une sentinelle Unicode `\uE000` et envoie message + JSON via le sous-canal `"GlobalItem"` du canal `proxy:chat`. Velocity reconstruit le message comme aujourd'hui, désérialise l'item, puis remplace la sentinelle par le Component de l'item via `Component.replaceText`. Logique pure partagée et testée dans Core-common ; gating par la permission `servercore.chat.item`.

**Tech Stack:** Java 21 (toolchain Gradle), Adventure 4.17 (api + gson serializer, fournis par les plateformes), Paper API 1.21.1, Velocity API 3.4.0, JUnit 5.

**Référence spec :** `docs/superpowers/specs/2026-06-11-chat-item-preview-design.md`

---

## Convention importante : la sentinelle

Dans tout le code Java ci-dessous, le marqueur sentinelle s'écrit `"\uE000"` (échappement
Unicode Java, caractère de zone privée U+E000). **À recopier littéralement tel quel** — le
compilateur le transforme en le bon caractère. Ne jamais le remplacer par une chaîne vide.

---

## Structure des fichiers

**Core-common** (`fr.iban.common.chat`, logique pure partagée + testée)
- Create `Core-common/src/main/java/fr/iban/common/chat/ChatItemConstants.java` — constantes (sentinelle, permission, taille max).
- Create `Core-common/src/main/java/fr/iban/common/chat/ChatItemTokens.java` — détection regex + substitution sentinelle (pur, sans Adventure).
- Create `Core-common/src/main/java/fr/iban/common/chat/ChatItemRenderer.java` — injection sentinelle → Component (Adventure api).
- Create `Core-common/src/test/java/fr/iban/common/chat/ChatItemTokensTest.java`
- Create `Core-common/src/test/java/fr/iban/common/chat/ChatItemRendererTest.java`
- Modify `Core-common/build.gradle.kts` — deps de test (JUnit5, adventure-api, adventure gson).

**Core-paper** (capture Bukkit, dépendant du runtime → vérif manuelle)
- Create `Core-paper/src/main/java/fr/iban/bukkitcore/utils/ChatItemHelper.java` — capture main principale + sérialisation + garde-fou taille.
- Modify `Core-paper/src/main/java/fr/iban/bukkitcore/utils/PluginMessageHelper.java` — nouveau `sendGlobalItemMessage`.
- Modify `Core-paper/src/main/java/fr/iban/bukkitcore/listeners/AsyncChatListener.java` — routage token → GlobalItem.

**Core-velocity** (câblage proxy → compile-check + vérif manuelle)
- Modify `Core-velocity/src/main/java/fr/iban/velocitycore/listener/PluginMessageListener.java` — gérer le sous-type `"GlobalItem"`.
- Modify `Core-velocity/src/main/java/fr/iban/velocitycore/manager/ChatManager.java` — surcharge `sendGlobalMessage(uuid, message, itemJson)` + injection.

---

## Task 1: Core-common — constantes + détection/substitution des tokens (TDD)

**Files:**
- Modify: `Core-common/build.gradle.kts`
- Create: `Core-common/src/main/java/fr/iban/common/chat/ChatItemConstants.java`
- Create: `Core-common/src/main/java/fr/iban/common/chat/ChatItemTokens.java`
- Test: `Core-common/src/test/java/fr/iban/common/chat/ChatItemTokensTest.java`

- [ ] **Step 1: Câbler JUnit5 dans Core-common**

Remplacer le contenu de `Core-common/build.gradle.kts` par :

```kotlin
/**
 * CoreCommon
 */
plugins {
    id("io.github.goooler.shadow")
}

dependencies {
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("redis.clients:jedis:5.1.3")
    compileOnly("com.google.code.gson:gson:2.10")
    implementation("org.jdbi:jdbi3-core:3.49.6")
    compileOnly("net.kyori:adventure-api:4.17.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.17.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
```

- [ ] **Step 2: Créer la classe de constantes**

Créer `Core-common/src/main/java/fr/iban/common/chat/ChatItemConstants.java` :

```java
package fr.iban.common.chat;

public final class ChatItemConstants {

    /** Marqueur sentinelle (zone privée Unicode U+E000) inséré par le backend à la place
     *  du token [i]/[item], puis remplacé par le Component de l'item sur le proxy. */
    public static final String ITEM_PLACEHOLDER = "\uE000";

    /** Permission requise pour partager l'item tenu en main dans le chat. */
    public static final String PERMISSION = "servercore.chat.item";

    /** Taille max du JSON de l'item (octets UTF-8). writeUTF plafonne à 65535 ; on reste en deçà. */
    public static final int MAX_ITEM_BYTES = 25000;

    private ChatItemConstants() {
    }
}
```

- [ ] **Step 3: Écrire le test (RED)**

Créer `Core-common/src/test/java/fr/iban/common/chat/ChatItemTokensTest.java` :

```java
package fr.iban.common.chat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatItemTokensTest {

    @Test
    void detectsShortToken() {
        assertTrue(ChatItemTokens.containsToken("regarde mon [i] stylé"));
    }

    @Test
    void detectsLongToken() {
        assertTrue(ChatItemTokens.containsToken("regarde mon [item]"));
    }

    @Test
    void ignoresMessageWithoutToken() {
        assertFalse(ChatItemTokens.containsToken("juste un message [x] normal"));
    }

    @Test
    void replacesAllOccurrencesWithSentinel() {
        String result = ChatItemTokens.replaceWithSentinel("a [i] b [item] c");
        assertEquals("a \uE000 b \uE000 c", result);
        assertFalse(result.contains("[i]"));
        assertFalse(result.contains("[item]"));
    }

    @Test
    void leavesMessageUnchangedWhenNoToken() {
        assertEquals("rien ici", ChatItemTokens.replaceWithSentinel("rien ici"));
    }
}
```

- [ ] **Step 4: Lancer le test → échec attendu (compilation)**

Run: `.\gradlew.bat :core-common:test --tests "fr.iban.common.chat.ChatItemTokensTest"`
Expected: FAIL — `ChatItemTokens` n'existe pas (erreur de compilation : `cannot find symbol`).

- [ ] **Step 5: Implémenter `ChatItemTokens`**

Créer `Core-common/src/main/java/fr/iban/common/chat/ChatItemTokens.java` :

```java
package fr.iban.common.chat;

import java.util.regex.Pattern;

public final class ChatItemTokens {

    private static final Pattern TOKEN = Pattern.compile("\\[(?:i|item)\\]");

    private ChatItemTokens() {
    }

    public static boolean containsToken(String message) {
        return message != null && TOKEN.matcher(message).find();
    }

    public static String replaceWithSentinel(String message) {
        if (message == null) {
            return null;
        }
        return TOKEN.matcher(message).replaceAll(ChatItemConstants.ITEM_PLACEHOLDER);
    }
}
```

- [ ] **Step 6: Lancer le test → succès attendu**

Run: `.\gradlew.bat :core-common:test --tests "fr.iban.common.chat.ChatItemTokensTest"`
Expected: PASS (5 tests).

- [ ] **Step 7: Commit**

```bash
git add Core-common/build.gradle.kts Core-common/src/main/java/fr/iban/common/chat/ChatItemConstants.java Core-common/src/main/java/fr/iban/common/chat/ChatItemTokens.java Core-common/src/test/java/fr/iban/common/chat/ChatItemTokensTest.java
git commit -m "feat(chat): add shared item-token constants and detection"
```

---

## Task 2: Core-common — injection sentinelle → Component + round-trip JSON (TDD)

**Files:**
- Modify: `Core-common/build.gradle.kts`
- Create: `Core-common/src/main/java/fr/iban/common/chat/ChatItemRenderer.java`
- Test: `Core-common/src/test/java/fr/iban/common/chat/ChatItemRendererTest.java`

- [ ] **Step 1: Ajouter les deps Adventure de test**

Dans `Core-common/build.gradle.kts`, compléter le bloc `dependencies` pour qu'il contienne aussi les deux lignes de test Adventure (le bloc final doit ressembler à ceci) :

```kotlin
dependencies {
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("redis.clients:jedis:5.1.3")
    compileOnly("com.google.code.gson:gson:2.10")
    implementation("org.jdbi:jdbi3-core:3.49.6")
    compileOnly("net.kyori:adventure-api:4.17.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.17.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("net.kyori:adventure-api:4.17.0")
    testImplementation("net.kyori:adventure-text-serializer-gson:4.17.0")
}
```

- [ ] **Step 2: Écrire le test (RED)**

Créer `Core-common/src/test/java/fr/iban/common/chat/ChatItemRendererTest.java` :

```java
package fr.iban.common.chat;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatItemRendererTest {

    @Test
    void injectsItemAndRemovesSentinel() {
        Component message = Component.text("voici " + ChatItemConstants.ITEM_PLACEHOLDER + " !");
        Component item = Component.text("[Épée]")
                .hoverEvent(HoverEvent.showText(Component.text("Épée en diamant")));

        Component result = ChatItemRenderer.inject(message, item);

        assertFalse(collectText(result).contains(ChatItemConstants.ITEM_PLACEHOLDER));
        assertTrue(collectText(result).contains("[Épée]"));
        assertTrue(hasHover(result));
    }

    @Test
    void returnsMessageUnchangedWhenItemNull() {
        Component message = Component.text("rien");
        assertSame(message, ChatItemRenderer.inject(message, null));
    }

    @Test
    void roundTripsShowItemHoverThroughGson() {
        Component item = Component.text("[Diamond Sword]")
                .hoverEvent(HoverEvent.showItem(Key.key("minecraft:diamond_sword"), 1));
        String json = GsonComponentSerializer.gson().serialize(item);
        Component restored = GsonComponentSerializer.gson().deserialize(json);

        Component message = Component.text("voici " + ChatItemConstants.ITEM_PLACEHOLDER);
        Component result = ChatItemRenderer.inject(message, restored);

        assertFalse(collectText(result).contains(ChatItemConstants.ITEM_PLACEHOLDER));
        assertTrue(findShowItemHover(result));
    }

    private String collectText(Component c) {
        StringBuilder sb = new StringBuilder();
        if (c instanceof TextComponent tc) {
            sb.append(tc.content());
        }
        for (Component child : c.children()) {
            sb.append(collectText(child));
        }
        return sb.toString();
    }

    private boolean hasHover(Component c) {
        if (c.style().hoverEvent() != null) {
            return true;
        }
        for (Component child : c.children()) {
            if (hasHover(child)) {
                return true;
            }
        }
        return false;
    }

    private boolean findShowItemHover(Component c) {
        HoverEvent<?> hover = c.style().hoverEvent();
        if (hover != null && hover.action() == HoverEvent.Action.SHOW_ITEM) {
            return true;
        }
        for (Component child : c.children()) {
            if (findShowItemHover(child)) {
                return true;
            }
        }
        return false;
    }
}
```

- [ ] **Step 3: Lancer le test → échec attendu (compilation)**

Run: `.\gradlew.bat :core-common:test --tests "fr.iban.common.chat.ChatItemRendererTest"`
Expected: FAIL — `ChatItemRenderer` n'existe pas (`cannot find symbol`).

- [ ] **Step 4: Implémenter `ChatItemRenderer`**

Créer `Core-common/src/main/java/fr/iban/common/chat/ChatItemRenderer.java` :

```java
package fr.iban.common.chat;

import net.kyori.adventure.text.Component;

public final class ChatItemRenderer {

    private ChatItemRenderer() {
    }

    /**
     * Remplace chaque marqueur sentinelle de {@code message} par {@code item}.
     * Retourne {@code message} inchangé si {@code item} est null ou si aucune sentinelle n'est présente.
     */
    public static Component inject(Component message, Component item) {
        if (item == null) {
            return message;
        }
        return message.replaceText(builder ->
                builder.matchLiteral(ChatItemConstants.ITEM_PLACEHOLDER).replacement(item));
    }
}
```

- [ ] **Step 5: Lancer le test → succès attendu**

Run: `.\gradlew.bat :core-common:test --tests "fr.iban.common.chat.ChatItemRendererTest"`
Expected: PASS (3 tests). Le test `roundTripsShowItemHoverThroughGson` valide le round-trip JSON `showItem` (le risque résiduel de la spec).

- [ ] **Step 6: Commit**

```bash
git add Core-common/build.gradle.kts Core-common/src/main/java/fr/iban/common/chat/ChatItemRenderer.java Core-common/src/test/java/fr/iban/common/chat/ChatItemRendererTest.java
git commit -m "feat(chat): add sentinel-to-component item renderer"
```

---

## Task 3: Core-paper — capture de l'item + envoi GlobalItem

**Files:**
- Create: `Core-paper/src/main/java/fr/iban/bukkitcore/utils/ChatItemHelper.java`
- Modify: `Core-paper/src/main/java/fr/iban/bukkitcore/utils/PluginMessageHelper.java`
- Modify: `Core-paper/src/main/java/fr/iban/bukkitcore/listeners/AsyncChatListener.java`

> Pas de test unitaire : `ItemStack.displayName()` dépend du runtime Bukkit. Validation au build (compilation) puis manuelle en jeu (Task 5).

- [ ] **Step 1: Créer `ChatItemHelper`**

Créer `Core-paper/src/main/java/fr/iban/bukkitcore/utils/ChatItemHelper.java` :

```java
package fr.iban.bukkitcore.utils;

import fr.iban.common.chat.ChatItemConstants;
import fr.iban.common.chat.ChatItemTokens;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

public final class ChatItemHelper {

    private ChatItemHelper() {
    }

    /**
     * Retourne le JSON sérialisé de l'item tenu en main principale à partager dans le chat,
     * ou null si : le message ne contient pas de token [i]/[item], le joueur n'a pas la
     * permission {@link ChatItemConstants#PERMISSION}, ou la main principale est vide.
     * Le Component porte le hover natif de l'item (showItem). Si le JSON dépasse
     * {@link ChatItemConstants#MAX_ITEM_BYTES}, on retombe sur le nom seul sans hover.
     */
    @Nullable
    public static String captureHeldItemJson(Player player, String message) {
        if (!ChatItemTokens.containsToken(message)) {
            return null;
        }
        if (!player.hasPermission(ChatItemConstants.PERMISSION)) {
            return null;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.isEmpty()) {
            return null;
        }

        Component display = item.displayName();
        String json = GsonComponentSerializer.gson().serialize(display);

        if (json.getBytes(StandardCharsets.UTF_8).length > ChatItemConstants.MAX_ITEM_BYTES) {
            Component nameOnly = display.hoverEvent((HoverEventSource<?>) null);
            json = GsonComponentSerializer.gson().serialize(nameOnly);
        }

        return json;
    }
}
```

- [ ] **Step 2: Ajouter `sendGlobalItemMessage` à `PluginMessageHelper`**

Dans `Core-paper/src/main/java/fr/iban/bukkitcore/utils/PluginMessageHelper.java`, ajouter cette méthode juste après `sendGlobalMessage` (vers la ligne 45) :

```java
	public static void sendGlobalItemMessage(Player player, String message, String itemJson) {
	    ByteArrayDataOutput out = ByteStreams.newDataOutput();
	    out.writeUTF("GlobalItem");
	    out.writeUTF(player.getUniqueId().toString());
	    out.writeUTF(message);
	    out.writeUTF(itemJson);
	    player.sendPluginMessage(CoreBukkitPlugin.getInstance(), "proxy:chat", out.toByteArray());
	}
```

- [ ] **Step 3: Router le token dans `AsyncChatListener`**

Dans `Core-paper/src/main/java/fr/iban/bukkitcore/listeners/AsyncChatListener.java` :

Ajouter les imports (après la ligne `import fr.iban.bukkitcore.utils.PluginMessageHelper;`) :

```java
import fr.iban.bukkitcore.utils.ChatItemHelper;
import fr.iban.common.chat.ChatItemTokens;
```

Remplacer le bloc existant :

```java
		if(!e.isCancelled() && plugin.getConfig().getBoolean("global-chat", true)) {
			PluginMessageHelper.sendGlobalMessage(player, message);
			e.setCancelled(true);
		}
```

par :

```java
		if(!e.isCancelled() && plugin.getConfig().getBoolean("global-chat", true)) {
			String itemJson = ChatItemHelper.captureHeldItemJson(player, message);
			if (itemJson != null) {
				PluginMessageHelper.sendGlobalItemMessage(player, ChatItemTokens.replaceWithSentinel(message), itemJson);
			} else {
				PluginMessageHelper.sendGlobalMessage(player, message);
			}
			e.setCancelled(true);
		}
```

> Note thread-safety : `AsyncChatEvent` est asynchrone ; `getItemInMainHand()` lit un snapshot de l'inventaire. Le risque (joueur changeant d'item à l'instant exact) est négligeable et au pire donne un item légèrement périmé. Si Paper journalise un avertissement, basculer la capture sur le thread principal via `plugin.getScheduler()` avant l'envoi.

- [ ] **Step 4: Compiler Core-paper**

Run: `.\gradlew.bat :core-paper:compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add Core-paper/src/main/java/fr/iban/bukkitcore/utils/ChatItemHelper.java Core-paper/src/main/java/fr/iban/bukkitcore/utils/PluginMessageHelper.java Core-paper/src/main/java/fr/iban/bukkitcore/listeners/AsyncChatListener.java
git commit -m "feat(chat): capture held item and forward it from Paper"
```

---

## Task 4: Core-velocity — réception GlobalItem + injection dans le message

**Files:**
- Modify: `Core-velocity/src/main/java/fr/iban/velocitycore/listener/PluginMessageListener.java`
- Modify: `Core-velocity/src/main/java/fr/iban/velocitycore/manager/ChatManager.java`

> Pas de test unitaire : dépend du runtime Velocity. Validation au build puis manuelle (Task 5). La logique d'injection elle-même est déjà couverte par les tests de la Task 2.

- [ ] **Step 1: Gérer le sous-type `"GlobalItem"` dans `PluginMessageListener`**

Dans `Core-velocity/src/main/java/fr/iban/velocitycore/listener/PluginMessageListener.java`, remplacer la méthode `handleChatMessage` :

```java
    private void handleChatMessage(byte[] data) {
        ByteArrayDataInput in = ByteStreams.newDataInput(data);
        String sub = in.readUTF();
        if ("Global".equals(sub)) {
            UUID uuid = UUID.fromString(in.readUTF());
            String message = in.readUTF();
            plugin.getChatManager().sendGlobalMessage(uuid, message);
        } else if ("GlobalItem".equals(sub)) {
            UUID uuid = UUID.fromString(in.readUTF());
            String message = in.readUTF();
            String itemJson = in.readUTF();
            plugin.getChatManager().sendGlobalMessage(uuid, message, itemJson);
        }
    }
```

- [ ] **Step 2: Ajouter les imports dans `ChatManager`**

Dans `Core-velocity/src/main/java/fr/iban/velocitycore/manager/ChatManager.java`, ajouter ces imports (à côté des imports `fr.iban.common.*` et `net.kyori.adventure.text.serializer.*` existants) :

```java
import fr.iban.common.chat.ChatItemRenderer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
```

- [ ] **Step 3: Surcharger `sendGlobalMessage` avec l'item**

Dans `ChatManager`, remplacer entièrement la méthode `sendGlobalMessage(UUID, String)` (lignes 47-104) par les deux méthodes suivantes :

```java
    public void sendGlobalMessage(UUID senderUUID, String message) {
        sendGlobalMessage(senderUUID, message, null);
    }

    public void sendGlobalMessage(UUID senderUUID, String message, @Nullable String itemJson) {
        Player sender = server.getPlayer(senderUUID).orElseThrow();

        Component itemComponent = itemJson == null ? null
                : GsonComponentSerializer.gson().deserialize(itemJson);

        if (message.startsWith("$") && sender.hasPermission("servercore.staffchat")) {
            sendStaffMessage(sender, message.substring(1), itemComponent);
            return;
        }

        if (isMuted && !sender.hasPermission("servercore.chatmanage")) {
            return;
        }

        if (sender.hasPermission("servercore.colors")) {
            message = componentToLegacy(parseMineDownInlineFormatting(message));
        }

        MSPlayerProfile senderProfile = playerManager.getProfile(senderUUID);

        if (!senderProfile.getOption(Option.CHAT)) {
            sender.sendMessage(MineDown.parse("&cVous ne pouvez pas envoyer ce message car votre tchat est désactivé"));
            logMessage(MineDown.parse("§8[§CDÉSACTIVÉ§8]§r " + message));
            return;
        }

        String finalMessage = message;
        Component finalItemComponent = itemComponent;
        replacePlaceHolders(plugin.getConfig().getString("chat-format").trim(), sender).thenAccept(chatFormat -> {
            Component prefixComponent = MiniMessage.miniMessage().deserialize(chatFormat);

            for (MSPlayerProfile receiverProfile : playerManager.getProfiles()) {
                Player receiverPlayer = server.getPlayer(receiverProfile.getUniqueId()).orElse(null);
                if (receiverPlayer == null) continue;

                String pmessage = finalMessage;
                String receiverUsername = receiverPlayer.getUsername();

                if (!receiverProfile.getOption(Option.CHAT) || receiverProfile.getIgnoredPlayers().contains(sender.getUniqueId())) {
                    continue;
                }

                if (pmessage.toLowerCase().contains(receiverUsername.toLowerCase()) && receiverProfile.getOption(Option.MENTION)) {
                    String ping = pingPrefix + receiverUsername;
                    String legacyFormattedPing = componentToLegacy(MineDown.parse(ping));
                    receiverPlayer.playSound(Sound.sound(Key.key("block.note_block.guitar"), Sound.Source.MASTER, 1f, 0.5f));
                    pmessage = pmessage.replace(receiverUsername, legacyFormattedPing + "§f");
                }

                Component messageComponent = ChatItemRenderer.inject(componentFromLegacy(pmessage), finalItemComponent);
                Component finalMessageComponent = Component.empty().append(prefixComponent).append(messageComponent);

                receiverPlayer.sendMessage(finalMessageComponent);
            }

            Component loggedMessage = ChatItemRenderer.inject(componentFromLegacy(finalMessage), finalItemComponent);
            logMessage(prefixComponent.append(loggedMessage));
        }).exceptionally(e -> {
            plugin.getLogger().error("Error while sending global message", e);
            return null;
        });
    }
```

- [ ] **Step 4: Propager l'item au chat staff**

Dans `ChatManager`, remplacer la méthode `sendStaffMessage` (lignes 141-156) par :

```java
    private void sendStaffMessage(Player sender, String message, @Nullable Component itemComponent) {
        String prefix = plugin.getConfig().getString("staff-chat-format");
        replacePlaceHolders(prefix, sender).thenAccept(chatFormat -> {
            String chatPrefix = componentToLegacy(MiniMessage.miniMessage().deserialize(chatFormat));
            String messageComponent = componentToLegacy(MineDown.parse(message));
            Component fullMessage = ChatItemRenderer.inject(componentFromLegacy(chatPrefix + messageComponent), itemComponent);

            plugin.getServer().getAllPlayers().forEach(p -> {
                if (p.hasPermission("servercore.staffchat") && !staffChatDisabledPlayers.contains(p.getUniqueId())) {
                    p.sendMessage(fullMessage);
                }
            });

            logMessage(fullMessage);
        });
    }
```

- [ ] **Step 5: Compiler Core-velocity**

Run: `.\gradlew.bat :core-velocity:compileJava`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add Core-velocity/src/main/java/fr/iban/velocitycore/listener/PluginMessageListener.java Core-velocity/src/main/java/fr/iban/velocitycore/manager/ChatManager.java
git commit -m "feat(chat): render shared item in global and staff chat on Velocity"
```

---

## Task 5: Build complet + vérification manuelle en jeu

**Files:** aucun (validation).

- [ ] **Step 1: Build complet de tous les modules**

Run: `.\gradlew.bat build`
Expected: BUILD SUCCESSFUL ; les tests de Core-common passent ; les jars sont copiés dans `../libs/`.

- [ ] **Step 2: Déployer les jars**

Copier le jar `Core-paper` (dans `libs/`) sur le serveur backend Paper et `Core-velocity` sur le proxy Velocity. Redémarrer les deux.

- [ ] **Step 3: Vérifications manuelles**

Donner la permission `servercore.chat.item` à un joueur de test (LuckPerms : `/lp user <pseudo> permission set servercore.chat.item true`), puis vérifier :

- [ ] Tenir une épée en main, taper `regarde mon [i]` → le chat affiche `[Épée ...]` coloré ; survoler montre le tooltip natif (nom, enchantements, lore).
- [ ] Taper `[item]` → même résultat que `[i]`.
- [ ] Plusieurs tokens `a [i] b [i]` → les deux remplacés par l'item.
- [ ] Main vide + `[i]` → le texte `[i]` reste littéral, aucun crash.
- [ ] Joueur SANS la permission + `[i]` → le texte `[i]` reste littéral.
- [ ] Joueur avec `servercore.colors` + `&a[i]` → l'item s'affiche (la sentinelle survit à MineDown).
- [ ] Un shulker rempli d'items en main + `[i]` → s'affiche (hover natif ou, si trop volumineux, nom coloré sans hover ; aucun crash).
- [ ] Message normal sans token → comportement inchangé (non-régression du chat global).

- [ ] **Step 4: Commit éventuel d'ajustements**

Si des correctifs sont nécessaires après test, les committer avec un message `fix(chat): ...`.

---

## Notes d'exécution

- Plateforme Windows / PowerShell : utiliser `.\gradlew.bat`. Les modules sont nommés `:core-common`, `:core-paper`, `:core-velocity` (voir `settings.gradle.kts`).
- Adventure (api + serializers gson/legacy) est fourni par Paper et Velocity au runtime ; Core-common le garde en `compileOnly` et les tests l'ajoutent en `testImplementation`.
- Le canal `proxy:chat` est déjà enregistré côté Paper (`PluginMessageHelper.registerChannels`) et écouté côté Velocity ; aucun nouvel enregistrement de canal n'est nécessaire.
