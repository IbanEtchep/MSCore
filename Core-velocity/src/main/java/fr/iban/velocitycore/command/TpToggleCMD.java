package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import fr.iban.common.enums.Option;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.velocitycore.CoreVelocityPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.velocity.annotation.CommandPermission;

public class TpToggleCMD {

    private final CoreVelocityPlugin plugin;

    public TpToggleCMD(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("tptoggle")
    @Description("Active ou désactive les demandes de téléportation pour le joueur.")
    @CommandPermission("servercore.tptoggle")
    public void execute(Player player) {
        PlayerManager accountManager = plugin.getPlayerManager();
        MSPlayerProfile profile = accountManager.getProfile(player.getUniqueId());
        profile.toggleOption(Option.TP);
        accountManager.saveProfile(profile);

        if (profile.getOption(Option.TP)) {
            player.sendMessage(Component.text("Vos demandes de téléportation sont maintenant ouvertes.", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Vos demandes de téléportation sont maintenant fermées.", NamedTextColor.RED));
        }
    }
}
