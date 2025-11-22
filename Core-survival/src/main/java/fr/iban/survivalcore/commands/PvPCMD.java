package fr.iban.survivalcore.commands;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.common.enums.Option;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.survivalcore.lang.LangKey;
import fr.iban.survivalcore.lang.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.CommandPlaceholder;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("pvp")
public class PvPCMD {

    private final CoreBukkitPlugin plugin;

    public PvPCMD(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @CommandPlaceholder
    @CommandPermission("servercore.pvp")
    public void toggleSelf(Player player) {

        PlayerManager playerManager = plugin.getPlayerManager();
        MSPlayerProfile profile = playerManager.getProfile(player.getUniqueId());

        profile.toggleOption(Option.PVP);

        if (profile.getOption(Option.PVP)) {
            player.sendMessage(MessageBuilder.translatable(LangKey.PVP_ENABLED).toLegacy());
        } else {
            player.sendMessage(MessageBuilder.translatable(LangKey.PVP_DISABLED).toLegacy());
        }

        playerManager.saveProfile(profile);
    }

    @Subcommand("toggle")
    @CommandPermission("servercore.pvp.others")
    public void toggleOther(Player sender, String targetName) {

        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            return;
        }

        PlayerManager playerManager = plugin.getPlayerManager();
        MSPlayerProfile profile = playerManager.getProfile(target.getUniqueId());

        profile.toggleOption(Option.PVP);

        if (profile.getOption(Option.PVP)) {
            sender.sendMessage(
                    MessageBuilder.translatable(LangKey.PVP_ENABLED_OTHER)
                            .placeholder("player", target.getName())
                            .toLegacy()
            );
        } else {
            sender.sendMessage(
                    MessageBuilder.translatable(LangKey.PVP_DISABLED_OTHER)
                            .placeholder("player", target.getName())
                            .toLegacy()
            );
        }

        playerManager.saveProfile(profile);
    }
}
