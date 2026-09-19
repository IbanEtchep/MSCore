package fr.iban.velocitycore.command.parametertypes;

import fr.iban.common.manager.PlayerManager;
import fr.iban.common.model.MSPlayerProfile;
import fr.iban.velocitycore.CoreVelocityPlugin;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.parameter.PrioritySpec;
import revxrsal.commands.stream.MutableStringStream;
import revxrsal.commands.velocity.actor.VelocityCommandActor;

import java.util.stream.Collectors;

public class MSPlayerProfileParameterType implements ParameterType<VelocityCommandActor, MSPlayerProfile> {

    private final CoreVelocityPlugin plugin;

    public MSPlayerProfileParameterType(CoreVelocityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public MSPlayerProfile parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<VelocityCommandActor> executionContext) {
        String name = input.readString();
        MSPlayerProfile profile = plugin.getPlayerManager().getProfile(name);

        if(profile == null) {
            throw new CommandErrorException("Le joueur " + name + " n''est pas en ligne.");
        }

        return profile;
    }

    @Override
    public @NotNull SuggestionProvider<VelocityCommandActor> defaultSuggestions() {
        // Lamp 4.0.0-rc.16 Brigadier adapter doesn't filter by the typed prefix; filter here.
        return (context) -> {
            String source = context.input().source();
            int lastSpace = source.lastIndexOf(' ');
            String prefix = (lastSpace < 0 ? "" : source.substring(lastSpace + 1)).toLowerCase();
            return plugin.getPlayerManager().getOnlinePlayerNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        };
    }

    @Override
    public @NotNull PrioritySpec parsePriority() {
        return PrioritySpec.highest();
    }

}