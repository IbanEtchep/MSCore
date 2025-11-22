package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.lang.LangKey;
import fr.iban.velocitycore.lang.MessageBuilder;
import net.kyori.adventure.text.Component;
import revxrsal.commands.annotation.*;

import java.util.UUID;

@Command("ignore")
public class IgnoreCommand {

    private final ProxyServer server;
    private final PlayerManager playerManager;

    public IgnoreCommand(CoreVelocityPlugin plugin) {
        this.server = plugin.getServer();
        this.playerManager = plugin.getPlayerManager();
    }

    @CommandPlaceholder
    public void ignore(Player player) {
        help(player);
    }

    @Subcommand("help")
    public void help(Player player) {
        Component message =
                MessageBuilder.translatable(LangKey.IGNORE_HELP_PREFIX).toComponent()
                        .append(MessageBuilder.translatable(LangKey.IGNORE_HELP_ADD).toComponent())
                        .append(MessageBuilder.translatable(LangKey.IGNORE_HELP_ADD_SUFFIX).toComponent())
                        .append(MessageBuilder.translatable(LangKey.IGNORE_HELP_REMOVE).toComponent())
                        .append(MessageBuilder.translatable(LangKey.IGNORE_HELP_REMOVE_SUFFIX).toComponent());

        player.sendMessage(message);
    }

    @Subcommand("add")
    @Usage("/ignore add <player>")
    public void addIgnore(Player player, @Named("player") Player target) {
        if (!player.getUniqueId().equals(target.getUniqueId())) {
            MSPlayerProfile account = playerManager.getProfile(player.getUniqueId());

            if (!account.getIgnoredPlayers().contains(target.getUniqueId())) {
                if (!target.hasPermission("servercore.staff")) {
                    account.getIgnoredPlayers().add(target.getUniqueId());
                    player.sendMessage(
                            MessageBuilder.translatable(LangKey.IGNORE_ADD_SUCCESS)
                                    .placeholder("player", target.getUsername())
                                    .toComponent()
                    );
                    playerManager.saveProfile(account);
                } else {
                    player.sendMessage(MessageBuilder.translatable(LangKey.IGNORE_ADD_STAFF).toComponent());
                }
            } else {
                player.sendMessage(MessageBuilder.translatable(LangKey.IGNORE_ADD_ALREADY).toComponent());
            }
        } else {
            player.sendMessage(MessageBuilder.translatable(LangKey.IGNORE_ADD_SELF).toComponent());
        }
    }

    @Subcommand("remove")
    @Usage("/ignore remove <player>")
    public void removeIgnore(Player player, @Named("player") Player target) {
        MSPlayerProfile account = playerManager.getProfile(player.getUniqueId());

        if (account.getIgnoredPlayers().contains(target.getUniqueId())) {
            account.getIgnoredPlayers().remove(target.getUniqueId());
            player.sendMessage(
                    MessageBuilder.translatable(LangKey.IGNORE_REMOVE_SUCCESS)
                            .placeholder("player", target.getUsername())
                            .toComponent()
            );
            playerManager.saveProfile(account);
        } else {
            player.sendMessage(MessageBuilder.translatable(LangKey.IGNORE_REMOVE_NOT_IGNORED).toComponent());
        }
    }

    @Subcommand("list")
    public void ignoreList(Player player) {
        MSPlayerProfile account = playerManager.getProfile(player.getUniqueId());

        if (!account.getIgnoredPlayers().isEmpty()) {
            player.sendMessage(MessageBuilder.translatable(LangKey.IGNORE_LIST_HEADER).toComponent());
            for (UUID ignoredPlayer : account.getIgnoredPlayers()) {
                String playerName = server.getPlayer(ignoredPlayer)
                        .map(Player::getUsername)
                        .orElse(MessageBuilder.translatable(LangKey.IGNORE_LIST_UNKNOWN).toStringRaw());

                player.sendMessage(
                        MessageBuilder.translatable(LangKey.IGNORE_LIST_ENTRY)
                                .placeholder("player", playerName)
                                .toComponent()
                );
            }
        } else {
            player.sendMessage(MessageBuilder.translatable(LangKey.IGNORE_LIST_EMPTY).toComponent());
        }
    }
}
