package fr.iban.bukkitcore.commands;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.commands.annotation.Context;
import fr.iban.bukkitcore.commands.annotation.SenderType;
import fr.iban.common.TrustedCommand;
import fr.iban.common.manager.TrustedCommandsManager;
import org.bukkit.Bukkit;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("trustcommand")
@CommandPermission("core.trustcommand")
public class TrustCommandsCMD {

    private final CoreBukkitPlugin plugin;
    private final TrustedCommandsManager trustedCommandsManager;

    public TrustCommandsCMD(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
        this.trustedCommandsManager = plugin.getTrustedCommandManager();
    }

    @Subcommand("add")
    public void addTrustedCommand(BukkitCommandActor sender,
                                  @Named("command") String command,
                                  @Named("senderType") @SenderType String senderType,
                                  @Named("context") @Default("global") @Context String context) {

        TrustedCommand trustedCommand = new TrustedCommand(command, senderType, context);
        if(trustedCommandsManager.getTrustedCommands().contains(trustedCommand)) {
            sender.reply("§cCette commande est déjà ajoutée.");
            return;
        }

        trustedCommandsManager.addTrustedCommand(trustedCommand);
        sender.reply("§aCommande ajoutée à la liste.");
    }

    @Subcommand("remove")
    public void removeTrustedCommand(
            BukkitCommandActor sender,
            @Named("command") String command,
            @Named("senderType") @SenderType String senderType,
            @Named("context") @Default("global") @Context String context) {
        TrustedCommand trustedCommand = new TrustedCommand(command, senderType, context);
        if(!trustedCommandsManager.getTrustedCommands().contains(trustedCommand)) {
            sender.reply("§cCette n'est pas dans la liste.");
            return;
        }

        trustedCommandsManager.deleteTrustedCommand(trustedCommand);
        sender.reply("§aCommande retirée de la liste.");
    }

    @Subcommand("transferToSql")
    public void transferToSql(BukkitCommandActor sender) {
        for (String command : plugin.getConfig().getStringList("tabcomplete.global")) {
            trustedCommandsManager.addTrustedCommand(new TrustedCommand(command, "player", "bukkit"));
        }
        for (String command : plugin.getConfig().getStringList("tabcomplete.moderation")) {
            trustedCommandsManager.addTrustedCommand(new TrustedCommand(command, "staff", "bukkit"));
        }
    }

    @Subcommand("reload")
    public void reload(BukkitCommandActor sender) {
        plugin.getScheduler().runAsync(task -> {
            trustedCommandsManager.loadTrustedCommands();
            sender.reply("§aCommandes rechargées.");
        });
    }

}
