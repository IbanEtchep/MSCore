package fr.iban.velocitycore.lang;

public enum LangKey implements Translatable {

    ANNOUNCE_DISABLED("announce.disabled"),
    ANNOUNCE_ALREADY_DISABLED("announce.already-disabled"),
    ANNOUNCE_NOT_EXIST("announce.not-exist"),

    ANNOUNCEEVENT_USAGE("announceevent.usage"),
    ANNOUNCEEVENT_PREFIX("announceevent.prefix"),

    CHAT_HELP_TITLE("chat.help.title"),
    CHAT_HELP_TOGGLE("chat.help.toggle"),
    CHAT_CLEAR("chat.clear"),

    VCORE_HELP("vcore.help"),
    VCORE_RELOAD_SUCCESS("vcore.reload-success"),
    VCORE_LANG_RELOAD_SUCCESS("vcore.lang-reload-success"),
    VCORE_DEBUG("vcore.debug"),
    VCORE_ENABLED("vcore.enabled"),
    VCORE_DISABLED("vcore.disabled"),

    IGNORE_HELP_PREFIX("ignore.help.prefix"),
    IGNORE_HELP_ADD("ignore.help.add"),
    IGNORE_HELP_ADD_SUFFIX("ignore.help.add_suffix"),
    IGNORE_HELP_REMOVE("ignore.help.remove"),
    IGNORE_HELP_REMOVE_SUFFIX("ignore.help.remove_suffix"),

    IGNORE_ADD_SUCCESS("ignore.add.success"),
    IGNORE_ADD_STAFF("ignore.add.staff"),
    IGNORE_ADD_ALREADY("ignore.add.already"),
    IGNORE_ADD_SELF("ignore.add.self"),

    IGNORE_REMOVE_SUCCESS("ignore.remove.success"),
    IGNORE_REMOVE_NOT_IGNORED("ignore.remove.not_ignored"),

    IGNORE_LIST_HEADER("ignore.list.header"),
    IGNORE_LIST_ENTRY("ignore.list.entry"),
    IGNORE_LIST_UNKNOWN("ignore.list.unknown"),
    IGNORE_LIST_EMPTY("ignore.list.empty"),

    JOINEVENT_NOT_FOUND("joinevent.not-found"),

    COMMANDS_MSG_FORMAT("commands.msg.format"),

    MISC_GD_OFFHAND("miscellaneous.geyser-offhand"),
    MISC_NOT_BEDROCK("miscellaneous.not-bedrock"),

    MSGTOGGLE_DISABLED("msgtoggle.disabled"),
    MSGTOGGLE_ENABLED("msgtoggle.enabled"),

    REPLY_USAGE("reply.usage"),
    REPLY_NO_TARGET("reply.no-target"),

    TABCOMPLETE_USAGE("tabcomplete.usage"),
    TABCOMPLETE_UNKNOWN_GROUP("tabcomplete.unknown-group"),
    TABCOMPLETE_ADDED("tabcomplete.added"),

    TELEPORT_BACK_NOT_FOUND("teleport.back.not-found"),
    TELEPORT_LASTRTP_NOT_FOUND("teleport.lastrtp.not-found"),

    TPTOGGLE_OPENED("tptoggle.opened"),
    TPTOGGLE_CLOSED("tptoggle.closed"),

    EVENT_STARTED("event.started"),
    EVENT_ARENA("event.arena"),
    EVENT_CLICK_TO_JOIN("event.click-to-join"),

    PROXY_PREFIX_JOIN("proxy.prefix.join"),
    PROXY_PREFIX_QUIT("proxy.prefix.quit"),
    PROXY_PREFIX_FIRST_JOIN("proxy.prefix.first-join"),

    PROXY_HOVER_LAST_SEEN("proxy.hover.last-seen"),
    PROXY_HOVER_FIRST_JOIN("proxy.hover.first-join"),

    PROXY_CLICK_FIRST_JOIN("proxy.click.first-join"),

    PROXY_LAST_SEEN_NEVER("proxy.last-seen.never"),

    PROXY_KICK_KEYWORD("proxy.kick.keyword"),

    PROXY_JOIN_MESSAGE_1("proxy.join-messages.1"),
    PROXY_JOIN_MESSAGE_2("proxy.join-messages.2"),
    PROXY_JOIN_MESSAGE_3("proxy.join-messages.3"),
    PROXY_JOIN_MESSAGE_4("proxy.join-messages.4"),
    PROXY_JOIN_MESSAGE_5("proxy.join-messages.5"),
    PROXY_JOIN_MESSAGE_6("proxy.join-messages.6"),

    PROXY_QUIT_MESSAGE_1("proxy.quit-messages.1"),
    PROXY_QUIT_MESSAGE_2("proxy.quit-messages.2"),
    PROXY_QUIT_MESSAGE_3("proxy.quit-messages.3"),
    PROXY_QUIT_MESSAGE_4("proxy.quit-messages.4"),

    PROXY_LONG_ABSENCE_1("proxy.long-absence.1"),
    PROXY_LONG_ABSENCE_2("proxy.long-absence.2"),
    PROXY_LONG_ABSENCE_3("proxy.long-absence.3"),
    PROXY_LONG_ABSENCE_4("proxy.long-absence.4"),
    PROXY_LONG_ABSENCE_5("proxy.long-absence.5"),

    PROXY_FIRST_JOIN_1("proxy.first-join.1"),
    PROXY_FIRST_JOIN_2("proxy.first-join.2"),
    PROXY_FIRST_JOIN_3("proxy.first-join.3"),
    PROXY_FIRST_JOIN_4("proxy.first-join.4"),
    PROXY_FIRST_JOIN_5("proxy.first-join.5"),

    ANNOUNCE_PREFIX_LEFT("announce.prefix-left"),
    ANNOUNCE_CLOSE_SYMBOL("announce.close-symbol"),
    ANNOUNCE_PREFIX_RIGHT("announce.prefix-right"),
    ANNOUNCE_HOVER_DISABLE("announce.hover-disable"),
    ANNOUNCE_NO_ANNOUNCES("announce.no-announces"),
    ANNOUNCE_INVALID_ID("announce.invalid-id"),

    CHAT_DISABLED("chat.disabled"),
    CHAT_ANNONCE("chat.annonce"),
    CHAT_MUTED("chat.muted"),
    CHAT_UNMUTED("chat.unmuted"),

    MSG_DISABLED("msg.disabled"),
    MSG_IGNORED("msg.ignored"),
    MSG_HOVER("msg.hover"),
    MSG_SENDER("msg.sender"),
    MSG_SENDER_STAFF("msg.sender-staff"),
    MSG_TARGET("msg.target"),
    MSG_TARGET_STAFF("msg.target-staff"),

    STAFFCHAT_ENABLED("staffchat.enabled"),
    STAFFCHAT_DISABLED("staffchat.disabled"),

    TP_DELAYED("tp.delayed"),
    TP_ALREADY_WAITING("tp.already-waiting"),

    TP_REQUEST_SENT("tp.request.sent"),
    TP_REQUEST_EXPIRED("tp.request.expired"),
    TP_REQUEST_TO_PLAYER("tp.request.to-player"),
    TP_REQUEST_HERE("tp.request.here");

    private final String key;

    LangKey(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}
