package fr.iban.survivalcore.commands;

import fr.iban.survivalcore.SurvivalCorePlugin;
import fr.iban.survivalcore.manager.AnnounceManager;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;

public class AnnounceCMD {

    private final SurvivalCorePlugin plugin;

    public AnnounceCMD(SurvivalCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Command("announce")
    public void sendAnnounce(Player player, String message) {
        AnnounceManager announceManager = plugin.getAnnounceManager();
        announceManager.sendAnnounce(player, message);
    }

}
