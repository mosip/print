package io.mosip.print.entity;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * Unit tests for {@link BIR} class.
 *
 * <p>This class contains test cases for verifying the functionality of the BIR class,
 * including its builder pattern implementation and various edge cases.</p>
 *
 */
class BIRTest {

    /**
     * Creates a VersionType instance with the specified major and minor version numbers.
     *
     * @param major the major version number
     * @param minor the minor version number
     * @return a new VersionType instance
     */
    private VersionType createVersionType(int major, int minor) {
        return new VersionType.VersionTypeBuilder()
                .withMajor(major)
                .withMinor(minor)
                .build();
    }

    /**
     * Creates an SBInfo instance with the specified organization and type.
     *
     * @param org the organization name
     * @param type the type identifier
     * @return a new SBInfo instance
     */
    private SBInfo createSBInfo(String org, String type) {
        RegistryIDType format = new RegistryIDType();
        format.setOrganization(org);
        format.setType(type);
        return new SBInfo.SBInfoBuilder().setFormatOwner(format).build();
    }

    /**
     * Tests that the BIR builder creates a complete BIR object when all fields are provided.
     * Verifies that all fields are correctly set in the resulting BIR instance.
     */
    @Test
    void builderWithAllFieldsShouldCreateCompleteBIR() {
        VersionType version = createVersionType(1, 0);
        VersionType cbeffVersion = createVersionType(1, 1);
        BIRInfo birInfo = new BIRInfo();
        BDBInfo bdbInfo = new BDBInfo();
        SBInfo sbInfo = createSBInfo("TestOrg", "TestType");
        byte[] bdb = {1, 2, 3};
        byte[] sb = {4, 5, 6};
        Map<String, Object> others = new HashMap<>();
        others.put("key1", "value1");
        others.put("key2", 123);

        BIR bir = new BIR.BIRBuilder()
                .withVersion(version)
                .withCbeffversion(cbeffVersion)
                .withBirInfo(birInfo)
                .withBdbInfo(bdbInfo)
                .withBdb(bdb)
                .withSb(sb)
                .withSbInfo(sbInfo)
                .withOthers(others)
                .build();

        assertAll("All fields should be set correctly",
                () -> assertEquals(version, bir.getVersion()),
                () -> assertEquals(cbeffVersion, bir.getCbeffversion()),
                () -> assertEquals(birInfo, bir.getBirInfo()),
                () -> assertEquals(bdbInfo, bir.getBdbInfo()),
                () -> assertArrayEquals(bdb, bir.getBdb()),
                () -> assertArrayEquals(sb, bir.getSb()),
                () -> assertEquals(sbInfo, bir.getSbInfo()),
                () -> assertEquals("value1", bir.getOthers().get("key1")),
                () -> assertEquals(123, bir.getOthers().get("key2"))
        );
    }

    /**
     * Tests that the BIR builder creates a partial BIR object when only some fields are provided.
     * Verifies that only the specified fields are set while others remain null.
     */
    @Test
    void builderWithPartialFieldsShouldCreatePartialBIR() {
        VersionType version = createVersionType(1, 0);
        BIRInfo birInfo = new BIRInfo();
        byte[] bdb = {1, 2, 3};

        BIR bir = new BIR.BIRBuilder()
                .withVersion(version)
                .withBirInfo(birInfo)
                .withBdb(bdb)
                .build();

        assertAll("Only specified fields should be set",
                () -> assertEquals(version, bir.getVersion()),
                () -> assertEquals(birInfo, bir.getBirInfo()),
                () -> assertArrayEquals(bdb, bir.getBdb()),
                () -> assertNull(bir.getCbeffversion()),
                () -> assertNull(bir.getBdbInfo()),
                () -> assertNull(bir.getSb()),
                () -> assertNull(bir.getSbInfo()),
                () -> assertNull(bir.getOthers())
        );
    }

    /**
     * Tests that the BIR builder correctly combines multiple 'others' maps and individual entries.
     * Verifies that all key-value pairs from different sources are properly merged.
     */
    @Test
    void builderWithMultipleOthersShouldCombineThem() {
        Map<String, Object> others1 = new HashMap<>();
        others1.put("key1", "value1");
        Map<String, Object> others2 = new HashMap<>();
        others2.put("key2", "value2");

        BIR bir = new BIR.BIRBuilder()
                .withOthers(others1)
                .withOther("key3", "value3")
                .withOthers(others2)
                .build();

        assertAll("All 'others' should be combined",
                () -> assertEquals("value1", bir.getOthers().get("key1")),
                () -> assertEquals("value2", bir.getOthers().get("key2")),
                () -> assertEquals("value3", bir.getOthers().get("key3"))
        );
    }

