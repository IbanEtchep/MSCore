package fr.iban.bukkitcore.commands;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.commands.annotation.SurvivalServer;
import fr.iban.bukkitcore.manager.TeleportManager;
import fr.iban.bukkitcore.menu.ServeurMenu;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Cooldown;
import revxrsal.commands.annotation.Optional;

import java.util.concurrent.TimeUnit;


public class ServerSwitchCommands {

    private final CoreBukkitPlugin plugin;
    private final TeleportManager teleportManager;

    public ServerSwitchCommands(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
        this.teleportManager = plugin.getTeleportManager();
    }

    @Command("survie")
    public void survie(Player sender, @Optional @SurvivalServer String server) {
        teleportManager.teleportToSurvivalServer(sender, server);
    }

    @Command("serveur")
    public void serveurMenu(Player sender) {
        new ServeurMenu(sender).open();
    }

    @Command("survivalrtp")
    @Cooldown(value = 2, unit = TimeUnit.MINUTES)
    public void survivalRandomTP(Player player,@Optional String server) {
        if(server == null) {
            plugin.getTeleportManager().randomTeleportToSurvivalServer(player);
        } else {
            plugin.getTeleportManager().randomTeleport(player, server, "world");
        }
    }

}
