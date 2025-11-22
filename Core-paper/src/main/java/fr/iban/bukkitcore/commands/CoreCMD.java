package fr.iban.bukkitcore.commands;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.lang.LangKey;
import fr.iban.bukkitcore.lang.MessageBuilder;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("core")
public class CoreCMD {

    private final CoreBukkitPlugin plugin;

    public CoreCMD(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Subcommand("reload")
    @CommandPermission("core.reload")
    public void reload(BukkitCommandActor sender) {
        plugin.reloadConfig();
        plugin.setServerName(plugin.getConfig().getString("servername"));
        plugin.getTranslator().reload(plugin);

        sender.reply(
                MessageBuilder.translatable(LangKey.CORE_RELOAD_SUCCESS).toComponent()
        );
    }

    @Subcommand("reloadlang")
    @CommandPermission("core.reload")
    public void reloadLang(BukkitCommandActor sender) {
        plugin.getTranslator().reloadOnlyFile(plugin);

        sender.reply(
                MessageBuilder.translatable(LangKey.CORE_LANG_RELOAD_SUCCESS).toComponent()
        );
    }

    @Subcommand("debug")
    @CommandPermission("core.debug")
    public void debug(BukkitCommandActor sender) {
        Player player = sender.requirePlayer();
        var messenger = plugin.getMessagingManager().getMessenger();
        messenger.setDebugMode(!messenger.isDebugMode());

        String state = messenger.isDebugMode()
                ? MessageBuilder.translatable(LangKey.CORE_ENABLED).toLegacy()
                : MessageBuilder.translatable(LangKey.CORE_DISABLED).toLegacy();

        player.sendMessage(
                MessageBuilder.translatable(LangKey.CORE_DEBUG)
                        .placeholder("state", state)
                        .toComponent()
        );
    }
}
