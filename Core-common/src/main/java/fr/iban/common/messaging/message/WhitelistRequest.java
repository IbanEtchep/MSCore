package fr.iban.common.messaging.message;

import java.util.UUID;

public class WhitelistRequest {

    private final UUID requestId;
    private final String command;
    private final String context;
    private final String playerName;
    private final long createdAt = System.currentTimeMillis();

    public WhitelistRequest(UUID requestId, String command, String context, String playerName) {
        this.requestId = requestId;
        this.command = command;
        this.context = context;
        this.playerName = playerName;
    }

    public UUID getRequestId() { return requestId; }
    public String getCommand() { return command; }
    public String getContext() { return context; }
    public String getPlayerName() { return playerName; }
    public long getCreatedAt() { return createdAt; }
}
