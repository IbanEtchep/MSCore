package fr.iban.survivalcore.listeners;

import com.google.common.collect.Lists;
import fr.iban.survivalcore.SurvivalCorePlugin;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.List;

public class VillagerEvents implements Listener {

	private final SurvivalCorePlugin plugin;

	public VillagerEvents(SurvivalCorePlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onVillagerInteract(final PlayerInteractAtEntityEvent e){
		if (!(e.getRightClicked() instanceof Villager villager)) return;

        List<MerchantRecipe> recipes = Lists.newArrayList(villager.getRecipes());

        for (MerchantRecipe recipe : recipes) {

            if (recipe.getResult().getType().equals(Material.ENCHANTED_BOOK)) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) recipe.getResult().getItemMeta();

                if (meta.hasStoredEnchant(Enchantment.MENDING)) {
                    recipe.setMaxUses(1);
                    int initialPrice = plugin.getConfig().getInt("mending-trade-price");
                    ItemStack diams = new ItemStack(Material.valueOf(plugin.getConfig().getString("mending-trade-material")), initialPrice);
                    recipe.setIngredients(List.of(diams));
                    recipe.setIgnoreDiscounts(true);
                }
            }
        }

		villager.setRecipes(recipes);
	}

    @EventHandler
    public void onVillagerCured(EntityTransformEvent event) {
        if(!plugin.getConfig().getBoolean("villagers.disable-curing-discounts", false)) return;

        if (event.getTransformReason() == EntityTransformEvent.TransformReason.CURED &&
                event.getEntityType() == EntityType.ZOMBIE_VILLAGER &&
                event.getTransformedEntity() instanceof Villager villager) {

            villager.clearReputations();
        }
    }

}
