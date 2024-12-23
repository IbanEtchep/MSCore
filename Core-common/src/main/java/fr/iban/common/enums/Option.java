package fr.iban.common.enums;

public enum Option {

	PVP(false),
	JOIN_MESSAGE(true),
	LEAVE_MESSAGE(true),
	DEATH_MESSAGE(true),
	TP(true),
	CHAT(true),
	MENTION(true),
	MSG(true);
	
	private final boolean defaultValue;
	
	Option(boolean defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean getDefaultValue() {
		return defaultValue;
	}

}
