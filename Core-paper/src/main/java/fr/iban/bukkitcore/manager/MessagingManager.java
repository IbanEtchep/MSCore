package fr.iban.bukkitcore.manager;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.event.CoreMessageEvent;
import fr.iban.common.data.redis.RedisCredentials;
import fr.iban.common.messaging.*;
import org.bukkit.configuration.file.FileConfiguration;

public class MessagingManager extends AbstractMessagingManager {

    private final CoreBukkitPlugin plugin;

    public MessagingManager(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        FileConfiguration config = plugin.getConfig();
        if (config.getString("messenger", "sql").equals("redis")) {
            RedisCredentials credentials = new RedisCredentials(
                    config.getString("redis.host"),
                    config.getString("redis.password"),
                    config.getInt("redis.port"),
                    config.getString("redis.clientName")
            );
            messenger = new RedisMessenger(credentials);
        } else {
            messenger = new SqlMessenger();
        }

        messenger.setOnMessageListener(message -> {
            if (!message.getServerFrom().equals(plugin.getServerName())) {
                plugin.getServer().getPluginManager().callEvent(new CoreMessageEvent(message));
            }
        });

        messenger.init();
    }

    @Override
    public void close() {
        messenger.close();
    }

    @Override
    protected String getServerName() {
        return plugin.getServerName();
    }
}
