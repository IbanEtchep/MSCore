package fr.iban.survivalcore.commands;

import fr.iban.survivalcore.SurvivalCorePlugin;
import fr.iban.survivalcore.lang.LangKey;
import fr.iban.survivalcore.lang.MessageBuilder;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.annotation.Usage;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("survivalcore")
public class SurvivalCoreCMD {

    private final SurvivalCorePlugin plugin;

    public SurvivalCoreCMD(SurvivalCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Subcommand("reload")
    @Usage("/survivalcore reload")
    @CommandPermission("survivalcore.reload")
    public void reload(BukkitCommandActor sender) {

        plugin.reloadConfig();
        plugin.getTranslator().reload();

        sender.reply(MessageBuilder.translatable(LangKey.CORE_RELOAD_SUCCESS).toLegacy());
    }

    @Subcommand("reloadlang")
    @Usage("/survivalcore reloadlang")
    @CommandPermission("survivalcore.reload")
    public void reloadLang(BukkitCommandActor sender) {

        plugin.getTranslator().reloadOnlyFile();

        sender.reply(MessageBuilder.translatable(LangKey.CORE_RELOAD_SUCCESS).toLegacy());
    }
}
