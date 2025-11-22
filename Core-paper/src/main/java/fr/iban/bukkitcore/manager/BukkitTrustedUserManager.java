package fr.iban.bukkitcore.manager;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.common.TrustedUser;
import fr.iban.common.manager.TrustedUserManager;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;

public class BukkitTrustedUserManager extends TrustedUserManager {

    private final CoreBukkitPlugin plugin;

    public BukkitTrustedUserManager(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
        loadTrustedUsers();
    }

    @Override
    public void loadTrustedUsers() {
        plugin.getScheduler().runAsync(task -> super.loadTrustedUsers());
    }

    public boolean isTrusted(Player player) {
        for (TrustedUser user : trustedUsers) {
            InetSocketAddress inetSocketAddress = player.getAddress();
            if(inetSocketAddress == null) continue;
            if(user.getUuid().equals(player.getUniqueId()) && player.getAddress().getHostString().equals(user.getIp())){
                return true;
            }
        }
        return false;
    }

}
