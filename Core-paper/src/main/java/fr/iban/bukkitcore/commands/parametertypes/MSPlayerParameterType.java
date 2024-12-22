package fr.iban.bukkitcore.commands.parametertypes;

import fr.iban.common.manager.PlayerManager;
import fr.iban.common.model.MSPlayer;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.node.ExecutionContext;
import revxrsal.commands.parameter.ParameterType;
import revxrsal.commands.parameter.PrioritySpec;
import revxrsal.commands.stream.MutableStringStream;

public class MSPlayerParameterType implements ParameterType<BukkitCommandActor, MSPlayer> {

    private final PlayerManager playerManager;

    public MSPlayerParameterType(PlayerManager playerManager) {
        this.playerManager = playerManager;
    }

    @Override
    public MSPlayer parse(@NotNull MutableStringStream input, @NotNull ExecutionContext<@NotNull BukkitCommandActor> executionContext) {
        String name = input.readString();
        MSPlayer player = playerManager.getOfflinePlayer(name);

        if(player == null) {
            throw new CommandErrorException("Le joueur " + name + " n''a jamais joué sur le serveur.");
        }

        return player;
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
