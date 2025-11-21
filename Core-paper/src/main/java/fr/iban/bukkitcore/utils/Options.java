package fr.iban.bukkitcore.utils;

import org.bukkit.inventory.ItemStack;

import fr.iban.common.enums.Option;

public enum Options {
	
	DEATH(Lang.get("options.death"), new ItemStack(Head.DEATH.get()), Option.DEATH_MESSAGE),
	JOIN(Lang.get("options.join"), new ItemStack(Head.PLUS.get()), Option.JOIN_MESSAGE),
	LEAVE(Lang.get("options.leave"), new ItemStack(Head.MOINS.get()), Option.LEAVE_MESSAGE),
	TP(Lang.get("options.tp"), new ItemStack(Head.ENDER_PEARL.get()), Option.TP),
	CHAT(Lang.get("options.chat"), new ItemStack(Head.TCHAT.get()), Option.CHAT),
	MENTION(Lang.get("options.mention"), new ItemStack(Head.AROBASE.get()), Option.MENTION);
	
	private final String displayName;
	private final ItemStack item;
	private final Option option;
	
	Options(String displayName, ItemStack item, Option option) {
		this.displayName = displayName;
		this.item = item;
		this.option = option;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public ItemStack getItem() {
		return item;
	}
	
	public Option getOption() {
		return option;
	}
	
	public static Options getByDisplayName(String displayName) {
		for (Options option : Options.values()) {
			if(displayName.contains(option.getDisplayName()))
				return option;
		}
		return null;
	}
	
}
