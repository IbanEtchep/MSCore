package fr.iban.network.mscore;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.event.CoreMessageEvent;
import fr.iban.common.messaging.Message;
import fr.iban.network.NetworkMessage;
import fr.iban.network.NetworkMessenger;
import fr.iban.network.Subscription;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;

class MSCoreMessenger implements NetworkMessenger, Listener {

    private final Plugin plugin;
    private final CoreBukkitPlugin core;
    private final Map<String, List<Consumer<NetworkMessage>>> handlers = new ConcurrentHashMap<>();
    private final AtomicBoolean listening = new AtomicBoolean();

    MSCoreMessenger(Plugin plugin, CoreBukkitPlugin core) {
        this.plugin = plugin;
        this.core = core;
    }

    @Override
    public void publish(String channel, String payload) {
        core.getMessagingManager().sendMessage(channel, payload);
    }

    @Override
    public void publish(String channel, Object payload) {
        core.getMessagingManager().sendMessage(channel, payload);
    }

    @Override
    public Subscription subscribe(String channel, Consumer<NetworkMessage> handler) {
        startListening();
        handlers.computeIfAbsent(channel, key -> new CopyOnWriteArrayList<>()).add(handler);
        return () -> {
            List<Consumer<NetworkMessage>> channelHandlers = handlers.get(channel);
            if (channelHandlers != null) {
                channelHandlers.remove(handler);
            }
        };
    }

    void close() {
        handlers.clear();
        if (listening.compareAndSet(true, false)) {
            HandlerList.unregisterAll(this);
        }
    }

    private void startListening() {
        if (listening.compareAndSet(false, true)) {
            plugin.getServer().getPluginManager().registerEvent(CoreMessageEvent.class, this, EventPriority.NORMAL,
                    (listener, event) -> {
                        if (event instanceof CoreMessageEvent coreMessageEvent) {
                            dispatch(coreMessageEvent.getMessage());
                        }
                    }, plugin);
        }
    }

    private void dispatch(Message message) {
        List<Consumer<NetworkMessage>> channelHandlers = handlers.get(message.getChannel());
        if (channelHandlers == null) {
            return;
        }

        NetworkMessage networkMessage = new NetworkMessage(message.getChannel(), message.getServerFrom(), message.getMessage());
        for (Consumer<NetworkMessage> handler : channelHandlers) {
            try {
                handler.accept(networkMessage);
            } catch (RuntimeException e) {
                plugin.getLogger().log(Level.SEVERE, "Error handling network message on channel " + message.getChannel(), e);
            }
        }
    }
}
