package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.lang.LangKey;
import fr.iban.velocitycore.lang.MessageBuilder;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.velocity.annotation.CommandPermission;

import java.io.IOException;
import java.util.List;

public class TabCompleteCMD {

    private final CoreVelocityPlugin plugin;

    public TabCompleteCMD(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Command("addtabcomplete")
    @CommandPermission("servercore.addtabcomplete")
    @Usage("/addtabcomplete <global/moderation> <command>")
    public void execute(Player sender, String message) throws IOException {
        String[] args = message.split(" ");

        if (args.length <= 1) {
            sender.sendMessage(
                    MessageBuilder.translatable(LangKey.TABCOMPLETE_USAGE).toComponent()
            );
        } else if (args.length == 2) {
            switch (args[0]) {
                case "global" -> addCommandToGroup(sender, "global", args[1]);
                case "moderation" -> addCommandToGroup(sender, "moderation", args[1]);
                default -> sender.sendMessage(
                        MessageBuilder.translatable(LangKey.TABCOMPLETE_UNKNOWN_GROUP).toComponent()
                );
            }
        }
    }

    private void addCommandToGroup(Player sender, String group, String command) throws IOException {
        String path = "tabcomplete." + group;
        List<String> list = plugin.getConfig().getStringList(path);
        list.add(command);
        plugin.getConfig().set(path, list);

        sender.sendMessage(
                MessageBuilder.translatable(LangKey.TABCOMPLETE_ADDED)
                        .placeholder("command", command)
                        .placeholder("group", group)
                        .toComponent()
        );

        plugin.getConfig().save();
    }
}
