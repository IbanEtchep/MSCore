package fr.iban.velocitycore.command;

import com.velocitypowered.api.proxy.Player;
import fr.iban.common.messaging.AbstractMessenger;
import fr.iban.velocitycore.CoreVelocityPlugin;
import fr.iban.velocitycore.lang.LangKey;
import fr.iban.velocitycore.lang.MessageBuilder;
import net.kyori.adventure.text.Component;
import revxrsal.commands.annotation.*;
import revxrsal.commands.velocity.actor.VelocityCommandActor;
import revxrsal.commands.velocity.annotation.CommandPermission;

import java.io.IOException;

@Command("vcore")
@CommandPermission("servercore.admin")
public class CoreCMD {

    private final CoreVelocityPlugin plugin;

    public CoreCMD(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @CommandPlaceholder
    public void core(VelocityCommandActor actor) {
        help(actor);
    }

    @Subcommand("help")
    public void help(VelocityCommandActor actor) {
        Component message = MessageBuilder.translatable(LangKey.VCORE_HELP).toComponent();
        actor.reply(message);
    }

    @Subcommand("reload")
    @Usage("/vcore reload")
    public void reloadConfig(VelocityCommandActor actor) throws IOException {
        plugin.getConfig().reload();
        plugin.getAnnounceManager().reloadAnnounces();
        plugin.getTranslator().reload();

        actor.reply(MessageBuilder.translatable(LangKey.VCORE_RELOAD_SUCCESS).toComponent());
    }

    @Subcommand("reloadlang")
    @Usage("/vcore reloadlang")
    public void reloadLang(VelocityCommandActor actor) {
        plugin.getTranslator().reloadOnlyFile();

        actor.reply(MessageBuilder.translatable(LangKey.VCORE_LANG_RELOAD_SUCCESS).toComponent());
    }

    @Subcommand("debug")
    @Usage("/vcore debug")
    public void toggleDebug(Player player) {
        AbstractMessenger messenger = plugin.getMessagingManager().getMessenger();
        messenger.setDebugMode(!messenger.isDebugMode());

        String state = messenger.isDebugMode()
                ? MessageBuilder.translatable(LangKey.VCORE_ENABLED).toStringRaw()
                : MessageBuilder.translatable(LangKey.VCORE_DISABLED).toStringRaw();

        player.sendMessage(
                MessageBuilder.translatable(LangKey.VCORE_DEBUG)
                        .placeholder("state", state)
                        .toComponent()
        );
    }
}
