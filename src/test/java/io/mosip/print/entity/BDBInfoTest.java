package io.mosip.print.entity;

import io.mosip.print.constant.BiometricType;
import io.mosip.print.constant.ProcessedLevelType;
import io.mosip.print.constant.PurposeType;
import io.mosip.print.constant.QualityType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link BDBInfo}.
 */
class BDBInfoTest {

    /**
     * Verifies default constructor creates an object with all fields unset.
     */
    @Test
    void verifyDefaultConstructor() {
        BDBInfo info = new BDBInfo();
        assertNull(info.getChallengeResponse());
        assertNull(info.getIndex());
        assertNull(info.getFormat());
        assertNull(info.getEncryption());
        assertNull(info.getCreationDate());
        assertNull(info.getNotValidBefore());
        assertNull(info.getNotValidAfter());
        assertNull(info.getType());
        assertNull(info.getSubtype());
        assertNull(info.getLevel());
        assertNull(info.getProduct());
        assertNull(info.getCaptureDevice());
        assertNull(info.getFeatureExtractionAlgorithm());
        assertNull(info.getComparisonAlgorithm());
        assertNull(info.getCompressionAlgorithm());
        assertNull(info.getPurpose());
        assertNull(info.getQuality());
    }

    /**
     * Verifies builder assigns all fields correctly.
     */
    @Test
    void verifyBuilderPopulatesFields() {
        byte[] challengeResponse = {1, 2, 3};
        String index = "IDX";
        RegistryIDType format = new RegistryIDType();
        Boolean encryption = Boolean.TRUE;
        LocalDateTime now = LocalDateTime.now();
        List<BiometricType> types = Collections.singletonList(BiometricType.FACE);
        List<String> subtypes = Collections.singletonList("LEFT");
        ProcessedLevelType level = ProcessedLevelType.RAW;
        RegistryIDType product = new RegistryIDType();
        RegistryIDType captureDevice = new RegistryIDType();
        RegistryIDType featureAlgo = new RegistryIDType();
        RegistryIDType comparisonAlgo = new RegistryIDType();
        RegistryIDType compressionAlgo = new RegistryIDType();
        PurposeType purpose = PurposeType.AUDIT;
        QualityType quality = new QualityType();

        BDBInfo info = new BDBInfo.BDBInfoBuilder()
                .withChallengeResponse(challengeResponse)
                .withIndex(index)
                .withFormat(format)
                .withEncryption(encryption)
                .withCreationDate(now)
                .withNotValidBefore(now.minusDays(1))
                .withNotValidAfter(now.plusDays(1))
                .withType(types)
                .withSubtype(subtypes)
                .withLevel(level)
                .withProduct(product)
                .withCaptureDevice(captureDevice)
                .withFeatureExtractionAlgorithm(featureAlgo)
                .withComparisonAlgorithm(comparisonAlgo)
                .withCompressionAlgorithm(compressionAlgo)
                .withPurpose(purpose)
                .withQuality(quality)
                .build();

        assertArrayEquals(challengeResponse, info.getChallengeResponse());
        assertEquals(index, info.getIndex());
        assertEquals(format, info.getFormat());
        assertEquals(encryption, info.getEncryption());
        assertEquals(now, info.getCreationDate());
        assertEquals(now.minusDays(1), info.getNotValidBefore());
        assertEquals(now.plusDays(1), info.getNotValidAfter());
        assertEquals(types, info.getType());
        assertEquals(subtypes, info.getSubtype());
        assertEquals(level, info.getLevel());
        assertEquals(product, info.getProduct());
        assertEquals(captureDevice, info.getCaptureDevice());
        assertEquals(featureAlgo, info.getFeatureExtractionAlgorithm());
        assertEquals(comparisonAlgo, info.getComparisonAlgorithm());
        assertEquals(compressionAlgo, info.getCompressionAlgorithm());
        assertEquals(purpose, info.getPurpose());
        assertEquals(quality, info.getQuality());
    }

    /**
     * Verifies equals and hashCode for multiple branches.
     */
    @Test
    void verifyEqualsAndHashCode() {
        BDBInfo info1 = new BDBInfo.BDBInfoBuilder().withIndex("IDX").build();
        BDBInfo info2 = new BDBInfo.BDBInfoBuilder().withIndex("IDX").build();
        BDBInfo info3 = new BDBInfo.BDBInfoBuilder().withIndex("DIFF").build();

        assertEquals(info1, info2);
        assertEquals(info1.hashCode(), info2.hashCode());
        assertEquals(info1, info1);
        assertNotEquals(info1, null);
        assertNotEquals(info1, "string");
        assertNotEquals(info1, info3);
        assertNotEquals(info1.hashCode(), info3.hashCode());
    }

    /**
     * Verifies toString produces a non-empty representation.
     */
    @Test
    void verifyToString() {
        BDBInfo info = new BDBInfo.BDBInfoBuilder().withIndex("123").build();
        String out = info.toString();
        assertNotNull(out);
        assertTrue(out.contains("123"));
    }

    /**
     * Verifies builder behaves correctly when all values are null.
     */
    @Test
    void verifyBuilderWithNulls() {
        BDBInfo info = new BDBInfo.BDBInfoBuilder()
                .withChallengeResponse(null)
                .withIndex(null)
                .withFormat(null)
                .withEncryption(null)
                .withCreationDate(null)
                .withNotValidBefore(null)
                .withNotValidAfter(null)
                .withType(null)
                .withSubtype(null)
                .withLevel(null)
                .withProduct(null)
                .withCaptureDevice(null)
                .withFeatureExtractionAlgorithm(null)
                .withComparisonAlgorithm(null)
                .withCompressionAlgorithm(null)
                .withPurpose(null)
                .withQuality(null)
                .build();

        assertNull(info.getIndex());
        assertNull(info.getFormat());
        assertNull(info.getCaptureDevice());
    }
}