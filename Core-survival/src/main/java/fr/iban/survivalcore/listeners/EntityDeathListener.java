package fr.iban.survivalcore.listeners;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.manager.BukkitPlayerManager;
import fr.iban.common.enums.Option;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.common.teleport.SLocation;
import fr.iban.survivalcore.SurvivalCorePlugin;
import fr.iban.survivalcore.lang.LangKey;
import fr.iban.survivalcore.lang.MessageBuilder;
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

        String message = MessageBuilder.translatable(LangKey.DEATH_PREFIX).toLegacy()
                + " " + player.getName() + " ";

        EntityDamageEvent damageCause = e.getEntity().getLastDamageCause();
        if (damageCause == null)
            return;

        switch (damageCause.getCause()) {
            case SUICIDE:
                message += MessageBuilder.translatable(LangKey.DEATH_SUICIDE).toLegacy();
                break;
            case BLOCK_EXPLOSION:
                message += MessageBuilder.translatable(LangKey.DEATH_BLOCK_EXPLOSION).toLegacy();
                break;
            case CONTACT:
                message += MessageBuilder.translatable(LangKey.DEATH_CONTACT).toLegacy();
                break;
            case DROWNING:
                message += MessageBuilder.translatable(LangKey.DEATH_DROWNING).toLegacy();
                break;
            case ENTITY_EXPLOSION:
                message += MessageBuilder.translatable(LangKey.DEATH_ENTITY_EXPLOSION).toLegacy();
                break;
            case FIRE_TICK:
                message += MessageBuilder.translatable(LangKey.DEATH_FIRE_TICK).toLegacy();
                break;
            case LAVA:
                message += MessageBuilder.translatable(LangKey.DEATH_LAVA).toLegacy();
                break;
            case MAGIC:
                message += MessageBuilder.translatable(LangKey.DEATH_MAGIC).toLegacy();
                break;
            case POISON:
                message += MessageBuilder.translatable(LangKey.DEATH_POISON).toLegacy();
                break;
            case PROJECTILE:
                if (killer != null)
                    message += MessageBuilder.translatable(LangKey.DEATH_PROJECTILE_KILLER)
                            .placeholder("killer", killer.getName())
                            .toLegacy();
                else
                    message += MessageBuilder.translatable(LangKey.DEATH_PROJECTILE).toLegacy();
                break;
            case STARVATION:
                message += MessageBuilder.translatable(LangKey.DEATH_STARVATION).toLegacy();
                break;
            case SUFFOCATION:
                message += MessageBuilder.translatable(LangKey.DEATH_SUFFOCATION).toLegacy();
                break;
            case VOID:
                message += MessageBuilder.translatable(LangKey.DEATH_VOID).toLegacy();
                break;
            case FALL:
                message += MessageBuilder.translatable(LangKey.DEATH_FALL).toLegacy();
                break;
            case WITHER:
                message += MessageBuilder.translatable(LangKey.DEATH_WITHER).toLegacy();
                break;
            case LIGHTNING:
                message += MessageBuilder.translatable(LangKey.DEATH_LIGHTNING).toLegacy();
                break;
            case HOT_FLOOR:
                message += MessageBuilder.translatable(LangKey.DEATH_HOT_FLOOR).toLegacy();
                break;
            case FLY_INTO_WALL:
                message += MessageBuilder.translatable(LangKey.DEATH_FLY_INTO_WALL).toLegacy();
                break;
            case FALLING_BLOCK:
                message += MessageBuilder.translatable(LangKey.DEATH_FALLING_BLOCK).toLegacy();
                break;
            case FIRE:
                message += MessageBuilder.translatable(LangKey.DEATH_FIRE).toLegacy();
                break;
            case DRAGON_BREATH:
                message += MessageBuilder.translatable(LangKey.DEATH_DRAGON_BREATH).toLegacy();
                break;
            case THORNS:
                if (damageCause instanceof EntityDamageByEntityEvent entity) {
                    if (entity.getDamager() instanceof Player) {
                        killer = (Player) entity.getDamager();
                        message += MessageBuilder.translatable(LangKey.DEATH_THORNS_PLAYER)
                                .placeholder("killer", killer.getName())
                                .toLegacy();
                    } else if (entity.getDamager() instanceof Mob) {
                        message += MessageBuilder.translatable(LangKey.DEATH_THORNS_MOB)
                                .placeholder("mob", entity.getDamager().getType().toString().toLowerCase().replace("_", " "))
                                .toLegacy();
                    } else if (entity.getDamager().getType() == EntityType.ARMOR_STAND) {
                        message += MessageBuilder.translatable(LangKey.DEATH_THORNS_ARMORSTAND).toLegacy();
                    }
                }
                break;
            case ENTITY_ATTACK:
                if (damageCause instanceof EntityDamageByEntityEvent entity) {
                    if (entity.getDamager() instanceof Player) {
                        killer = (Player) entity.getDamager();
                        Material weapon = killer.getInventory().getItemInMainHand().getType();
                        if (weapon == Material.AIR) {
                            message += MessageBuilder.translatable(LangKey.DEATH_ATTACK_PLAYER_AIR)
                                    .placeholder("killer", killer.getName())
                                    .toLegacy();
                        } else {
                            message += MessageBuilder.translatable(LangKey.DEATH_ATTACK_PLAYER_WEAPON)
                                    .placeholder("killer", killer.getName())
                                    .toLegacy();
                        }
                    } else if (entity.getDamager() instanceof Mob) {
                        if(entity.getDamager().getType() == EntityType.WARDEN) {
                            message += MessageBuilder.translatable(LangKey.DEATH_ATTACK_WARDEN).toLegacy();
                        } else {
                            message += MessageBuilder.translatable(LangKey.DEATH_ATTACK_MOB)
                                    .placeholder("mob", entity.getDamager().getType().toString().toLowerCase().replace("_", " "))
                                    .toLegacy();
                        }
                    }
                }
                break;
            default:
                message += MessageBuilder.translatable(LangKey.DEATH_DEFAULT).toLegacy();
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
                MessageBuilder.translatable(LangKey.DEATH_LOCATION)
                        .placeholder("server", core.getServerName())
                        .placeholder("world", location.getWorld().getName())
                        .placeholder("x", String.valueOf((int) location.getX()))
                        .placeholder("y", String.valueOf((int) location.getY()))
                        .placeholder("z", String.valueOf((int) location.getZ()))
                        .toLegacy()
        );

        MSPlayerProfile profile = core.getPlayerManager().getProfile(player.getUniqueId());
        SLocation rtpLocation = profile.getLastRTPLocation();

        if(rtpLocation != null && rtpLocation.getWorld().equals(location.getWorld().getName())) {
            String miniMessageText = MessageBuilder.translatable(LangKey.DEATH_LASTRTP).toRaw();
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
