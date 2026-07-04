package fr.iban.bukkitcore.listeners;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.common.manager.GlobalLoggerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.ArrayList;
import java.util.List;

public class CommandsListener implements Listener {

    private final CoreBukkitPlugin plugin;

    public CommandsListener(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent e) {
        Player player = e.getPlayer();

        if (player.hasPermission("servercore.admin")) {
            return;
        }

        List<String> allowed = new ArrayList<>(plugin.getTrustedCommandManager().getBukkitPlayerCommands());

        if (player.hasPermission("servercore.moderation")) {
            allowed.addAll(plugin.getTrustedCommandManager().getBukkitStaffCommands());
        }

        e.getCommands().clear();
        e.getCommands().addAll(allowed);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();

        if (!plugin.getConfig().getBoolean("command-curation", false)) {
            return;
        }
        if (player.hasPermission("servercore.admin")) {
            return;
        }

        String command = e.getMessage().split(" ")[0].replace("/", "");

        if (plugin.getTrustedCommandManager().getTrustedBukkitCommands().contains(command.toLowerCase())) {
            return;
        }

        Command bukkitCommand = Bukkit.getCommandMap().getCommand(command);
        if (bukkitCommand == null || !bukkitCommand.testPermission(player)) {
            return;
        }

        plugin.getCommandCurationManager().request(command, "bukkit", player.getName());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandLogger(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();

        if (e.isCancelled()) return;

        GlobalLoggerManager.saveLog(plugin.getServerName(), player.getName() + " issued server command: " + e.getMessage() + ".");
    }
}
