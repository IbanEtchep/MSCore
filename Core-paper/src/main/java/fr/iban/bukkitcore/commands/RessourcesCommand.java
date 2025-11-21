package fr.iban.bukkitcore.commands;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.commands.annotation.SurvivalServer;
import fr.iban.bukkitcore.manager.RessourcesWorldManager;
import fr.iban.bukkitcore.manager.TeleportManager;
import fr.iban.bukkitcore.menu.RessourceMenu;
import fr.iban.bukkitcore.menu.ServeurMenu;
import fr.iban.bukkitcore.utils.Lang;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Cooldown;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Suggest;

import java.util.concurrent.TimeUnit;

public class RessourcesCommand {

    private final CoreBukkitPlugin plugin;
    private final TeleportManager teleportManager;

    public RessourcesCommand(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
        this.teleportManager = plugin.getTeleportManager();
    }

    @Command("ressources")
    public void ressources(
            Player sender,
            @Suggest({"world", "nether", "end", "lastpos"})
            @Optional String world
    ) {
        if (world == null) {
            new RessourceMenu(sender).open();
        } else {
            RessourcesWorldManager ressourcesWorldManager = plugin.getRessourcesWorldManager();
            switch (world) {
                case "world" -> ressourcesWorldManager.randomTpResourceWorld(sender, "resource_world");
                case "nether" -> ressourcesWorldManager.randomTpResourceWorld(sender, "resource_nether");
                case "end" -> ressourcesWorldManager.randomTpResourceWorld(sender, "resource_end");
                case "lastpos" -> teleportManager.teleport(sender, ressourcesWorldManager.getResourceServerName());
                default -> sender.sendMessage(Lang.get("ressources.invalid-world"));
            }
        }
    }

}
