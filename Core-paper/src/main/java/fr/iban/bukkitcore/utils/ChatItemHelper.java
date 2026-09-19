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
     * Retourne le JSON serialise de l'item tenu en main principale a partager dans le chat,
     * ou null si : le message ne contient pas de token [i]/[item], le joueur n'a pas la
     * permission {@link ChatItemConstants#PERMISSION}, ou la main principale est vide.
     * Le Component porte le hover natif de l'item (showItem). Si le JSON depasse
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
