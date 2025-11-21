package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import fr.iban.common.enums.Option;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.util.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.velocity.annotation.CommandPermission;

public class MsgToggleCMD {

    private final CoreVelocityPlugin plugin;

    public MsgToggleCMD(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("msgtoggle")
    @CommandPermission("servercore.msgtoggle")
    @Description("Permet d'activer ou désactiver la réception de messages de la part des autres joueurs.")
    public void execute(Player player) {
        PlayerManager playerManager = plugin.getPlayerManager();
        MSPlayerProfile account = playerManager.getProfile(player.getUniqueId());

        if (account.getOption(Option.MSG)) {
            account.setOption(Option.MSG, false);
            player.sendMessage(Component.text(Lang.get("msgtoggle.disabled"), NamedTextColor.RED));
        } else {
            account.setOption(Option.MSG, true);
            player.sendMessage(Component.text(Lang.get("msgtoggle.enabled"), NamedTextColor.GREEN));
        }

        playerManager.saveProfile(account);
    }
}
