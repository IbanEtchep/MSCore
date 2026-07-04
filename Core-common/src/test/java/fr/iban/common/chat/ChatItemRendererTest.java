package fr.iban.common.chat;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatItemRendererTest {

    @Test
    void injectsItemAndRemovesSentinel() {
        Component message = Component.text("voici " + ChatItemConstants.ITEM_PLACEHOLDER + " !");
        Component item = Component.text("[Epee]")
                .hoverEvent(HoverEvent.showText(Component.text("Epee en diamant")));

        Component result = ChatItemRenderer.inject(message, item);

        assertFalse(collectText(result).contains(ChatItemConstants.ITEM_PLACEHOLDER));
        assertTrue(collectText(result).contains("[Epee]"));
        assertTrue(hasHover(result));
    }

    @Test
    void returnsMessageUnchangedWhenItemNull() {
        Component message = Component.text("rien");
        assertSame(message, ChatItemRenderer.inject(message, null));
    }

    @Test
    void sealNeutralizesInheritedDecorations() {
        Component item = Component.text("[Epee]");
        Component sealed = ChatItemRenderer.seal(item);

        assertEquals(TextDecoration.State.FALSE, sealed.style().decoration(TextDecoration.BOLD));
        assertEquals(TextDecoration.State.FALSE, sealed.style().decoration(TextDecoration.ITALIC));
        assertEquals(NamedTextColor.WHITE, sealed.color());
        assertTrue(collectText(sealed).contains("[Epee]"));
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
