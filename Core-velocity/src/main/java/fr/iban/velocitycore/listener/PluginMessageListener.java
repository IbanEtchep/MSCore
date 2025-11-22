package fr.iban.velocitycore.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import fr.iban.velocitycore.CoreVelocityPlugin;

import java.util.UUID;

public class PluginMessageListener {

    private final CoreVelocityPlugin plugin;

    public PluginMessageListener(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!(event.getTarget() instanceof Player)) {
            return;
        }

        String channel = event.getIdentifier().getId();


        byte[] data = event.getData();

        if ("proxy:chat".equals(channel)) {
            handleChatMessage(data);
        } else if ("proxy:annonce".equals(channel)) {
            handleAnnonceMessage(data);
        }
    }

    private void handleChatMessage(byte[] data) {
        ByteArrayDataInput in = ByteStreams.newDataInput(data);
        String sub = in.readUTF();
        if ("Global".equals(sub)) {
            UUID uuid = UUID.fromString(in.readUTF());
            String message = in.readUTF();
            plugin.getChatManager().sendGlobalMessage(uuid, message);
        }
    }

    private void handleAnnonceMessage(byte[] data) {
        ByteArrayDataInput in = ByteStreams.newDataInput(data);
        String sub = in.readUTF();
        if ("Annonce".equals(sub)) {
            UUID uuid = UUID.fromString(in.readUTF());
            String message = in.readUTF();
            plugin.getChatManager().sendAnnonce(uuid, message);
        }
    }
}
