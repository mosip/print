package io.mosip.print.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link RegistryIDType}.
 */
public class RegistryIDTypeTest {

    /**
     * Validates constructors, accessors, mutators, equality, hash code, and string representation.
     */
    @Test
    void verifyRegistryIDTypeMethods() {
        RegistryIDType obj1 = new RegistryIDType();
        obj1.setOrganization("Org1");
        obj1.setType("Type1");

        assertEquals("Org1", obj1.getOrganization());
        assertEquals("Type1", obj1.getType());

        RegistryIDType obj2 = new RegistryIDType("Org1", "Type1");
        assertEquals("Org1", obj2.getOrganization());
        assertEquals("Type1", obj2.getType());

        assertEquals(obj1, obj2);
        assertEquals(obj1.hashCode(), obj2.hashCode());
        assertNotNull(obj1.toString());

        assertEquals(obj1, obj1);
        assertNotEquals(obj1, null);
        assertNotEquals(obj1, "string");

        obj2.setOrganization("DifferentOrg");
        assertNotEquals(obj1, obj2);
        assertNotEquals(obj1.hashCode(), obj2.hashCode());
    }
}