package fr.iban.bukkitcore.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.common.teleport.SLocation;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class SLocationUtils {
	
	public static SLocation getSLocation(Location loc) {
		return new SLocation(CoreBukkitPlugin.getInstance().getServerName(), loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getPitch(), loc.getYaw());
	}

	public static SLocation getSLocation(Location loc, String serverName) {
		return new SLocation(serverName, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getPitch(), loc.getYaw());
	}

	public static Location getLocation(SLocation sloc) {
		if(sloc == null){
			return null;
		}
		return new Location(Bukkit.getWorld(sloc.getWorld()), sloc.getX(), sloc.getY(), sloc.getZ(), sloc.getYaw(), sloc.getPitch());
	}

	/**
	 * Checks if a location is safe (solid ground with 2 breathable blocks)
	 *
	 * @param location Location to check
	 * @return True if location is safe
	 */
	public static boolean isSafeLocation(Location location) {
		Block feet = location.getBlock();
		if (feet.getType().isSolid() || !feet.getLocation().add(0, 1, 0).getBlock().getType().isSolid()) {
			if (feet.getType().isSolid()) {
				return false;
			}
		}
		if (feet.getLocation().add(0, 1, 0).getBlock().getType().isSolid()) {
			return false;
		}

		Block head = feet.getRelative(BlockFace.UP);
		if (head.getType().isSolid()) {
			return false;
		}

		for (int i = 1; i < 5; i++) {
			Material relativeMaterial = feet.getRelative(BlockFace.DOWN, i).getType();
			if(relativeMaterial == Material.LAVA) {
				return false;
			}
			if(relativeMaterial.isSolid() || relativeMaterial == Material.WATER) {
				return true;
			}
		}
		return false;
	}
}
