package fr.iban.survivalcore.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.iban.bukkitcore.utils.Lang;

public class SpecialTools {

    public static final Set<Material> canBreakWith3x3Pickaxe = EnumSet.of(
            Material.COBBLESTONE, Material.STONE, Material.DIRT, Material.NETHERRACK, Material.GRASS_BLOCK, Material.DIORITE,
            Material.ANDESITE, Material.GRANITE, Material.IRON_ORE, Material.COAL_ORE, Material.GOLD_ORE, Material.LAPIS_ORE,
            Material.REDSTONE_ORE, Material.GRAVEL, Material.BLACKSTONE, Material.END_STONE,
            Material.BASALT, Material.MAGMA_BLOCK, Material.DEEPSLATE, Material.DEEPSLATE_COAL_ORE, Material.COBBLED_DEEPSLATE,
            Material.DEEPSLATE_COPPER_ORE, Material.DEEPSLATE_GOLD_ORE, Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.DEEPSLATE_REDSTONE_ORE, Material.COPPER_ORE, Material.TUFF, Material.CRIMSON_NYLIUM, Material.WARPED_NYLIUM
    );

    public static final Set<Material> canBreakWith3x3Shovel = EnumSet.of(
            Material.GRASS_BLOCK, Material.DIRT, Material.SAND, Material.RED_SAND, Material.GRAVEL, Material.SOUL_SAND, Material.SOUL_SOIL, Material.CLAY
    );

    public static Map<UUID, BlockFace> faces = new HashMap<>();

    public static ItemStack get3x3Pickaxe() {
        ItemStack hades = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemMeta cus = hades.getItemMeta();
        cus.setDisplayName(Lang.get("tools.pickaxe3x3.name"));
        cus.setLore(Lang.getList("tools.pickaxe3x3.lore"));
        hades.setItemMeta(cus);
        return hades;
    }

    public static ItemStack getCutCleanPickaxe() {
        ItemStack hades = new ItemStack(Material.NETHERITE_PICKAXE);
        ItemMeta cus = hades.getItemMeta();
        cus.setDisplayName(Lang.get("tools.cutclean.name"));
        cus.setLore(Lang.getList("tools.cutclean.lore"));
        hades.setItemMeta(cus);
        return hades;
    }

    public static ItemStack get3x3Shovel() {
        ItemStack hades = new ItemStack(Material.NETHERITE_SHOVEL);
        ItemMeta cus = hades.getItemMeta();
        cus.setDisplayName(Lang.get("tools.shovel3x3.name"));
        cus.setLore(Lang.getList("tools.shovel3x3.lore"));
        hades.setItemMeta(cus);
        return hades;
    }

    public static ItemStack getLumberjackAxe() {
        ItemStack hades = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta cus = hades.getItemMeta();
        cus.setDisplayName(Lang.get("tools.lumberjack.name"));
        cus.setLore(Lang.getList("tools.lumberjack.lore"));
        hades.setItemMeta(cus);
        return hades;
    }

    public static ItemStack getFarmerHoe() {
        ItemStack hades = new ItemStack(Material.NETHERITE_HOE);
        ItemMeta cus = hades.getItemMeta();
        cus.setDisplayName(Lang.get("tools.hoe.name"));
        cus.setLore(Lang.getList("tools.hoe.lore"));
        hades.setItemMeta(cus);
        return hades;
    }

    public static ItemStack get3x3FarmerHoe() {
        ItemStack hades = new ItemStack(Material.NETHERITE_HOE);
        ItemMeta cus = hades.getItemMeta();
        cus.setDisplayName(Lang.get("tools.hoe3x3.name"));
        cus.setLore(Lang.getList("tools.hoe3x3.lore"));
        hades.setItemMeta(cus);
        return hades;
    }

    public static ItemStack getXpSword() {
        ItemStack xpsword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta cus = xpsword.getItemMeta();
        cus.setDisplayName(Lang.get("tools.xpsword.name"));
        cus.setLore(Lang.getList("tools.xpsword.lore"));
        xpsword.setItemMeta(cus);
        return xpsword;
    }

    public static boolean isLumberjackAxe(ItemStack item) {
        return item.getType() == Material.NETHERITE_AXE && item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().contains(Lang.get("tools.lumberjack.check"));
    }

