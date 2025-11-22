package fr.iban.bukkitcore.listeners;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.lang.LangKey;
import fr.iban.bukkitcore.lang.MessageBuilder;
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
import java.util.UUID;

public class CommandsListener implements Listener {

    private final CoreBukkitPlugin plugin;
    private final Multimap<UUID, String> approvedCommands = ArrayListMultimap.create();

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
        String ip = player.getAddress() != null ? player.getAddress().getHostString() : "unknown";

        if (!plugin.getConfig().getBoolean("command-approval", true)) {
            return;
        }

        if (plugin.getTrustedUserManager().isTrusted(player)) {
            return;
        }

        String command = e.getMessage().split(" ")[0].replace("/", "");

        if (plugin.getTrustedCommandManager().getTrustedBukkitCommands().contains(command.toLowerCase())) {
            return;
        }

        if (approvedCommands.get(player.getUniqueId()).contains(command.toLowerCase())) {
            approvedCommands.remove(player.getUniqueId(), command);
            return;
        }

        Command bukkitCommand = Bukkit.getCommandMap().getCommand(command);
        if (bukkitCommand != null) {
            if (!bukkitCommand.testPermission(player)) return;
            e.setCancelled(true);

            player.sendMessage(MessageBuilder.translatable(LangKey.COMMANDS_APPROVAL_REQUIRED).toLegacy());

            plugin.getApprovalManager().sendRequest(player,
                    MessageBuilder.translatable(LangKey.COMMANDS_APPROVAL_MESSAGE)
                            .placeholder("player", player.getName())
                            .placeholder("ip", ip)
                            .placeholder("command", e.getMessage())
                            .toLegacy(),
                    result -> {
                        if (result) {
                            plugin.getScheduler().runAtEntity(player, task -> {
                                approvedCommands.put(player.getUniqueId(), command);
                                player.chat(e.getMessage());
                            });
                        }
                    }
            );
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandLogger(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();

        if (e.isCancelled()) return;

        GlobalLoggerManager.saveLog(
                plugin.getServerName(),
                player.getName() + " issued server command: " + e.getMessage() + "."
        );
    }
}
