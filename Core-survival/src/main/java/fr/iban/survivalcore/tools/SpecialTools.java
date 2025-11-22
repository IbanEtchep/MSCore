package fr.iban.survivalcore.tools;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.iban.survivalcore.lang.LangKey;
import fr.iban.survivalcore.lang.MessageBuilder;

import java.util.*;
import java.util.stream.Collectors;

public class SpecialTools {

    public static final Set<Material> canBreakWith3x3Pickaxe = Set.of(
            Material.COBBLESTONE, Material.STONE, Material.DIRT, Material.NETHERRACK, Material.GRASS_BLOCK, Material.DIORITE,
            Material.ANDESITE, Material.GRANITE, Material.IRON_ORE, Material.COAL_ORE, Material.GOLD_ORE, Material.LAPIS_ORE,
            Material.REDSTONE_ORE, Material.GRAVEL, Material.BLACKSTONE, Material.END_STONE, Material.BASALT,
            Material.MAGMA_BLOCK, Material.DEEPSLATE, Material.DEEPSLATE_COAL_ORE, Material.COBBLED_DEEPSLATE,
            Material.DEEPSLATE_COPPER_ORE, Material.DEEPSLATE_GOLD_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.DEEPSLATE_LAPIS_ORE, Material.DEEPSLATE_REDSTONE_ORE, Material.COPPER_ORE, Material.TUFF,
            Material.CRIMSON_NYLIUM, Material.WARPED_NYLIUM
    );

    public static final Set<Material> canBreakWith3x3Shovel = Set.of(
            Material.GRASS_BLOCK, Material.DIRT, Material.SAND, Material.RED_SAND, Material.GRAVEL,
            Material.SOUL_SAND, Material.SOUL_SOIL, Material.CLAY
    );

    public static final Map<UUID, BlockFace> faces = new HashMap<>();

    // ------------------------------------------------------------------------
    // Generic methods to remove 80% of duplicates
    // ------------------------------------------------------------------------

    private static ItemStack createTool(Material material, LangKey nameKey, LangKey loreKey) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(component(nameKey));
        meta.lore(components(loreKey));
        item.setItemMeta(meta);
        return item;
    }

    private static boolean checkTool(ItemStack item, Material mat, LangKey checkKey) {
        if (item.getType() != mat || !item.hasItemMeta() || !item.getItemMeta().hasLore())
            return false;

        Component comp = component(checkKey);
        return item.getItemMeta().lore().contains(comp);
    }

    private static Component component(LangKey key) {
        return Component.text(MessageBuilder.translatable(key).toLegacy());
    }

    private static List<Component> components(LangKey key) {
        return MessageBuilder.translatable(key).toLegacyComponents();
    }

    public static ItemStack get3x3Pickaxe() {
        return createTool(Material.NETHERITE_PICKAXE, LangKey.TOOLS_PICKAXE3X3_NAME, LangKey.TOOLS_PICKAXE3X3_LORE);
    }

    public static ItemStack getCutCleanPickaxe() {
        return createTool(Material.NETHERITE_PICKAXE, LangKey.TOOLS_CUTCLEAN_NAME, LangKey.TOOLS_CUTCLEAN_LORE);
    }

    public static ItemStack get3x3Shovel() {
        return createTool(Material.NETHERITE_SHOVEL, LangKey.TOOLS_SHOVEL3X3_NAME, LangKey.TOOLS_SHOVEL3X3_LORE);
    }

    public static ItemStack getLumberjackAxe() {
        return createTool(Material.NETHERITE_AXE, LangKey.TOOLS_LUMBERJACK_NAME, LangKey.TOOLS_LUMBERJACK_LORE);
    }

    public static ItemStack getFarmerHoe() {
        return createTool(Material.NETHERITE_HOE, LangKey.TOOLS_HOE_NAME, LangKey.TOOLS_HOE_LORE);
    }

    public static ItemStack get3x3FarmerHoe() {
        return createTool(Material.NETHERITE_HOE, LangKey.TOOLS_HOE3X3_NAME, LangKey.TOOLS_HOE3X3_LORE);
    }

    public static ItemStack getXpSword() {
        return createTool(Material.NETHERITE_SWORD, LangKey.TOOLS_XPSWORD_NAME, LangKey.TOOLS_XPSWORD_LORE);
    }

    public static boolean isLumberjackAxe(ItemStack item) {
        return checkTool(item, Material.NETHERITE_AXE, LangKey.TOOLS_LUMBERJACK_CHECK);
    }

    public static boolean is3x3Shovel(ItemStack item) {
        return checkTool(item, Material.NETHERITE_SHOVEL, LangKey.TOOLS_SHOVEL3X3_CHECK);
    }

    public static boolean isCutCleanPickaxe(ItemStack item) {
        return checkTool(item, Material.NETHERITE_PICKAXE, LangKey.TOOLS_CUTCLEAN_CHECK);
    }

    public static boolean isReplantHoue(ItemStack item) {
        return checkTool(item, Material.NETHERITE_HOE, LangKey.TOOLS_HOE_CHECK);
    }

    public static boolean is3x3ReplantHoue(ItemStack item) {
        return isReplantHoue(item) &&
                checkTool(item, Material.NETHERITE_HOE, LangKey.TOOLS_HOE3X3_CHECK);
    }

    public static boolean is3x3Pickaxe(ItemStack item) {
        return checkTool(item, Material.NETHERITE_PICKAXE, LangKey.TOOLS_PICKAXE3X3_CHECK);
    }

    public static boolean isXpBoostSword(ItemStack item) {
        return checkTool(item, Material.NETHERITE_SWORD, LangKey.TOOLS_XPSWORD_CHECK);
    }

    public static List<Block> getSurroundingBlocksShovel(Player player, Block targetBlock) {
        return getSurroundingBlocks(faces.get(player.getUniqueId()), targetBlock).stream()
                .filter(block -> canBreakWith3x3Shovel.contains(block.getType()))
                .collect(Collectors.toList());
    }

    public static List<Block> getSurroundingBlocksPickaxe(Player player, Block targetBlock) {
        return getSurroundingBlocks(faces.get(player.getUniqueId()), targetBlock).stream()
                .filter(block -> canBreakWith3x3Pickaxe.contains(block.getType()))
                .collect(Collectors.toList());
    }

    public static List<Block> getSurroundingBlocks(BlockFace face, Block block) {
        List<Block> blocks = new ArrayList<>();
        World w = block.getWorld();
        int x = block.getX(), y = block.getY(), z = block.getZ();

        switch (face) {
            case UP, DOWN -> add9(blocks, w, x, y, z, 1, 0, 1);
            case EAST, WEST -> add9(blocks, w, x, y, z, 0, 1, 1);
            case NORTH, SOUTH -> add9(blocks, w, x, y, z, 1, 1, 0);
            default -> {
        
            }
        }

        blocks.removeIf(Objects::isNull);
        return blocks;
    }

    private static void add9(List<Block> list, World w, int x, int y, int z, int dx, int dy, int dz) {
        list.add(w.getBlockAt(x + dx, y, z));
        list.add(w.getBlockAt(x - dx, y, z));
        list.add(w.getBlockAt(x, y + dy, z));
        list.add(w.getBlockAt(x, y - dy, z));
        list.add(w.getBlockAt(x + dx, y + dy, z + dz));
        list.add(w.getBlockAt(x - dx, y - dy, z - dz));
        list.add(w.getBlockAt(x + dx, y - dy, z + dz));
        list.add(w.getBlockAt(x - dx, y + dy, z - dz));
    }
}
