package fr.iban.bukkitcore.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.iban.bukkitcore.lang.LangKey;
import fr.iban.bukkitcore.lang.MessageBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public abstract class PaginatedMenu extends Menu {

    protected int page = 0;
    protected int maxItemsPerPage = getSlots() - 14 - (getRows()*2);
    protected int index = 0;

    protected PaginatedMenu(Player player) {
        super(player);
    }

    public int getElementAmount() {
        return -1;
    }

    @Override
    public void addMenuBorder(){

        index = page*getMaxItemsPerPage();

        int lastRowFirst = (getRows()-1)*9;

        if(getElementAmount() != -1 && getElementAmount() > maxItemsPerPage && (index+getMaxItemsPerPage() + 1) <= getElementAmount()) {
            inventory.setItem(
                    lastRowFirst+5,
                    makeItem(
                            Material.GREEN_STAINED_GLASS_PANE,
                            MessageBuilder.translatable(LangKey.MENUS_PAGINATED_NEXT).toLegacy()
                    )
            );
        }

        if(page > 0) {
            inventory.setItem(
                    lastRowFirst+3,
                    makeItem(
                            Material.GREEN_STAINED_GLASS_PANE,
                            MessageBuilder.translatable(LangKey.MENUS_PAGINATED_PREVIOUS).toLegacy()
                    )
            );
        }

        inventory.setItem(
                lastRowFirst+4,
                makeItem(
                        Material.RED_STAINED_GLASS_PANE,
                        MessageBuilder.translatable(LangKey.MENUS_PAGINATED_CLOSE).toLegacy()
                )
        );

        super.addMenuBorder();
    }

    protected void checkBottonsClick(ItemStack item, Player player) {
        checkNextBottonClick(item);
        checkPreviousBottonClick(item);
        checkCloseBottonClick(item, player);
    }

    protected void checkNextBottonClick(ItemStack item) {
        Component name = item.getItemMeta().displayName();
        String plain = name == null ? "" : PlainTextComponentSerializer.plainText().serialize(name);

        if(plain.equalsIgnoreCase(
                MessageBuilder.translatable(LangKey.MENUS_PAGINATED_NEXT).toLegacy()
        )){
            page += 1;
            super.open();
        }
    }

    protected void checkPreviousBottonClick(ItemStack item) {
        Component name = item.getItemMeta().displayName();
        String plain = name == null ? "" : PlainTextComponentSerializer.plainText().serialize(name);

        if(plain.equalsIgnoreCase(
                MessageBuilder.translatable(LangKey.MENUS_PAGINATED_PREVIOUS).toLegacy()
        )){
            page -= 1;
            if(page == 0) {
                index = 0;
            }
            super.open();
        }
    }

    protected void checkCloseBottonClick(ItemStack item, Player player) {
        Component name = item.getItemMeta().displayName();
        String plain = name == null ? "" : PlainTextComponentSerializer.plainText().serialize(name);

        if(plain.equalsIgnoreCase(
                MessageBuilder.translatable(LangKey.MENUS_PAGINATED_CLOSE).toLegacy()
        )){
            player.closeInventory();
        }
    }

    public int getMaxItemsPerPage() {
        return maxItemsPerPage;
    }
}
