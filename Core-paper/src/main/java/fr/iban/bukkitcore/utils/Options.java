package fr.iban.bukkitcore.utils;

import org.bukkit.inventory.ItemStack;

import fr.iban.common.enums.Option;

public enum Options {
	
	DEATH("Afficher les messages de morts", new ItemStack(Head.DEATH.get()), Option.DEATH_MESSAGE),
	JOIN("Afficher les messages de connexions", new ItemStack(Head.PLUS.get()), Option.JOIN_MESSAGE),
	LEAVE("Afficher les messages de déconnexions", new ItemStack(Head.MOINS.get()), Option.LEAVE_MESSAGE),
	TP("Afficher les demandes de téléportations", new ItemStack(Head.ENDER_PEARL.get()), Option.TP),
	CHAT("Afficher les message du tchat", new ItemStack(Head.TCHAT.get()), Option.TCHAT),
	MENTION("Activer les mentions dans le tchat les message du tchat", new ItemStack(Head.AROBASE.get()), Option.MENTION),
	;
	
	
	private String displayName;
	private ItemStack item;
	private Option option;
	
	private Options(String displayName, ItemStack item, Option option) {
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
