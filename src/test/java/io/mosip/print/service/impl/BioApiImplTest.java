package io.mosip.print.service.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.mosip.print.constant.QualityType;
import io.mosip.print.entity.BDBInfo;
import io.mosip.print.entity.BIR;
import io.mosip.print.entity.MatchDecision;
import io.mosip.print.model.KeyValuePair;
import io.mosip.print.model.QualityScore;
import io.mosip.print.model.Response;

/**
 * Test class for {@link BioApiImpl}
 */
@ExtendWith(MockitoExtension.class)
class BioApiImplTest {

    @InjectMocks
    private BioApiImpl bioApi;

    @Mock
    private BIR sampleBir;

    @Mock
    private BDBInfo bdbInfo;

    private BIR[] gallery;
    private KeyValuePair[] flags;
    private static final byte[] SAMPLE_BDB = {1, 2, 3, 4, 5};
    private static final byte[] DIFFERENT_BDB = {5, 4, 3, 2, 1};

    /**
     * Sets up test fixtures before each test method execution.
     * Initializes the gallery array and flags array for testing.
     */
    @BeforeEach
    void setUp() {
        gallery = new BIR[2];
        gallery[0] = new BIR();
        gallery[1] = new BIR();
        flags = new KeyValuePair[0];
    }

    /**
     * Tests the checkQuality method with valid input containing quality information.
     * Verifies that the method returns a proper quality score response when BDB info and quality are available.
     */
    @Test
    void checkQualityWithValidInputShouldReturnQualityScore() {
        QualityType qualityType = new QualityType();
        qualityType.setScore(85L);
        when(sampleBir.getBdbInfo()).thenReturn(bdbInfo);
        when(bdbInfo.getQuality()).thenReturn(qualityType);

        Response<QualityScore> response = bioApi.checkQuality(sampleBir, flags);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getResponse());
        assertEquals(85, response.getResponse().getScore());
    }

    /**
     * Tests the checkQuality method when BDB info is null.
     * Verifies that the method handles null BDB info gracefully and returns a zero quality score.
     */
    @Test
    void checkQualityWithNullBdbInfoShouldReturnZeroScore() {
        when(sampleBir.getBdbInfo()).thenReturn(null);

        Response<QualityScore> response = bioApi.checkQuality(sampleBir, flags);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals(0, response.getResponse().getScore());
    }

    /**
     * Tests the match method with biometric data that has matching entries in the gallery.
     * Verifies that the method correctly identifies matches and non-matches in the gallery.
     */
    @Test
    void matchWithMatchingBdbShouldReturnMatchDecision() {
        when(sampleBir.getBdb()).thenReturn(SAMPLE_BDB);
        gallery[0].setBdb(SAMPLE_BDB);
        gallery[1].setBdb(DIFFERENT_BDB);

        Response<MatchDecision[]> response = bioApi.match(sampleBir, gallery, flags);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertArrayEquals(new Boolean[]{true, false},
                new Boolean[]{
                        response.getResponse()[0].isMatch(),
                        response.getResponse()[1].isMatch()
                });
    }

    /**
     * Tests the match method with a null gallery parameter.
     * Verifies that the method throws a NullPointerException when gallery is null.
     */
    @Test
    void matchWithNullGalleryShouldThrowNPE() {
        assertThrows(NullPointerException.class, () -> {
            bioApi.match(sampleBir, null, flags);
        });
    }

    /**
     * Tests the match method when the sample BIR has null biometric data.
     * Verifies that the method returns false for all gallery comparisons when sample data is null.
     */
    @Test
    void matchWithNullSampleBdbShouldReturnAllFalse() {
        when(sampleBir.getBdb()).thenReturn(null);
        gallery[0].setBdb(SAMPLE_BDB);
        gallery[1].setBdb(DIFFERENT_BDB);

        Response<MatchDecision[]> response = bioApi.match(sampleBir, gallery, flags);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertArrayEquals(new Boolean[]{false, false},
                new Boolean[]{
                        response.getResponse()[0].isMatch(),
                        response.getResponse()[1].isMatch()
                });
    }

    /**
     * Tests the extractTemplate method functionality.
     * Verifies that the method returns the same BIR object that was passed as input.
     */
    @Test
    void extractTemplateShouldReturnSameBir() {
        Response<BIR> response = bioApi.extractTemplate(sampleBir, flags);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertSame(sampleBir, response.getResponse());
    }

    /**
     * Tests the segment method functionality.
     * Verifies that the method returns a single-element array containing the input BIR.
     */
    @Test
    void segmentShouldReturnSingleElementArray() {
        Response<BIR[]> response = bioApi.segment(sampleBir, flags);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals(1, response.getResponse().length);
        assertSame(sampleBir, response.getResponse()[0]);
    }

    /**
     * Tests the match method with an empty gallery array.
     * Verifies that the method handles empty galleries correctly and returns an empty match decision array.
     */
    @Test
    void matchWithEmptyGalleryShouldReturnEmptyMatchDecision() {
        BIR[] emptyGallery = new BIR[0];

        Response<MatchDecision[]> response = bioApi.match(sampleBir, emptyGallery, flags);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals(0, response.getResponse().length);
    }

    /**
     * Tests the match method when some gallery entries have null biometric data.
     * Verifies that the method handles null BDB entries in the gallery gracefully.
     */
    @Test
    void matchWithNullBdbInGalleryShouldHandleGracefully() {
        when(sampleBir.getBdb()).thenReturn(SAMPLE_BDB);
        gallery[0].setBdb(null);
        gallery[1].setBdb(SAMPLE_BDB);

        Response<MatchDecision[]> response = bioApi.match(sampleBir, gallery, flags);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertArrayEquals(new Boolean[]{false, true},
                new Boolean[]{
                        response.getResponse()[0].isMatch(),
                        response.getResponse()[1].isMatch()
                });
    }

    /**
     * Tests the checkQuality method when quality information is null.
     * Verifies that the method handles null quality data and returns a zero quality score.
     */
    @Test
    void checkQualityWithNullQualityShouldReturnZeroScore() {
        when(sampleBir.getBdbInfo()).thenReturn(bdbInfo);
        when(bdbInfo.getQuality()).thenReturn(null);

        Response<QualityScore> response = bioApi.checkQuality(sampleBir, flags);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals(0, response.getResponse().getScore());
    }
}
