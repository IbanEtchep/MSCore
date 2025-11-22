package fr.iban.bukkitcore.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


public abstract class Menu implements InventoryHolder {

    protected Player player;
    protected Inventory inventory;
    protected ItemStack FILLER_GLASS = makeItem(Material.WHITE_STAINED_GLASS_PANE, " ");
    protected final List<Consumer<InventoryClickEvent>> clickHandlers = new ArrayList<>();


    public abstract String getMenuName();

    public abstract int getRows();

    public int getSlots() {
        return getRows()*9;
    }

    public abstract void handleMenu(InventoryClickEvent e);

    public void handleMenuClose(InventoryCloseEvent e) {

    }

    //let each menu decide what items are to be placed in the inventory menu
    public abstract void setMenuItems();

    public Menu(Player player) {
        this.player = player;
    }

    //When called, an inventory is created and opened for the player
    public void open() {
        //The owner of the inventory created is the Menu itself,
        // so we are able to reverse engineer the Menu object from the
        // inventoryHolder in the MenuListener class when handling clicks
        inventory = Bukkit.createInventory(this, getSlots(), Component.text(getMenuName()));

        //grab all the items specified to be used for this menu and add to inventory
        this.setMenuItems();

        //open the inventory for the player
        player.openInventory(inventory);
    }

    //Overridden method from the InventoryHolder interface
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    //Helpful utility method to fill all remaining slots with "filler glass"
    public void setFillerGlass(){
        for (int i = 0; i < getSlots(); i++) {
            if (inventory.getItem(i) == null){
                inventory.setItem(i, FILLER_GLASS);
            }
        }
    }

    public ItemStack makeItem(Material material, String displayName, String... lore) {

        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.displayName(Component.text(displayName));

        List<Component> compLore = new ArrayList<>();
        for (String s : lore) compLore.add(Component.text(s));
        itemMeta.lore(compLore);

        item.setItemMeta(itemMeta);

        return item;
    }

    //Set the border for the menu
    public void addMenuBorder(){

        for (int i = 0; i < 10; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, FILLER_GLASS);
            }
        }

        if(getRows() >= 2) {
            inventory.setItem(9, FILLER_GLASS);
            inventory.setItem(17, FILLER_GLASS);
        }
        if(getRows() >= 3) {
            inventory.setItem(18, FILLER_GLASS);
            inventory.setItem(26, FILLER_GLASS);
        }
        if(getRows() >= 4) {
            inventory.setItem(35, FILLER_GLASS);
            inventory.setItem(27, FILLER_GLASS);
        }

        if(getRows() >= 5) {
            inventory.setItem(36, FILLER_GLASS);
            inventory.setItem(44, FILLER_GLASS);
        }

        for (int i = getSlots() - 9; i < getSlots(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, FILLER_GLASS);
            }
        }
    }


    public void fillWithGlass() {
        for (int i = inventory.firstEmpty() ; inventory.firstEmpty() != -1; i = inventory.firstEmpty()) {
            inventory.setItem(i, FILLER_GLASS);
        }
    }

    protected List<String> splitString(String msg, int lineSize) {
        List<String> res = new ArrayList<>();

        Pattern p = Pattern.compile("\\b.{1," + (lineSize-1) + "}\\b\\W?");
        Matcher m = p.matcher(msg);

        while(m.find()) {
            res.add("§a" + m.group());
        }
        return res;
    }

    protected boolean displayNameEquals(ItemStack item, String toCompare) {
        Component name = item.getItemMeta().displayName();
        String plain = name == null ? "" : PlainTextComponentSerializer.plainText().serialize(name);
        return plain.equals(toCompare);
    }

    /**
     *
     * @param x - colonne (1 à 9)
     * @param y - ligne (1 à nb de lignes)
     * @param it - item à mettre
     */
    protected void setItem(int x, int y, ItemStack it) {
        inventory.setItem((x-1)*9 + x-1, it);
    }

}
