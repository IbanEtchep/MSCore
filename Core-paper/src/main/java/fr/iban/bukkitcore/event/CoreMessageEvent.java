package fr.iban.bukkitcore.event;

import fr.iban.common.messaging.Message;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CoreMessageEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Message message;

    public CoreMessageEvent(Message message) {
        super(true);
        this.message = message;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public Message getMessage() {
        return message;
    }

}
