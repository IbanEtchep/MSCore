package fr.iban.common.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class ChatItemRenderer {

    private ChatItemRenderer() {
    }

    /**
     * Remplace chaque marqueur sentinelle de {@code message} par {@code item}.
     * Retourne {@code message} inchange si {@code item} est null ou si aucune sentinelle n'est presente.
     */
    public static Component inject(Component message, Component item) {
        if (item == null) {
            return message;
        }
        Component sealed = seal(item);
        return message.replaceText(builder ->
                builder.matchLiteral(ChatItemConstants.ITEM_PLACEHOLDER).replacement(sealed));
    }

    /**
     * Isole le style de l'item du contexte : force les décorations et une couleur par défaut sur
     * un wrapper, pour que l'item ne soit pas mis en gras/italique/... ni recoloré par le texte
     * qui l'entoure. Les styles explicitement définis par l'item ou par le format de config
     * restent prioritaires (seul l'héritage est neutralisé). Le texte autour n'est pas affecté.
     */
    static Component seal(Component item) {
        return Component.empty()
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.BOLD, false)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.UNDERLINED, false)
                .decoration(TextDecoration.STRIKETHROUGH, false)
                .decoration(TextDecoration.OBFUSCATED, false)
                .append(item);
    }
}
