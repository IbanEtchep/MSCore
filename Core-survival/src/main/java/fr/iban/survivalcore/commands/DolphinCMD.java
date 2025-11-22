package fr.iban.survivalcore.commands;

import fr.iban.survivalcore.lang.LangKey;
import fr.iban.survivalcore.lang.MessageBuilder;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("dolphin")
public class DolphinCMD {

    @CommandPermission("servercore.dolphin")
    public void toggle(Player player) {
        if (player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE)) {
            player.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
            player.sendMessage(
                    MessageBuilder.translatable(LangKey.DOLPHIN_DISABLED).toLegacy()
            );
        } else {
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.DOLPHINS_GRACE,
                    Integer.MAX_VALUE,
                    0
            ));
            player.sendMessage(
                    MessageBuilder.translatable(LangKey.DOLPHIN_ENABLED).toLegacy()
            );
        }
    }
	
}
