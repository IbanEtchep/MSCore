package fr.iban.bukkitcore.commands;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.utils.ChatUtils;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

public class BungeeBroadcastCMD{

    private final CoreBukkitPlugin plugin;

    public BungeeBroadcastCMD(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @revxrsal.commands.annotation.Command({
        "bungeebroadcast",
        "bbc"
    })
    public void onBungeeBroadcastCommand(BukkitCommandActor actor, String message) {
        plugin.getPlayerManager().broadcastMessage(ChatUtils.translateColors(message));
    }
}
