package io.mosip.print.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SBInfo}.
 */
class SBIInfoTest {

    /**
     * Verifies builder correctly sets the format field.
     */
    @Test
    void verifyBuilderSetsFormat() {
        RegistryIDType format = new RegistryIDType("Org", "Type");
        SBInfo info = new SBInfo.SBInfoBuilder()
                .setFormatOwner(format)
                .build();

        assertEquals(format, info.getFormat());
    }

    /**
     * Verifies setter and getter work correctly.
     */
    @Test
    void verifySetterAndGetter() {
        SBInfo info = new SBInfo.SBInfoBuilder().build();
        RegistryIDType format = new RegistryIDType("Org2", "Type2");
        info.setFormat(format);
        assertEquals(format, info.getFormat());
    }

    /**
     * Verifies equals and hashCode across different branches.
     */
    @Test
    void verifyEqualsAndHashCode() {
        RegistryIDType format = new RegistryIDType("Org", "Type");

        SBInfo info1 = new SBInfo.SBInfoBuilder().setFormatOwner(format).build();
        SBInfo info2 = new SBInfo.SBInfoBuilder().setFormatOwner(format).build();
        SBInfo info3 = new SBInfo.SBInfoBuilder().setFormatOwner(new RegistryIDType("OtherOrg", "Type")).build();

        assertEquals(info1, info2);
        assertEquals(info1.hashCode(), info2.hashCode());
        assertEquals(info1, info1);
        assertNotEquals(info1, null);
        assertNotEquals(info1, "string");
        assertNotEquals(info1, info3);
        assertNotEquals(info1.hashCode(), info3.hashCode());
    }

    /**
     * Verifies toString returns a non-null string containing expected content.
     */
    @Test
    void verifyToString() {
        RegistryIDType format = new RegistryIDType("FmtOrg", "FmtType");
        SBInfo info = new SBInfo.SBInfoBuilder()
                .setFormatOwner(format)
                .build();
        String out = info.toString();
        assertNotNull(out);
        assertTrue(out.contains("FmtOrg"));
        assertTrue(out.contains("FmtType"));
    }
}