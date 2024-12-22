package fr.iban.bukkitcore.menu;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.manager.BukkitPlayerManager;
import fr.iban.bukkitcore.utils.ItemBuilder;
import fr.iban.bukkitcore.utils.Options;
import fr.iban.common.model.MSPlayerProfile;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class OptionsMenu extends PaginatedMenu {

    private final CoreBukkitPlugin core;

    public OptionsMenu(Player player) {
        super(player);
        this.core = CoreBukkitPlugin.getInstance();
    }

    @Override
    public String getMenuName() {
        return "§2Vos paramètres";
    }

    @Override
    public int getRows() {
        return 3;
    }

    @Override
    public int getElementAmount() {
        return Options.values().length;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();

        BukkitPlayerManager playerManager = core.getPlayerManager();
        MSPlayerProfile profile = playerManager.getProfile(player.getUniqueId());

        checkBottonsClick(item, player);

        if (Options.getByDisplayName(item.getItemMeta().getDisplayName()) != null) {
            if (item.getItemMeta().getDisplayName().startsWith("§4")) {
                profile.setOption(Objects.requireNonNull(Options.getByDisplayName(item.getItemMeta().getDisplayName())).getOption(), true);
                playerManager.saveProfile(profile);
            } else {
                profile.setOption(Objects.requireNonNull(Options.getByDisplayName(item.getItemMeta().getDisplayName())).getOption(), false);
                playerManager.saveProfile(profile);
            }
            super.open();
        }
    }

    @Override
    public void setMenuItems() {
        addMenuBorder();

        for (int i = 0; i < getMaxItemsPerPage(); i++) {
            index = getMaxItemsPerPage() * page + i;
            if (index >= Options.values().length) break;
            Options option = Options.values()[index];
            MSPlayerProfile msPlayer = core.getPlayerManager().getProfile(player.getUniqueId());

            if (option != null) {
                if (msPlayer.getOption(option.getOption())) {
                    inventory.addItem(new ItemBuilder(option.getItem()).setName("§2" + option.getDisplayName()).build());
                } else {
                    inventory.addItem(new ItemBuilder(option.getItem()).setName("§4" + option.getDisplayName()).build());
                }
            }
        }
    }

}