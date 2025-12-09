package io.mosip.print.entity;

import io.mosip.print.model.KeyValuePair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link MatchDecision}
 */
public class MatchDecisionTest {

    /**
     * Verifies all methods of {@link MatchDecision}.
     */
    @Test
    void verifyMatchDecisionMethods() {
        MatchDecision decision1 = new MatchDecision();
        decision1.setMatch(true);

        KeyValuePair kv1 = new KeyValuePair();
        kv1.setKey("key1");
        kv1.setValue("value1");

        KeyValuePair kv2 = new KeyValuePair();
        kv2.setKey("key2");
        kv2.setValue("value2");

        decision1.setAnalyticsInfo(new KeyValuePair[]{kv1, kv2});

        assertTrue(decision1.isMatch());
        assertNotNull(decision1.getAnalyticsInfo());
        assertEquals("key1", decision1.getAnalyticsInfo()[0].getKey());
        assertEquals("value1", decision1.getAnalyticsInfo()[0].getValue());

        MatchDecision decision2 = new MatchDecision();
        decision2.setMatch(true);
        decision2.setAnalyticsInfo(new KeyValuePair[]{kv1, kv2});

        assertEquals(decision1, decision2);
        assertEquals(decision1.hashCode(), decision2.hashCode());
        assertNotNull(decision1.toString());

        decision2.setMatch(false);
        assertNotEquals(decision1, decision2);
    }
}
