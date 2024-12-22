package fr.iban.bukkitcore.commands;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.common.messaging.AbstractMessenger;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("core")
public class CoreCMD {

    private final CoreBukkitPlugin plugin;

    public CoreCMD(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("reload")
    @CommandPermission("core.reload")
    public void reload(BukkitCommandActor sender) {
        plugin.reloadConfig();
        plugin.setServerName(plugin.getConfig().getString("servername"));
        sender.reply("§apapercore reloaded.");
    }

    @Command("debug")
    @CommandPermission("core.debug")
    public void debug(BukkitCommandActor sender) {
        AbstractMessenger messenger = plugin.getMessagingManager().getMessenger();
        messenger.setDebugMode(!messenger.isDebugMode());
        sender.reply("debug mode : " + messenger.isDebugMode());
    }

}
