package fr.iban.survivalcore.lang;

public enum LangKey implements Translatable {

    DOLPHIN_ENABLED("dolphin.enabled"),
    DOLPHIN_DISABLED("dolphin.disabled"),

    FEED_FED("feed.fed"),
    FEED_COOLDOWN("feed.cooldown"),

    PVP_ENABLED("pvp.enabled"),
    PVP_DISABLED("pvp.disabled"),
    PVP_ENABLED_OTHER("pvp.enabled-other"),
    PVP_DISABLED_OTHER("pvp.disabled-other"),

    REPAIR_DONE_HAND("repair.done-hand"),
    REPAIR_DONE_ALL("repair.done-all"),
    REPAIR_NOT_REPAIRABLE("repair.not-repairable"),
    REPAIR_COOLDOWN_HAND("repair.cooldown-hand"),
    LEGENDARY_TAG("repair.legendary-tag"),
    REPAIR_COOLDOWN_ALL("repair.cooldown-all"),

    CORE_RELOAD_SUCCESS("core.reload-success"),
    CORE_LANG_RELOAD_SUCCESS("core.lang-reload-success"),

    DEATH_PREFIX("death.prefix"),
    DEATH_SUICIDE("death.suicide"),
    DEATH_BLOCK_EXPLOSION("death.block-explosion"),
    DEATH_CONTACT("death.contact"),
    DEATH_DROWNING("death.drowning"),
    DEATH_ENTITY_EXPLOSION("death.entity-explosion"),
    DEATH_FIRE_TICK("death.fire-tick"),
    DEATH_LAVA("death.lava"),
    DEATH_MAGIC("death.magic"),
    DEATH_POISON("death.poison"),
    DEATH_PROJECTILE_KILLER("death.projectile-killer"),
    DEATH_PROJECTILE("death.projectile"),
    DEATH_STARVATION("death.starvation"),
    DEATH_SUFFOCATION("death.suffocation"),
    DEATH_VOID("death.void"),
    DEATH_FALL("death.fall"),
    DEATH_WITHER("death.wither"),
    DEATH_LIGHTNING("death.lightning"),
    DEATH_HOT_FLOOR("death.hot-floor"),
    DEATH_FLY_INTO_WALL("death.fly-into-wall"),
    DEATH_FALLING_BLOCK("death.falling-block"),
    DEATH_FIRE("death.fire"),
    DEATH_DRAGON_BREATH("death.dragon-breath"),
    DEATH_THORNS_PLAYER("death.thorns-player"),
    DEATH_THORNS_MOB("death.thorns-mob"),
    DEATH_THORNS_ARMORSTAND("death.thorns-armorstand"),
    DEATH_ATTACK_PLAYER_AIR("death.attack-player-air"),
    DEATH_ATTACK_PLAYER_WEAPON("death.attack-player-weapon"),
    DEATH_ATTACK_WARDEN("death.attack-warden"),
    DEATH_ATTACK_MOB("death.attack-mob"),
    DEATH_DEFAULT("death.default"),
    DEATH_LOCATION("death.location"),
    DEATH_LASTRTP("death.lastrtp"),

    RAIDS_DISABLED("raids.disabled"),

    RTP_TELEPORTED("rtp.teleported"),

    ANNOUNCE_NOT_ENOUGH_MONEY("announce.not-enough-money"),
    ANNOUNCE_COOLDOWN("announce.cooldown"),

    TOOLS_PICKAXE3X3_NAME("tools.pickaxe3x3.name"),
    TOOLS_PICKAXE3X3_LORE("tools.pickaxe3x3.lore"),
    TOOLS_PICKAXE3X3_CHECK("tools.pickaxe3x3.check"),

    TOOLS_CUTCLEAN_NAME("tools.cutclean.name"),
    TOOLS_CUTCLEAN_LORE("tools.cutclean.lore"),
    TOOLS_CUTCLEAN_CHECK("tools.cutclean.check"),

    TOOLS_SHOVEL3X3_NAME("tools.shovel3x3.name"),
    TOOLS_SHOVEL3X3_LORE("tools.shovel3x3.lore"),
    TOOLS_SHOVEL3X3_CHECK("tools.shovel3x3.check"),

    TOOLS_LUMBERJACK_NAME("tools.lumberjack.name"),
    TOOLS_LUMBERJACK_LORE("tools.lumberjack.lore"),
    TOOLS_LUMBERJACK_CHECK("tools.lumberjack.check"),

    TOOLS_HOE_NAME("tools.hoe.name"),
    TOOLS_HOE_LORE("tools.hoe.lore"),
    TOOLS_HOE_CHECK("tools.hoe.check"),

    TOOLS_HOE3X3_NAME("tools.hoe3x3.name"),
    TOOLS_HOE3X3_LORE("tools.hoe3x3.lore"),
    TOOLS_HOE3X3_CHECK("tools.hoe3x3.check"),

    TOOLS_XPSWORD_NAME("tools.xpsword.name"),
    TOOLS_XPSWORD_LORE("tools.xpsword.lore"),
    TOOLS_XPSWORD_CHECK("tools.xpsword.check"),

    HOURLY_REWARD("hourlyreward.received-money"),
    HOURLY_PENDING("hourlyreward.pending-reward");

    private final String key;

    LangKey(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}