    /**
     * Tests that the BIR builder handles null values gracefully without throwing exceptions.
     * Verifies that null inputs don't cause the builder to fail and that the others map is initialized.
     */
    @Test
    void builderWithNullValuesShouldHandleThemGracefully() {
        BIR bir = new BIR.BIRBuilder()
                .withVersion(null)
                .withBirInfo(null)
                .withBdb(null)
                .withOthers(null)
                .build();

        assertAll("Null values should be handled gracefully",
                () -> assertNull(bir.getVersion()),
                () -> assertNull(bir.getBirInfo()),
                () -> assertNull(bir.getBdb()),
                () -> assertNotNull(bir.getOthers()),
                () -> assertTrue(bir.getOthers().isEmpty())
        );
    }

    /**
     * Tests that the BIR builder correctly handles empty byte arrays.
     * Verifies that empty arrays are properly stored and can be retrieved.
     */
    @Test
    void builderWithEmptyByteArraysShouldHandleThem() {
        byte[] emptyBdb = new byte[0];
        byte[] emptySb = new byte[0];

        BIR bir = new BIR.BIRBuilder()
                .withBdb(emptyBdb)
                .withSb(emptySb)
                .build();

        assertAll("Empty byte arrays should be handled correctly",
                () -> assertNotNull(bir.getBdb()),
                () -> assertEquals(0, bir.getBdb().length),
                () -> assertNotNull(bir.getSb()),
                () -> assertEquals(0, bir.getSb().length)
        );
    }

    /**
     * Tests that multiple builds from the same builder instance create independent BIR objects.
     * Verifies that modifications to the builder don't affect previously built instances.
     */
    @Test
    void builderWithMultipleBuildsShouldCreateIndependentInstances() {
        BIR.BIRBuilder builder = new BIR.BIRBuilder()
                .withVersion(createVersionType(1, 0));

        BIR bir1 = builder.build();
        BIR bir2 = builder.withVersion(createVersionType(2, 0)).build();

        assertNotSame(bir1, bir2);
        assertEquals(1, bir1.getVersion().getMajor());
        assertEquals(2, bir2.getVersion().getMajor());
    }

    /**
     * Tests that the default constructor creates an empty BIR object with all fields set to null.
     * Verifies the initial state of a BIR instance created without the builder pattern.
     */
    @Test
    void defaultConstructorShouldInitializeEmptyBIR() {
        BIR bir = new BIR();

        assertAll("All fields should be null in default constructor",
                () -> assertNull(bir.getVersion()),
                () -> assertNull(bir.getCbeffversion()),
                () -> assertNull(bir.getBirInfo()),
                () -> assertNull(bir.getBdbInfo()),
                () -> assertNull(bir.getBdb()),
                () -> assertNull(bir.getSb()),
                () -> assertNull(bir.getSbInfo()),
                () -> assertNull(bir.getOthers()),
                () -> assertNull(bir.getBirs())
        );
    }

    /**
     * Tests that all builder methods work correctly when used together.
     * Comprehensive test verifying the complete functionality of the BIR builder pattern.
     */
    @Test
    void builderWithAllMethodsShouldWorkCorrectly() {
        VersionType version = createVersionType(1, 0);
        BIRInfo birInfo = new BIRInfo();
        BDBInfo bdbInfo = new BDBInfo();
        SBInfo sbInfo = createSBInfo("TestOrg", "TestType");
        byte[] bdb = {1, 2, 3};
        byte[] sb = {4, 5, 6};
        Map<String, Object> others = new HashMap<>();
        others.put("testKey", "testValue");

        BIR bir = new BIR.BIRBuilder()
                .withVersion(version)
                .withCbeffversion(version)
                .withBirInfo(birInfo)
                .withBdbInfo(bdbInfo)
                .withBdb(bdb)
                .withSb(sb)
                .withSbInfo(sbInfo)
                .withOther("key1", "value1")
                .withOthers(others)
                .build();

        assertAll("All builder methods should work correctly",
                () -> assertEquals(version, bir.getVersion()),
                () -> assertEquals(version, bir.getCbeffversion()),
                () -> assertEquals(birInfo, bir.getBirInfo()),
                () -> assertEquals(bdbInfo, bir.getBdbInfo()),
                () -> assertArrayEquals(bdb, bir.getBdb()),
                () -> assertArrayEquals(sb, bir.getSb()),
                () -> assertEquals(sbInfo, bir.getSbInfo()),
                () -> assertEquals("value1", bir.getOthers().get("key1")),
                () -> assertEquals("testValue", bir.getOthers().get("testKey"))
        );
    }
}
