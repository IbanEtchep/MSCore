package fr.iban.common.messaging.message;

import java.util.UUID;

public class WhitelistReply {

    private UUID requestId;
    private String category;

    public WhitelistReply(UUID requestId, String category) {
        this.requestId = requestId;
        this.category = category;
    }

    public UUID getRequestId() { return requestId; }
    public void setRequestId(UUID requestId) { this.requestId = requestId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
