package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import fr.iban.common.enums.Option;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.lang.LangKey;
import fr.iban.velocitycore.lang.MessageBuilder;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.velocity.annotation.CommandPermission;

public class MsgToggleCMD {

    private final CoreVelocityPlugin plugin;

    public MsgToggleCMD(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("msgtoggle")
    @CommandPermission("servercore.msgtoggle")
    public void execute(Player player) {
        PlayerManager playerManager = plugin.getPlayerManager();
        MSPlayerProfile account = playerManager.getProfile(player.getUniqueId());

        if (account.getOption(Option.MSG)) {
            account.setOption(Option.MSG, false);
            player.sendMessage(MessageBuilder.translatable(LangKey.MSGTOGGLE_DISABLED).toComponent());
        } else {
            account.setOption(Option.MSG, true);
            player.sendMessage(MessageBuilder.translatable(LangKey.MSGTOGGLE_ENABLED).toComponent());
        }

        playerManager.saveProfile(account);
    }
}
