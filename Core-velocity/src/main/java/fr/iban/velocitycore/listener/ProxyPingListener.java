package fr.iban.velocitycore.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.ServerPing;
import de.themoep.minedown.adventure.MineDown;
import fr.iban.velocitycore.CoreVelocityPlugin;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Random;

public class ProxyPingListener {

    private final CoreVelocityPlugin plugin;
    private final Random random = new Random();

    public ProxyPingListener(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onPing(ProxyPingEvent e) {
        ServerPing serverPing = e.getPing().asBuilder().build();
        List<String> motd = plugin.getConfig().getStringList("motd");
        Component randomMotd = MineDown.parse(motd.get(random.nextInt(motd.size())));

        serverPing = serverPing.asBuilder()
                .maximumPlayers(100)
                .description(randomMotd)
                .build();

        e.setPing(serverPing);
    }
}
