package fr.iban.common.chat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatItemTokensTest {

    @Test
    void detectsShortToken() {
        assertTrue(ChatItemTokens.containsToken("regarde mon [i] stylé"));
    }

    @Test
    void detectsLongToken() {
        assertTrue(ChatItemTokens.containsToken("regarde mon [item]"));
    }

    @Test
    void ignoresMessageWithoutToken() {
        assertFalse(ChatItemTokens.containsToken("juste un message [x] normal"));
    }

    @Test
    void replacesAllOccurrencesWithSentinel() {
        String result = ChatItemTokens.replaceWithSentinel("a [i] b [item] c");
        assertEquals("a  b  c", result);
        assertFalse(result.contains("[i]"));
        assertFalse(result.contains("[item]"));
    }

    @Test
    void leavesMessageUnchangedWhenNoToken() {
        assertEquals("rien ici", ChatItemTokens.replaceWithSentinel("rien ici"));
    }
}
