package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.iban.common.data.dao.MSPlayerDAO;
import fr.iban.common.manager.PlayerManager;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.velocitycore.CoreVelocityPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import revxrsal.commands.annotation.*;

import java.util.UUID;

@Command("ignore")
public class IgnoreCommand {

    private final CoreVelocityPlugin plugin;
    private final ProxyServer server;
    private final PlayerManager playerManager;

    public IgnoreCommand(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.playerManager = plugin.getPlayerManager();
    }

    @Subcommand("help")
    @CommandPlaceholder
    @Description("Affiche les options de la commande ignore.")
    public void ignore(Player player) {
        Component message = Component.text("Utilisez ", NamedTextColor.GRAY)
                .append(Component.text("/ignore add <joueur>", NamedTextColor.GREEN))
                .append(Component.text(" pour ignorer un joueur.\n", NamedTextColor.GRAY))
                .append(Component.text("/ignore remove <joueur>", NamedTextColor.GREEN))
                .append(Component.text(" pour ne plus ignorer un joueur.", NamedTextColor.GRAY));
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
                    player.sendMessage(Component.text("Vous ignorez maintenant " + target.getUsername() + ".", NamedTextColor.GREEN));
                    playerManager.saveProfile(account);
                } else {
                    player.sendMessage(Component.text("Vous ne pouvez pas ignorer un membre du staff !", NamedTextColor.RED));
                }
            } else {
                player.sendMessage(Component.text("Ce joueur est déjà ignoré.", NamedTextColor.RED));
            }
        } else {
            player.sendMessage(Component.text("Vous ne pouvez pas vous ignorer vous-même.", NamedTextColor.RED));
        }
    }

    @Subcommand("remove")
    @Description("Retire un joueur de la liste des ignorés.")
    @Usage("/ignore remove <joueur>")
    public void removeIgnore(Player player, @Named("joueur") Player target) {
        MSPlayerProfile account = playerManager.getProfile(player.getUniqueId());

        if (account.getIgnoredPlayers().contains(target.getUniqueId())) {
            account.getIgnoredPlayers().remove(target.getUniqueId());
            player.sendMessage(Component.text("Vous n'ignorez plus " + target.getUsername() + ".", NamedTextColor.GREEN));
            new MSPlayerDAO().removeIgnoredPlayer(target.getUniqueId(), target.getUniqueId());
        } else {
            player.sendMessage(Component.text("Ce joueur n'est pas ignoré.", NamedTextColor.RED));
        }
    }

    @Subcommand("list")
    @Description("Affiche la liste des joueurs ignorés.")
    public void ignoreList(Player player) {
        MSPlayerProfile account = playerManager.getProfile(player.getUniqueId());

        if (!account.getIgnoredPlayers().isEmpty()) {
            player.sendMessage(Component.text("|| Joueurs Ignorés ||").color(NamedTextColor.GRAY));
            for (UUID ignoredPlayer : account.getIgnoredPlayers()) {
                String playerName = server.getPlayer(ignoredPlayer).map(Player::getUsername).orElse("Joueur Inconnu");
                player.sendMessage(Component.text("- " + playerName, NamedTextColor.GREEN));
            }
        } else {
            player.sendMessage(Component.text("Vous n'ignorez personne.", NamedTextColor.RED));
        }
    }
}
