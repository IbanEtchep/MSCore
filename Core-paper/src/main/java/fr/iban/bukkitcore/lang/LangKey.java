package fr.iban.bukkitcore.lang;

public enum LangKey implements Translatable {

    CORE_RELOAD_SUCCESS("core.reload-success"),
    CORE_LANG_RELOAD_SUCCESS("core.lang-reload-success"),
    CORE_ENABLED("core.enabled"),
    CORE_DISABLED("core.disabled"),
    CORE_DEBUG("core.debug"),

    RECOMPENSES_NO_PENDING("recompenses.no-pending"),
    RECOMPENSES_RECEIVED("recompenses.received"),
    RECOMPENSES_MUST_BE_INT("recompenses.must-be-int"),
    RECOMPENSES_PLAYER_NOT_FOUND("recompenses.player-not-found"),
    RECOMPENSES_NO_TEMPLATES("recompenses.no-templates"),
    RECOMPENSES_TEMPLATE_ADDED("recompenses.template-added"),

    RECOMPENSES_USAGE_GIVE("recompenses.usage-give"),
    RECOMPENSES_USAGE_REMOVETEMPLATE("recompenses.usage-removetemplate"),
    RECOMPENSES_USAGE_ADDTEMPLATE("recompenses.usage-addtemplate"),

    RECOMPENSES_TEMPLATE_ENTRY("recompenses.template-entry"),

    RESSOURCES_INVALID_WORLD("ressources.invalid-world"),
    RESSOURCES_TP_WAIT("ressources.tp-wait"),
    RESSOURCES_TP_START("ressources.tp-start"),

    TELEPORT_CANNOT_SELF("teleport.cannot-self"),
    TELEPORT_REQUEST_ACCEPTED("teleport.request-accepted"),
    TELEPORT_REQUEST_DENIED("teleport.request-denied"),
    TELEPORT_NO_REQUEST("teleport.no-request"),
    TELEPORT_NO_REQUEST_FROM_PLAYER("teleport.no-request-from-player"),
    TELEPORT_CANCELLED_MOVE("teleport.cancelled-move"),
    TELEPORT_NO_WHILE_FALLING("teleport.no-while-falling"),
    TELEPORT_DELAY("teleport.delay"),
    TELEPORT_LOADING("teleport.loading"),
    TELEPORT_SUCCESS("teleport.success"),
    TELEPORT_FAILED("teleport.failed"),
    TELEPORT_UNSAFE_WARNING("teleport.unsafe-warning"),
    TELEPORT_UNSAFE_HOVER("teleport.unsafe-hover"),
    TELEPORT_NO_UNSAFE("teleport.no-unsafe"),
    TELEPORT_ALREADY_SURVIVAL("teleport.already-survival"),

    TRUSTCOMMAND_ALREADY_ADDED("trustcommand.already-added"),
    TRUSTCOMMAND_ADDED("trustcommand.added"),
    TRUSTCOMMAND_NOT_FOUND("trustcommand.not-found"),
    TRUSTCOMMAND_REMOVED("trustcommand.removed"),
    TRUSTCOMMAND_RELOADED("trustcommand.reloaded"),

    COMMANDS_APPROVAL_REQUIRED("commands.approval-required"),
    COMMANDS_APPROVAL_MESSAGE("commands.approval-message"),

    JOIN_REWARDS_PENDING("join.rewards-pending"),

    MENUS_CONFIRM_TITLE("menus.confirm.title"),
    MENUS_CONFIRM_DESC_DEFAULT("menus.confirm.desc-default"),
    MENUS_CONFIRM_BUTTON("menus.confirm.confirm-button"),
    MENUS_CANCEL_BUTTON("menus.confirm.cancel-button"),

    MENUS_OPTIONS_TITLE("menus.options.title"),

    MENUS_PAGINATED_NEXT("menus.paginated.next"),
    MENUS_PAGINATED_PREVIOUS("menus.paginated.previous"),
    MENUS_PAGINATED_CLOSE("menus.paginated.close"),

    MENUS_RESSOURCES_TITLE("menus.ressources.title"),
    MENUS_RESSOURCES_NORMAL_NAME("menus.ressources.normal.name"),
    MENUS_RESSOURCES_NORMAL_LORE("menus.ressources.normal.lore"),
    MENUS_RESSOURCES_NETHER_NAME("menus.ressources.nether.name"),
    MENUS_RESSOURCES_NETHER_LORE("menus.ressources.nether.lore"),
    MENUS_RESSOURCES_END_NAME("menus.ressources.end.name"),
    MENUS_RESSOURCES_END_LORE("menus.ressources.end.lore"),

    MENUS_REWARD_SELECT_TITLE("menus.reward-select.title"),
    MENUS_REWARD_SELECT_ITEM_NAME("menus.reward-select.item-name"),
    MENUS_REWARD_SELECT_ITEM_LORE("menus.reward-select.item-lore"),

    MENUS_SERVEUR_TITLE("menus.serveur.title"),
    MENUS_SERVEUR_SURVIE_NAME("menus.serveur.survie-name"),
    MENUS_SERVEUR_SURVIE_LORE("menus.serveur.survie-lore"),
    MENUS_SERVEUR_RESSOURCES_NAME("menus.serveur.ressources-name"),
    MENUS_SERVEUR_RESSOURCES_LORE("menus.serveur.ressources-lore"),

    OPTIONS_DEATH("options.death"),
    OPTIONS_JOIN("options.join"),
    OPTIONS_LEAVE("options.leave"),
    OPTIONS_TP("options.tp"),
    OPTIONS_CHAT("options.chat"),
    OPTIONS_MENTION("options.mention");

    private final String key;

    LangKey(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}
