package fr.iban.common.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * Parseur des textes de configuration : accepte MiniMessage et les codes couleur legacy dans la
 * meme chaine. Les codes legacy sont traduits en balises MiniMessage avant la deserialisation,
 * ce qui laisse une seule syntaxe a interpreter en sortie.
 *
 * <p>Formats legacy reconnus, avec {@code &} ou {@code §} comme marqueur :
 * <ul>
 *   <li>{@code &0}-{@code &f} couleurs, {@code &k}-{@code &o} decorations, {@code &r} reset</li>
 *   <li>{@code &#rrggbb} (Essentials) et {@code &#rrggbb&} (MineDown)</li>
 *   <li>{@code &x&r&r&g&g&b&b} (Spigot)</li>
 * </ul>
 * {@code \&} produit un marqueur litteral.
 *
 * <p>Une couleur legacy emet {@code <reset>} avant sa balise : en legacy un code couleur efface
 * les decorations en cours, alors qu'en MiniMessage une couleur ne ferme pas un {@code <bold>}
 * ouvert. Sans ce reset, une chaine legacy existante ne rendrait plus comme avant.
 *
 * <p><b>A n'appliquer qu'a des textes de confiance</b> (configuration, messages du plugin), jamais
 * a du texte tape par un joueur : les balises MiniMessage y sont interpretees, {@code <click>} et
 * {@code <hover>} compris. Le contenu joueur doit passer par un
 * {@link net.kyori.adventure.text.minimessage.tag.resolver.Placeholder#unparsed}.
 */
public final class MessageParser {

    private static final String[] COLORS = {
            "black", "dark_blue", "dark_green", "dark_aqua",
            "dark_red", "dark_purple", "gold", "gray",
            "dark_gray", "blue", "green", "aqua",
            "red", "light_purple", "yellow", "white"
    };

    private MessageParser() {
    }

    /**
     * Convertit les codes legacy puis deserialise le resultat en MiniMessage.
     */
    public static Component parse(String message, TagResolver... resolvers) {
        if (message == null) {
            return Component.empty();
        }
        return MiniMessage.miniMessage().deserialize(legacyToMiniMessage(message), resolvers);
    }

    /**
     * Traduit les codes legacy de {@code message} en balises MiniMessage. Les balises MiniMessage
     * deja presentes sont laissees telles quelles.
     */
    public static String legacyToMiniMessage(String message) {
        if (message == null) {
            return "";
        }

        StringBuilder out = new StringBuilder(message.length() + 16);
        int i = 0;

        while (i < message.length()) {
            char c = message.charAt(i);

            if (c == '\\' && i + 1 < message.length() && isMarker(message.charAt(i + 1))) {
                out.append(message.charAt(i + 1));
                i += 2;
                continue;
            }

            if (!isMarker(c)) {
                out.append(c);
                i++;
                continue;
            }

            int consumed = appendCode(message, i, out);
            if (consumed == 0) {
                out.append(c);
                i++;
            } else {
                i += consumed;
            }
        }

        return out.toString();
    }

    /**
     * Tente de lire un code legacy commencant a {@code start}. Retourne le nombre de caracteres
     * consommes, ou 0 si le marqueur n'ouvre aucun code connu.
     */
    private static int appendCode(String message, int start, StringBuilder out) {
        if (start + 1 >= message.length()) {
            return 0;
        }

        char marker = message.charAt(start);
        char code = Character.toLowerCase(message.charAt(start + 1));

        if (code == 'x') {
            String hex = readSpigotHex(message, start, marker);
            if (hex != null) {
                out.append("<reset><color:#").append(hex).append('>');
                return 14;
            }
            return 0;
        }

        if (code == '#') {
            String hex = readHex(message, start + 2);
            if (hex != null) {
                out.append("<reset><color:#").append(hex).append('>');
                return 8 + trailingMarkerLength(message, start + 8, marker);
            }
            return 0;
        }

        int color = "0123456789abcdef".indexOf(code);
        if (color >= 0) {
            out.append("<reset><").append(COLORS[color]).append('>');
            return 2;
        }

        switch (code) {
            case 'k' -> out.append("<obfuscated>");
            case 'l' -> out.append("<bold>");
            case 'm' -> out.append("<strikethrough>");
            case 'n' -> out.append("<underlined>");
            case 'o' -> out.append("<italic>");
            case 'r' -> out.append("<reset>");
            default -> {
                return 0;
            }
        }
        return 2;
    }

    /**
     * Lit la forme Spigot {@code &x&r&r&g&g&b&b} : six paires marqueur + chiffre hexadecimal.
     */
    private static String readSpigotHex(String message, int start, char marker) {
        if (start + 14 > message.length()) {
            return null;
        }
        StringBuilder hex = new StringBuilder(6);
        for (int pair = 0; pair < 6; pair++) {
            int at = start + 2 + pair * 2;
            if (message.charAt(at) != marker || !isHexDigit(message.charAt(at + 1))) {
                return null;
            }
            hex.append(message.charAt(at + 1));
        }
        return hex.toString();
    }

    private static String readHex(String message, int start) {
        if (start + 6 > message.length()) {
            return null;
        }
        for (int offset = 0; offset < 6; offset++) {
            if (!isHexDigit(message.charAt(start + offset))) {
                return null;
            }
        }
        return message.substring(start, start + 6);
    }

    /**
     * MineDown termine ses couleurs hexadecimales par un marqueur ({@code &#rrggbb&}). On ne le
     * consomme que s'il n'ouvre pas lui-meme un autre code, pour ne pas avaler le {@code &l} de
     * {@code &#ff0000&lGras}.
     */
    private static int trailingMarkerLength(String message, int at, char marker) {
        if (at >= message.length() || message.charAt(at) != marker) {
            return 0;
        }
        if (at + 1 < message.length() && isCodeStart(message.charAt(at + 1))) {
            return 0;
        }
        return 1;
    }

    private static boolean isCodeStart(char c) {
        char lower = Character.toLowerCase(c);
        return "0123456789abcdefklmnorx#".indexOf(lower) >= 0;
    }

    private static boolean isHexDigit(char c) {
        return "0123456789abcdefABCDEF".indexOf(c) >= 0;
    }

    private static boolean isMarker(char c) {
        return c == '&' || c == '§';
    }
}
