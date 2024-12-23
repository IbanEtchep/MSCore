package fr.iban.bukkitcore.commands.parametertypes;

import fr.iban.common.manager.PlayerManager;
import fr.iban.common.model.MSPlayerProfile;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.parameter.PrioritySpec;
import revxrsal.commands.stream.MutableStringStream;

public class MSPlayerProfileParameterType implements ParameterType<BukkitCommandActor, MSPlayerProfile> {

    private final PlayerManager playerManager;

    public MSPlayerProfileParameterType(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @Override
    public MSPlayerProfile parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<@NotNull BukkitCommandActor> executionContext) {
        String name = input.readString();
        MSPlayerProfile profile = playerManager.getProfile(name);

        if(profile == null) {
            throw new CommandErrorException("Le joueur " + name + " n''est pas en ligne.");
        }

        return profile;
    }

    @Override
    public @NotNull SuggestionProvider<@NotNull BukkitCommandActor> defaultSuggestions() {
        return (context) -> playerManager.getOnlinePlayerNames();
    }

    @Override
    public @NotNull PrioritySpec parsePriority() {
        return PrioritySpec.highest();
    }
}
