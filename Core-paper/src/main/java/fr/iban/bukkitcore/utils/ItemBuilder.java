package fr.iban.bukkitcore.utils;

import java.util.ArrayList;
import java.util.List;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;


public class ItemBuilder {

    private ItemStack stack;

    public ItemBuilder(Material mat) {
        stack = new ItemStack(mat);
    }
    
    public ItemBuilder(ItemStack item) {
    	stack = item;
    }

    public ItemMeta getItemMeta() {
        return stack.getItemMeta();
    }
    
    public ItemBuilder setName(String name) {
        ItemMeta meta = stack.getItemMeta();
    	meta.displayName(Component.text(name));
    	stack.setItemMeta(meta);
    	return this;
    }
    

    public ItemBuilder setColor(Color color) {
            LeatherArmorMeta meta = (LeatherArmorMeta) stack.getItemMeta();
            meta.setColor(color);
            setItemMeta(meta);
        return this;
    }

    public ItemBuilder setGlow (boolean glow) {
        if (glow) {
            addEnchant(Enchantment.KNOCKBACK, 1);
            addItemFlag(ItemFlag.HIDE_ENCHANTS);
        } else {
            ItemMeta meta = getItemMeta();
            for (Enchantment enchantment : meta.getEnchants().keySet()) {
                meta.removeEnchant(enchantment);
            }
        }
        return this;
    }

    public ItemBuilder setUnbreakable (boolean unbreakable) {
        ItemMeta meta = stack.getItemMeta();
        meta.setUnbreakable(unbreakable);
        stack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        stack.setAmount(amount);
        return this;
    }

    public ItemBuilder setItemMeta(ItemMeta meta) {
        stack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setDisplayName(String displayname) {
        ItemMeta meta = getItemMeta();
        meta.displayName(Component.text(displayname));
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder setItemStack (ItemStack stack) {
        this.stack = stack;
        return this;
    }

    public ItemBuilder setLore(List<String> list) {
        ItemMeta meta = getItemMeta();
        List<Component> lore = new ArrayList<>();
        for (String s : list) lore.add(Component.text(s));
        meta.lore(lore);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder setLore (String lore) {
        ArrayList<String> loreList = new ArrayList<>();
        loreList.add(lore);
        ItemMeta meta = getItemMeta();
        List<Component> comp = new ArrayList<>();
        comp.add(Component.text(lore));
        meta.lore(comp);
        setItemMeta(meta);
        return this;
    }
    
    public ItemBuilder addLore (String lore) {
        ItemMeta metaCurrent = stack.getItemMeta();
        List<Component> existing = metaCurrent.lore();
        List<String> loreList = new ArrayList<>();
        if (existing != null) {
            for (Component c : existing) {
                loreList.add(PlainTextComponentSerializer.plainText().serialize(c));
            }
        }
        loreList.add(lore);
        ItemMeta meta = getItemMeta();
        List<Component> comp = new ArrayList<>();
        for (String s : loreList) comp.add(Component.text(s));
        meta.lore(comp);
        setItemMeta(meta);
        return this;
    }


    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        ItemMeta meta = getItemMeta();
        meta.addEnchant(enchantment, level, true);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder addItemFlag(ItemFlag flag) {
        ItemMeta meta = getItemMeta();
        meta.addItemFlags(flag);
        setItemMeta(meta);
        return this;
    }

    public ItemStack build() {
        return stack;
    }

}