    public static boolean is3x3Shovel(ItemStack item) {
        return item.getType() == Material.NETHERITE_SHOVEL && item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().contains(Lang.get("tools.shovel3x3.check"));
    }

    public static boolean isCutCleanPickaxe(ItemStack item) {
        return item.getType() == Material.NETHERITE_PICKAXE && item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().contains(Lang.get("tools.cutclean.check"));
    }

    public static boolean isReplantHoue(ItemStack item) {
        return item.getType() == Material.NETHERITE_HOE && item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().contains(Lang.get("tools.hoe.check"));
    }

    public static boolean is3x3ReplantHoue(ItemStack item) {
        return isReplantHoue(item) && item.getItemMeta().getLore().contains(Lang.get("tools.hoe3x3.check"));
    }

    public static boolean is3x3Pickaxe(ItemStack item) {
        return item.getType() == Material.NETHERITE_PICKAXE && item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().contains(Lang.get("tools.pickaxe3x3.check"));
    }

    public static boolean isXpBoostSword(ItemStack item) {
        return item.getType() == Material.NETHERITE_SWORD && item.hasItemMeta() && item.getItemMeta().hasLore() && item.getItemMeta().getLore().contains(Lang.get("tools.xpsword.check"));
    }

    public static List<Block> getSurroundingBlocksShovel(Player player, Block targetBlock) {
        return getSurroundingBlocks(faces.get(player.getUniqueId()), targetBlock).stream().filter(block -> canBreakWith3x3Shovel.contains(block.getType())).collect(Collectors.toList());
    }

    public static List<Block> getSurroundingBlocksPickaxe(Player player, Block targetBlock) {
        return getSurroundingBlocks(faces.get(player.getUniqueId()), targetBlock).stream().filter(block -> canBreakWith3x3Pickaxe.contains(block.getType())).collect(Collectors.toList());
    }

    public static List<Block> getSurroundingBlocks(BlockFace blockFace, Block targetBlock) {
        ArrayList<Block> blocks = new ArrayList<>();
        World world = targetBlock.getWorld();

        int x, y, z;
        x = targetBlock.getX();
        y = targetBlock.getY();
        z = targetBlock.getZ();

        switch (blockFace) {
            case UP, DOWN -> {
                blocks.add(world.getBlockAt(x + 1, y, z));
                blocks.add(world.getBlockAt(x - 1, y, z));
                blocks.add(world.getBlockAt(x, y, z + 1));
                blocks.add(world.getBlockAt(x, y, z - 1));
                blocks.add(world.getBlockAt(x + 1, y, z + 1));
                blocks.add(world.getBlockAt(x - 1, y, z - 1));
                blocks.add(world.getBlockAt(x + 1, y, z - 1));
                blocks.add(world.getBlockAt(x - 1, y, z + 1));
            }
            case EAST, WEST -> {
                blocks.add(world.getBlockAt(x, y, z + 1));
                blocks.add(world.getBlockAt(x, y, z - 1));
                blocks.add(world.getBlockAt(x, y + 1, z));
                blocks.add(world.getBlockAt(x, y - 1, z));
                blocks.add(world.getBlockAt(x, y + 1, z + 1));
                blocks.add(world.getBlockAt(x, y - 1, z - 1));
                blocks.add(world.getBlockAt(x, y - 1, z + 1));
                blocks.add(world.getBlockAt(x, y + 1, z - 1));
            }
            case NORTH, SOUTH -> {
                blocks.add(world.getBlockAt(x + 1, y, z));
                blocks.add(world.getBlockAt(x - 1, y, z));
                blocks.add(world.getBlockAt(x, y + 1, z));
                blocks.add(world.getBlockAt(x, y - 1, z));
                blocks.add(world.getBlockAt(x + 1, y + 1, z));
                blocks.add(world.getBlockAt(x - 1, y - 1, z));
                blocks.add(world.getBlockAt(x + 1, y - 1, z));
                blocks.add(world.getBlockAt(x - 1, y + 1, z));
            }
            default -> {
            }
        }

        blocks.removeAll(Collections.singleton(null));
        return blocks;
    }

}
