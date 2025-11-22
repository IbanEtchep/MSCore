package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import fr.iban.common.enums.Option;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.lang.LangKey;
import fr.iban.velocitycore.lang.MessageBuilder;
import net.kyori.adventure.text.format.NamedTextColor;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.velocity.annotation.CommandPermission;

public class TpToggleCMD {

    private final CoreVelocityPlugin plugin;

    public TpToggleCMD(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("tptoggle")
    @CommandPermission("servercore.tptoggle")
    public void execute(Player player) {
        PlayerManager accountManager = plugin.getPlayerManager();
        MSPlayerProfile profile = accountManager.getProfile(player.getUniqueId());
        profile.toggleOption(Option.TP);
        accountManager.saveProfile(profile);

        if (profile.getOption(Option.TP)) {
            player.sendMessage(
                    MessageBuilder.translatable(LangKey.TPTOGGLE_OPENED).toComponent()
                            .color(NamedTextColor.GREEN)
            );
        } else {
            player.sendMessage(
                    MessageBuilder.translatable(LangKey.TPTOGGLE_CLOSED).toComponent()
                            .color(NamedTextColor.RED)
            );
        }
    }
}
