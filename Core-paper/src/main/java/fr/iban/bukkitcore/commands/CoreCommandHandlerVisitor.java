package fr.iban.bukkitcore.commands;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.bukkitcore.commands.annotation.Context;
import fr.iban.bukkitcore.commands.annotation.SenderType;
import fr.iban.bukkitcore.commands.annotation.SurvivalServer;
import fr.iban.bukkitcore.commands.parametertypes.MSPlayerParameterType;
import fr.iban.bukkitcore.commands.parametertypes.MSPlayerProfileParameterType;
import fr.iban.common.model.MSPlayer;
import fr.iban.common.model.MSPlayerProfile;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.LampBuilderVisitor;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.util.Arrays;

public class CoreCommandHandlerVisitor {

    private final CoreBukkitPlugin plugin;

    public CoreCommandHandlerVisitor(CoreBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    public <A extends BukkitCommandActor> @NotNull LampBuilderVisitor<A> visitor() {
        return builder -> {
            builder.suggestionProviders()
                    .addProviderForAnnotation(SurvivalServer.class,
                            annotation -> context -> plugin.getServerManager().getSurvivalServers()
                    )
                    .addProviderForAnnotation(Context.class,
                            annotation -> context -> Arrays.asList("global", "bukkit", "proxy")
                    )
                    .addProviderForAnnotation(SenderType.class,
                            annotation -> context -> Arrays.asList("player", "staff", "console")
                    );

            builder.parameterTypes()
                    .addParameterType(MSPlayer.class, new MSPlayerParameterType(plugin.getPlayerManager()))
                    .addParameterType(MSPlayerProfile.class, new MSPlayerProfileParameterType(plugin.getPlayerManager()));
        };
    }
}
