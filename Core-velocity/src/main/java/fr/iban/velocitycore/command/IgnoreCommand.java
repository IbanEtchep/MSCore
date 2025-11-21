package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.iban.common.data.dao.MSPlayerDAO;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.util.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
    @Description("Affiche les options de la commande ignore.")
    public void help(Player player) {
        Component message = Component.text(Lang.get("ignore.help.prefix"), NamedTextColor.GRAY)
                .append(Component.text(Lang.get("ignore.help.add"), NamedTextColor.GREEN))
                .append(Component.text(Lang.get("ignore.help.add_suffix"), NamedTextColor.GRAY))
                .append(Component.text(Lang.get("ignore.help.remove"), NamedTextColor.GREEN))
                .append(Component.text(Lang.get("ignore.help.remove_suffix"), NamedTextColor.GRAY));
        player.sendMessage(message);
    }

    @Subcommand("add")
    @Description("Ajoute un joueur à la liste des ignorés.")
    @Usage("/ignore add <joueur>")
    public void addIgnore(Player player, @Named("joueur") Player target) {
        if (!player.getUniqueId().equals(target.getUniqueId())) {
            MSPlayerProfile account = playerManager.getProfile(player.getUniqueId());

            if (!account.getIgnoredPlayers().contains(target.getUniqueId())) {
                if (!target.hasPermission("servercore.staff")) {
                    account.getIgnoredPlayers().add(target.getUniqueId());
                    player.sendMessage(Component.text(Lang.get("ignore.add.success").replace("%player%", target.getUsername()), NamedTextColor.GREEN));
                    playerManager.saveProfile(account);
                } else {
                    player.sendMessage(Component.text(Lang.get("ignore.add.staff"), NamedTextColor.RED));
                }
            } else {
                player.sendMessage(Component.text(Lang.get("ignore.add.already"), NamedTextColor.RED));
            }
        } else {
            player.sendMessage(Component.text(Lang.get("ignore.add.self"), NamedTextColor.RED));
        }
    }

    @Subcommand("remove")
    @Description("Retire un joueur de la liste des ignorés.")
    @Usage("/ignore remove <joueur>")
    public void removeIgnore(Player player, @Named("joueur") Player target) {
        MSPlayerProfile account = playerManager.getProfile(player.getUniqueId());

        if (account.getIgnoredPlayers().contains(target.getUniqueId())) {
            account.getIgnoredPlayers().remove(target.getUniqueId());
            player.sendMessage(Component.text(Lang.get("ignore.remove.success").replace("%player%", target.getUsername()), NamedTextColor.GREEN));
            playerManager.saveProfile(account);
        } else {
            player.sendMessage(Component.text(Lang.get("ignore.remove.not_ignored"), NamedTextColor.RED));
        }
    }

    @Subcommand("list")
    @Description("Affiche la liste des joueurs ignorés.")
    public void ignoreList(Player player) {
        MSPlayerProfile account = playerManager.getProfile(player.getUniqueId());

        if (!account.getIgnoredPlayers().isEmpty()) {
            player.sendMessage(Component.text(Lang.get("ignore.list.header")).color(NamedTextColor.GRAY));
            for (UUID ignoredPlayer : account.getIgnoredPlayers()) {
                String playerName = server.getPlayer(ignoredPlayer).map(Player::getUsername).orElse(Lang.get("ignore.list.unknown"));
                player.sendMessage(Component.text(Lang.get("ignore.list.entry").replace("%player%", playerName), NamedTextColor.GREEN));
            }
        } else {
            player.sendMessage(Component.text(Lang.get("ignore.list.empty"), NamedTextColor.RED));
        }
    }
}
