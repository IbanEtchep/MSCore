package fr.iban.bukkitcore.commands;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.manager.TeleportManager;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.common.teleport.RequestType;
import fr.iban.common.teleport.TpRequest;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.UUID;


public class TeleportCommands {

    private final CoreBukkitPlugin plugin;
    private final TeleportManager teleportManager;

    public TeleportCommands(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
        this.teleportManager = plugin.getTeleportManager();
    }

    @Command("tpa")
    @CommandPermission("core.tpa")
    public void tpa(Player sender, @Named("cible") MSPlayerProfile target) {
        UUID targetUniqueId = target.getUniqueId();

        if (targetUniqueId.equals(sender.getUniqueId())) {
            sender.sendMessage("§cVous ne pouvez pas vous téléporter à vous même.");
            return;
        }

        teleportManager.sendTeleportRequest(sender.getUniqueId(), targetUniqueId, RequestType.TP);
    }

    @Command("tp")
    @CommandPermission("core.tp")
    public void tp(Player sender, @Named("cible") MSPlayerProfile target) {
        UUID targetUniqueId = target.getUniqueId();

        if (targetUniqueId.equals(sender.getUniqueId())) {
            sender.sendMessage("§cVous ne pouvez pas vous téléporter à vous même.");
            return;
        }

        plugin.getTeleportManager().teleport(sender.getUniqueId(), targetUniqueId);
    }

    @Command("tpahere")
    @CommandPermission("core.tpahere")
    public void tpahere(Player sender, @Named("cible") MSPlayerProfile target) {
        UUID targetUniqueId = target.getUniqueId();

        if (targetUniqueId.equals(sender.getUniqueId())) {
            sender.sendMessage("§cVous ne pouvez pas vous téléporter à vous même.");
            return;
        }

        teleportManager.sendTeleportRequest(sender.getUniqueId(), targetUniqueId, RequestType.TPHERE);
    }

    @Command({"tphere", "s"})
    @CommandPermission("core.tphere")
    public void tphere(Player sender, @Named("cible") MSPlayerProfile target) {
        UUID targetUniqueId = target.getUniqueId();

        if (targetUniqueId.equals(sender.getUniqueId())) {
            sender.sendMessage("§cVous ne pouvez pas vous téléporter à vous même.");
            return;
        }

        plugin.getTeleportManager().teleport(targetUniqueId, sender.getUniqueId());
    }

    @Command({"tpyes", "tpaccept"})
    @CommandPermission("core.tpyes")
    public void tpyes(Player sender, @Optional MSPlayerProfile target) {
        if (target != null) {
            UUID targetUniqueId = target.getUniqueId();
            TpRequest request = teleportManager.getTpRequestFrom(sender, targetUniqueId);
            if (request != null) {
                if (request.getRequestType() == RequestType.TP) {
                    teleportManager.teleport(targetUniqueId, sender.getUniqueId(), 3);
                } else if (request.getRequestType() == RequestType.TPHERE) {
                    teleportManager.teleport(sender.getUniqueId(), targetUniqueId, 3);
                }
                sender.sendMessage("§aDemande de téléportation acceptée.");
                teleportManager.removeTpRequest(sender.getUniqueId(), request);
            } else {
                sender.sendMessage("§cVous n'avez pas de requête de téléportation de ce joueur.");
            }
        } else if (!teleportManager.getTpRequests(sender).isEmpty()) {
            TpRequest request = teleportManager.getTpRequests(sender).getLast();
            if (request.getRequestType() == RequestType.TP) {
                teleportManager.teleport(request.getPlayerFrom(), request.getPlayerTo(), 3);
            } else if (request.getRequestType() == RequestType.TPHERE) {
                teleportManager.teleport(request.getPlayerTo(), request.getPlayerFrom(), 3);
            }
            sender.sendMessage("§aDemande de téléportation acceptée.");
            teleportManager.removeTpRequest(sender.getUniqueId(), request);
        } else {
            sender.sendMessage("§cVous n'avez pas de requête de téléportation.");
        }
    }

    @Command({"tpno", "tpdeny"})
    @CommandPermission("core.tpno")
    public void tpno(Player sender, @Optional MSPlayerProfile target) {
        if (target != null) {
            UUID targetUniqueId = target.getUniqueId();
            TpRequest request = teleportManager.getTpRequestFrom(sender, targetUniqueId);
            if (request != null) {
                sender.sendMessage("§cDemande de téléportation rejetée.");
                teleportManager.removeTpRequest(sender.getUniqueId(), request);
            } else {
                sender.sendMessage("§cVous n'avez pas de requête de téléportation de ce joueur.");
            }
        } else if (!teleportManager.getTpRequests(sender).isEmpty()) {
            TpRequest request = teleportManager.getTpRequests(sender).getLast();
            sender.sendMessage("§cDemande de téléportation rejetée.");
            teleportManager.removeTpRequest(sender.getUniqueId(), request);
        } else {
            sender.sendMessage("§cVous n'avez pas de requête de téléportation.");
        }
    }

    @Command("tplastunsafe")
    public void tpLastUnsafe(Player player) {
        teleportManager.tpAsyncLastUnsafe(player);
    }

}
