package fr.iban.bukkitcore.utils;

import fr.iban.bukkitcore.lang.LangKey;
import fr.iban.bukkitcore.lang.MessageBuilder;
import org.bukkit.inventory.ItemStack;
import fr.iban.common.enums.Option;

public enum Options {

    DEATH(LangKey.OPTIONS_DEATH, new ItemStack(Head.DEATH.get()), Option.DEATH_MESSAGE),
    JOIN(LangKey.OPTIONS_JOIN, new ItemStack(Head.PLUS.get()), Option.JOIN_MESSAGE),
    LEAVE(LangKey.OPTIONS_LEAVE, new ItemStack(Head.MOINS.get()), Option.LEAVE_MESSAGE),
    TP(LangKey.OPTIONS_TP, new ItemStack(Head.ENDER_PEARL.get()), Option.TP),
    CHAT(LangKey.OPTIONS_CHAT, new ItemStack(Head.TCHAT.get()), Option.CHAT),
    MENTION(LangKey.OPTIONS_MENTION, new ItemStack(Head.AROBASE.get()), Option.MENTION);

    private final LangKey langKey;
    private final ItemStack item;
    private final Option option;

    Options(LangKey langKey, ItemStack item, Option option) {
        this.langKey = langKey;
        this.item = item;
        this.option = option;
    }

    public String getDisplayName() {
        return MessageBuilder.translatable(langKey).toLegacy();
    }

    public ItemStack getItem() {
        return item;
    }

    public Option getOption() {
        return option;
    }

    public static Options getByDisplayName(String displayName) {
        for (Options option : Options.values()) {
            if (displayName.contains(option.getDisplayName()))
                return option;
        }
        return null;
    }
}
