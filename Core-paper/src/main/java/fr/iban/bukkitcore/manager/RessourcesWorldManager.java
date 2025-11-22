package fr.iban.bukkitcore.manager;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.lang.LangKey;
import fr.iban.bukkitcore.lang.MessageBuilder;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RessourcesWorldManager {

    private final CoreBukkitPlugin plugin;
    private final Map<UUID, Long> lastTeleportTime = new HashMap<>();

    public RessourcesWorldManager(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    public void randomTpResourceWorld(Player player, String worldname) {
        int secondsLeft = getSecondsLeft(player.getUniqueId());

        if(secondsLeft > 0){
            player.sendMessage(
                    MessageBuilder.translatable(LangKey.RESSOURCES_TP_WAIT)
                            .placeholder("seconds", String.valueOf(secondsLeft))
                            .toLegacy()
            );
            return;
        }

        lastTeleportTime.put(player.getUniqueId(), System.currentTimeMillis());

        player.sendMessage(
                MessageBuilder.translatable(LangKey.RESSOURCES_TP_START).toLegacy()
        );

        String ressourcesServer = plugin.getConfig().getString("ressources.servername", "ressources");
        plugin.getTeleportManager().randomTeleport(player, ressourcesServer, worldname);
    }

    private int getSecondsLeft(UUID uuid){
        if(lastTeleportTime.containsKey(uuid)){
            long lastTp = lastTeleportTime.get(uuid);
            if(System.currentTimeMillis() - lastTp < 60000){
                return (int) ((60000 - (System.currentTimeMillis() - lastTp))/1000);
            }else{
                lastTeleportTime.remove(uuid);
            }
        }
        return 0;
    }

    public String getResourceServerName() {
        return plugin.getConfig().getString("ressources.servername", "ressources");
    }
}
