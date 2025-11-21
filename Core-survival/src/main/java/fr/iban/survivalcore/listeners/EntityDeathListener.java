package fr.iban.survivalcore.listeners;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.manager.BukkitPlayerManager;
import fr.iban.bukkitcore.utils.Lang;
import fr.iban.common.enums.Option;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.common.teleport.SLocation;
import fr.iban.survivalcore.SurvivalCorePlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class EntityDeathListener implements Listener {

    private final SurvivalCorePlugin plugin;

    public EntityDeathListener(SurvivalCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        e.deathMessage(null);
        Player killer = e.getEntity().getKiller();
        Player player = e.getEntity();
        String message = Lang.get("death.prefix") + " " + player.getName() + " ";

        EntityDamageEvent damageCause = e.getEntity().getLastDamageCause();
        if (damageCause == null)
            return;

        switch (damageCause.getCause()) {
            case SUICIDE:
                message += Lang.get("death.suicide");
                break;
            case BLOCK_EXPLOSION:
                message += Lang.get("death.block-explosion");
                break;
            case CONTACT:
                message += Lang.get("death.contact");
                break;
            case DROWNING:
                message += Lang.get("death.drowning");
                break;
            case ENTITY_EXPLOSION:
                message += Lang.get("death.entity-explosion");
                break;
            case FIRE_TICK:
                message += Lang.get("death.fire-tick");
                break;
            case LAVA:
                message += Lang.get("death.lava");
                break;
            case MAGIC:
                message += Lang.get("death.magic");
                break;
            case POISON:
                message += Lang.get("death.poison");
                break;
            case PROJECTILE:
                if (killer != null)
                    message += Lang.get("death.projectile-killer").replace("%killer%", killer.getName());
                else
                    message += Lang.get("death.projectile");
                break;
            case STARVATION:
                message += Lang.get("death.starvation");
                break;
            case SUFFOCATION:
                message += Lang.get("death.suffocation");
                break;
            case VOID:
                message += Lang.get("death.void");
                break;
            case FALL:
                message += Lang.get("death.fall");
                break;
            case WITHER:
                message += Lang.get("death.wither");
                break;
            case LIGHTNING:
                message += Lang.get("death.lightning");
                break;
            case HOT_FLOOR:
                message += Lang.get("death.hot-floor");
                break;
            case FLY_INTO_WALL:
                message += Lang.get("death.fly-into-wall");
                break;
            case FALLING_BLOCK:
                message += Lang.get("death.falling-block");
                break;
            case FIRE:
                message += Lang.get("death.fire");
                break;
            case DRAGON_BREATH:
                message += Lang.get("death.dragon-breath");
                break;
            case THORNS:
                if (damageCause instanceof EntityDamageByEntityEvent entity) {
                    if (entity.getDamager() instanceof Player) {
                        killer = (Player) entity.getDamager();
                        message += Lang.get("death.thorns-player").replace("%killer%", killer.getName());
                    } else if (entity.getDamager() instanceof Mob) {
                        message += Lang.get("death.thorns-mob").replace("%mob%", entity.getDamager().getType().toString().toLowerCase().replace("_", " "));
                    } else if (entity.getDamager().getType() == EntityType.ARMOR_STAND) {
                        message += Lang.get("death.thorns-armorstand");
                    }
                }
                break;
            case ENTITY_ATTACK:
                if (damageCause instanceof EntityDamageByEntityEvent entity) {
                    if (entity.getDamager() instanceof Player) {
                        killer = (Player) entity.getDamager();
                        Material weapon = killer.getInventory().getItemInMainHand().getType();
                        if (weapon == Material.AIR) {
                            message += Lang.get("death.attack-player-air").replace("%killer%", killer.getName());
                        } else {
                            message += Lang.get("death.attack-player-weapon").replace("%killer%", killer.getName());
                        }
                    } else if (entity.getDamager() instanceof Mob) {
                        if(entity.getDamager().getType() == EntityType.WARDEN) {
                            message += Lang.get("death.attack-warden");
                        }else{
                            message += Lang.get("death.attack-mob").replace("%mob%", entity.getDamager().getType().toString().toLowerCase().replace("_", " "));
                        }
                    }
                }
                break;
            default:
                message += Lang.get("death.default");
        }

        BukkitPlayerManager playerManager = CoreBukkitPlugin.getInstance().getPlayerManager();
        for (MSPlayerProfile msPlayer : playerManager.getProfiles()) {
            if (msPlayer.getOption(Option.DEATH_MESSAGE) && !msPlayer.getIgnoredPlayers().contains(player.getUniqueId())) {
                playerManager.sendMessage(msPlayer, Component.text(message));
            }
        }

        Bukkit.getConsoleSender().sendMessage(message);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        CoreBukkitPlugin core = CoreBukkitPlugin.getInstance();
        Player player = e.getEntity();
        Location location = player.getLocation();

        player.sendMessage(
                Lang.get("death.location")
                        .replace("%server%", core.getServerName())
                        .replace("%world%", location.getWorld().getName())
                        .replace("%x%", String.valueOf((int) location.getX()))
                        .replace("%y%", String.valueOf((int) location.getY()))
                        .replace("%z%", String.valueOf((int) location.getZ()))
        );

        MSPlayerProfile profile = core.getPlayerManager().getProfile(player.getUniqueId());
        SLocation rtpLocation = profile.getLastRTPLocation();

        if(rtpLocation != null && rtpLocation.getWorld().equals(location.getWorld().getName())) {
            String miniMessageText = Lang.get("death.lastrtp");
            Component message = MiniMessage.miniMessage().deserialize(miniMessageText);
            player.sendMessage(message);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        boolean disableEnemyDrops = plugin.getConfig().getBoolean("loots.disable-enemy-drops", false);
        Entity entity = event.getEntity();

        if(disableEnemyDrops && entity instanceof Enemy) {
            event.getDrops().clear();
        }
    }
}
