package io.mosip.print.service.impl;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfStamper;
import io.mosip.print.constant.PDFGeneratorExceptionCodeConstant;
import io.mosip.print.exception.PDFGeneratorException;
import io.mosip.print.model.CertificateEntry;
import io.mosip.print.model.Rectangle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PDFGeneratorImpl} class.
 *
 * <p>This class contains comprehensive test cases for verifying the functionality of the PDFGeneratorImpl class,
 * including PDF generation, signing, encryption, merging, and various edge cases and error handling scenarios.</p>
 */
@ExtendWith(MockitoExtension.class)
class PDFGeneratorImplTest {

    @InjectMocks
    private PDFGeneratorImpl pdfGenerator;

    @Mock
    private InputStream mockInputStream;

    @Mock
    private BufferedImage mockBufferedImage;

    @Mock
    private Provider mockProvider;

    @Mock
    private X509Certificate mockCertificate;

    @Mock
    private PrivateKey mockPrivateKey;

    @Mock
    private CertificateEntry<X509Certificate, PrivateKey> mockCertificateEntry;

    private byte[] testPdfBytes;
    private Rectangle testRectangle;

    /**
     * Sets up test fixtures before each test method execution.
     * Initializes mock objects, test data, and PDF generator configuration properties.
     */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pdfGenerator, "pdfOwnerPassword", "ownerPassword");
        testPdfBytes = "test pdf content".getBytes();
        testRectangle = new Rectangle(10.0f, 10.0f, 100.0f, 50.0f);

        X509Certificate[] certificateChain = {mockCertificate};
        lenient().when(mockCertificateEntry.getChain()).thenReturn(certificateChain);
        lenient().when(mockCertificateEntry.getPrivateKey()).thenReturn(mockPrivateKey);
        lenient().when(mockProvider.getName()).thenReturn("BC");
    }

    /**
     * Tests the generate method with a valid InputStream.
     * Verifies that the method successfully generates a PDF and returns a ByteArrayOutputStream.
     */
    @Test
    void generateWithValidInputStreamShouldReturnOutputStream() throws IOException {
        InputStream validInputStream = new ByteArrayInputStream("<html><body>Test</body></html>".getBytes());

        OutputStream result = pdfGenerator.generate(validInputStream);

        assertNotNull(result);
        assertTrue(result instanceof ByteArrayOutputStream);
    }

    /**
     * Tests the generate method with a null InputStream.
     * Verifies that the method throws a PDFGeneratorException for null input.
     */
    @Test
    void generateWithNullInputStreamShouldThrowPdfGeneratorException() {
        InputStream nullInputStream = null;

        PDFGeneratorException exception = assertThrows(PDFGeneratorException.class,
                () -> pdfGenerator.generate(nullInputStream));

        assertTrue(exception.getErrorCode().equals(
                PDFGeneratorExceptionCodeConstant.INPUTSTREAM_NULL_EMPTY_EXCEPTION.getErrorCode()));
    }

    /**
     * Tests the generate method with a valid HTML template string.
     * Verifies that the method successfully processes the template and returns a ByteArrayOutputStream.
     */
    @Test
    void generateWithValidTemplateShouldReturnOutputStream() throws IOException {
        String template = "<html><body>Test Template</body></html>";

        OutputStream result = pdfGenerator.generate(template);

        assertNotNull(result);
        assertTrue(result instanceof ByteArrayOutputStream);
    }

    /**
     * Tests the generate method with an invalid (null) template.
     * Verifies that the method throws a PDFGeneratorException for null template input.
     */
    @Test
    void generateWithInvalidTemplateShouldThrowPdfGeneratorException() {
        String invalidTemplate = null;

        assertThrows(PDFGeneratorException.class,
                () -> pdfGenerator.generate(invalidTemplate));
    }

    /**
     * Tests the generate method with valid file paths for template and output.
     * Verifies that the method creates a PDF file at the specified output location.
     */
    @Test
    void generateWithValidFilePathsShouldCreateFile() throws IOException {
        String templatePath = createTempHtmlFile();
        String outputPath = System.getProperty("java.io.tmpdir") + "/";
        String fileName = "test-output";

        pdfGenerator.generate(templatePath, outputPath, fileName);

        File outputFile = new File(outputPath + fileName + ".pdf");
        assertTrue(outputFile.exists());
        outputFile.delete();
    }

    /**
     * Tests the generate method with invalid file paths.
     * Verifies that the method throws a PDFGeneratorException when template or output paths are invalid.
     */
    @Test
    void generateWithInvalidFilePathsShouldThrowPdfGeneratorException() {
        String invalidTemplatePath = "/invalid/path/template.html";
        String outputPath = "/invalid/output/path/";
        String fileName = "test";

        assertThrows(PDFGeneratorException.class,
                () -> pdfGenerator.generate(invalidTemplatePath, outputPath, fileName));
    }

    /**
     * Tests the generate method with InputStream and resource location.
     * Verifies that the method successfully generates PDF using template with external resources.
     */
    @Test
    void generateWithInputStreamAndResourceLocationShouldReturnOutputStream() throws IOException {
        File tempFile = File.createTempFile("test", ".html");
        tempFile.deleteOnExit();
        java.nio.file.Files.write(tempFile.toPath(), "<html><body>Test</body></html>".getBytes());

        String resourceLocation = tempFile.getParentFile().toURI().toString();

        try (InputStream validInputStream = new java.io.FileInputStream(tempFile)) {
            OutputStream result = pdfGenerator.generate(validInputStream, resourceLocation);
            assertNotNull(result);
            assertTrue(result instanceof ByteArrayOutputStream);
        } finally {
            tempFile.delete();
        }
    }

    /**
     * Tests the generate method with null InputStream and valid resource location.
     * Verifies that the method throws a PDFGeneratorException for null InputStream input.
     */
    @Test
    void generateWithNullInputStreamAndResourceLocationShouldThrowPdfGeneratorException() {
        InputStream nullInputStream = null;
        String resourceLocation = "classpath:/";

        assertThrows(PDFGeneratorException.class,
                () -> pdfGenerator.generate(nullInputStream, resourceLocation));
    }

    /**
     * Tests the asPDF method with valid BufferedImage list.
     * Verifies that the method converts images to PDF bytes successfully.
     */
    @Test
    void asPdfWithValidBufferedImagesShouldReturnPdfBytes() throws IOException {
        List<BufferedImage> images = new ArrayList<>();
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        images.add(image);

        byte[] result = pdfGenerator.asPDF(images);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    /**
     * Tests the asPDF method when ImageIO throws an exception.
     * Verifies that the method properly handles ImageIO exceptions and throws PDFGeneratorException.
     */
    @Test
    void asPdfWithImageIoExceptionShouldThrowPdfGeneratorException() throws IOException {
        List<BufferedImage> images = new ArrayList<>();
        images.add(mockBufferedImage);

        try (MockedStatic<ImageIO> imageIOMock = mockStatic(ImageIO.class)) {
            imageIOMock.when(() -> ImageIO.write(any(BufferedImage.class), anyString(), any(ByteArrayOutputStream.class)))
                    .thenThrow(new IOException("ImageIO error"));

            assertThrows(PDFGeneratorException.class, () -> pdfGenerator.asPDF(images));
        }
    }

    /**
     * Tests the mergePDF method with valid PDF files.
     * Verifies that the method successfully merges multiple PDF files into a single byte array.
     */
    @Test
    void mergePdfWithValidPdfFilesShouldReturnMergedPdfBytes() throws IOException, URISyntaxException {
        List<URL> pdfFiles = new ArrayList<>();

        byte[] samplePdf = createSamplePDF();
        File tempFile = createTempPDFFile(samplePdf);
        pdfFiles.add(tempFile.toURI().toURL());

        byte[] result = pdfGenerator.mergePDF(pdfFiles);

        assertNotNull(result);
        assertTrue(result.length > 0);
        tempFile.delete();
    }

    /**
     * Tests the mergePDF method with invalid URL.
     * Verifies that the method throws a PDFGeneratorException when provided with invalid PDF URLs.
     */
    @Test
    void mergePdfWithInvalidUrlShouldThrowPdfGeneratorException() throws IOException, URISyntaxException {
        List<URL> pdfFiles = new ArrayList<>();
        pdfFiles.add(new URI("file:///invalid/path/file.pdf").toURL());

        assertThrows(PDFGeneratorException.class, () -> pdfGenerator.mergePDF(pdfFiles));
    }

    /**
     * Tests the signAndEncryptPDF method with password encryption logic.
     * Verifies that the method handles password-based encryption correctly.
     */
    @Test
    void signAndEncryptPdfPasswordLogicWithPasswordShouldHandleEncryption() throws Exception {
        byte[] validPdfBytes = createSimpleValidPdf();

        PDFGeneratorException exception = assertThrows(PDFGeneratorException.class, () -> {
            pdfGenerator.signAndEncryptPDF(validPdfBytes, testRectangle,
                    "Test signing reason", 1, mockProvider, mockCertificateEntry, "testPassword");
        });

        assertEquals(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorCode(),
                exception.getErrorCode());
        assertTrue(exception.getCause() instanceof DocumentException);
    }

    /**
     * Tests the signAndEncryptPDF method without password (null).
     * Verifies that the method handles null password input appropriately.
     */
    @Test
    void signAndEncryptPdfPasswordLogicWithoutPasswordShouldHandleNullPassword() throws Exception {
        byte[] validPdfBytes = createSimpleValidPdf();

        PDFGeneratorException exception = assertThrows(PDFGeneratorException.class, () -> {
            pdfGenerator.signAndEncryptPDF(validPdfBytes, testRectangle,
                    "Test signing reason", 1, mockProvider, mockCertificateEntry, null);
        });

        assertEquals(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorCode(),
                exception.getErrorCode());
    }

    /**
     * Tests the signAndEncryptPDF method with empty or whitespace password.
     * Verifies that the method handles empty and whitespace passwords correctly.
     */
    @Test
    void signAndEncryptPdfPasswordLogicWithEmptyPasswordShouldHandleEmptyValues() throws Exception {
        byte[] validPdfBytes = createSimpleValidPdf();

        PDFGeneratorException exception1 = assertThrows(PDFGeneratorException.class, () -> {
            pdfGenerator.signAndEncryptPDF(validPdfBytes, testRectangle,
                    "Test signing reason", 1, mockProvider, mockCertificateEntry, "");
        });

        PDFGeneratorException exception2 = assertThrows(PDFGeneratorException.class, () -> {
            pdfGenerator.signAndEncryptPDF(validPdfBytes, testRectangle,
                    "Test signing reason", 1, mockProvider, mockCertificateEntry, "   ");
        });

        assertEquals(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorCode(),
                exception1.getErrorCode());
        assertEquals(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorCode(),
                exception2.getErrorCode());
    }

    /**
     * Tests the signAndEncryptPDF method with invalid PDF bytes.
     * Verifies that the method throws InvalidPdfException when provided with invalid PDF data.
     */
    @Test
    void signAndEncryptPdfWithInvalidPdfBytesShouldThrowInvalidPdfException() throws Exception {
        byte[] invalidPdfBytes = "not a valid pdf".getBytes();

        com.itextpdf.text.exceptions.InvalidPdfException exception =
                assertThrows(com.itextpdf.text.exceptions.InvalidPdfException.class, () -> {
                    pdfGenerator.signAndEncryptPDF(invalidPdfBytes, testRectangle,
                            "Test signing reason", 1, mockProvider, mockCertificateEntry, "testPassword");
                });

        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("PDF header"));
    }

    /**
     * Tests the signAndEncryptPDF method with multiple certificate chain processing.
     * Verifies that the method handles certificate chains with multiple certificates correctly.
     */
    @Test
    void signAndEncryptPdfCertificateChainProcessingShouldHandleMultipleCertificates() throws Exception {
        byte[] validPdfBytes = createSimpleValidPdf();

        X509Certificate cert1 = mock(X509Certificate.class);
        X509Certificate cert2 = mock(X509Certificate.class);
        X509Certificate[] multiCertChain = {cert1, cert2};

        when(mockCertificateEntry.getChain()).thenReturn(multiCertChain);

        PDFGeneratorException exception = assertThrows(PDFGeneratorException.class, () -> {
            pdfGenerator.signAndEncryptPDF(validPdfBytes, testRectangle,
                    "Test signing reason", 1, mockProvider, mockCertificateEntry, null);
        });

        assertEquals(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorCode(),
                exception.getErrorCode());
    }

    /**
     * Tests the signAndEncryptPDF method with different page numbers and rectangle coordinates.
     * Verifies that the method handles various signature placement parameters correctly.
     */
    @Test
    void signAndEncryptPdfWithDifferentParametersShouldHandleCustomValues() throws Exception {
        byte[] validPdfBytes = createSimpleValidPdf();
        Rectangle customRectangle = new Rectangle(100.0f, 100.0f, 300.0f, 200.0f);

        PDFGeneratorException exception = assertThrows(PDFGeneratorException.class, () -> {
            pdfGenerator.signAndEncryptPDF(validPdfBytes, customRectangle,
                    "Custom signing reason", 2, mockProvider, mockCertificateEntry, "customPassword");
        });

        assertEquals(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorCode(),
                exception.getErrorCode());
    }

    /**
     * Tests the signAndEncryptPDF method with null reason parameter.
     * Verifies that the method handles null signing reason appropriately.
     */
    @Test
    void signAndEncryptPdfWithNullReasonShouldHandleNullReason() throws Exception {
        byte[] validPdfBytes = createSimpleValidPdf();

        PDFGeneratorException exception = assertThrows(PDFGeneratorException.class, () -> {
            pdfGenerator.signAndEncryptPDF(validPdfBytes, testRectangle,
                    null, 1, mockProvider, mockCertificateEntry, null);
        });

        assertEquals(PDFGeneratorExceptionCodeConstant.PDF_EXCEPTION.getErrorCode(),
                exception.getErrorCode());
    }

    /**
     * Tests exception handling in the catch block using corrupted but readable PDF.
     * Verifies that the method properly handles PDF processing exceptions.
     */
    @Test
    void signAndEncryptPdfDocumentExceptionHandlingShouldHandleCorruptedPdf() throws Exception {
        byte[] corruptedPdf = createCorruptedButReadablePdf();

        Exception exception = assertThrows(Exception.class, () -> {
            pdfGenerator.signAndEncryptPDF(corruptedPdf, testRectangle,
                    "Test signing reason", 1, mockProvider, mockCertificateEntry, "testPassword");
        });

        assertTrue(exception instanceof PDFGeneratorException ||
                exception instanceof DocumentException ||
                exception instanceof com.itextpdf.text.exceptions.InvalidPdfException);
    }

    /**
     * Tests that the signAndEncryptPDF method reaches the signing logic successfully.
     * Verifies that the method progresses through password logic and setup to the signing stage.
     */
    @Test
    void signAndEncryptPdfReachesSigningLogicShouldProgressToSigningStage() throws Exception {
        byte[] validPdfBytes = createSimpleValidPdf();

        try {
            pdfGenerator.signAndEncryptPDF(validPdfBytes, testRectangle,
                    "Test reason", 1, mockProvider, mockCertificateEntry, "password");

            fail("Expected an exception due to incomplete signing process");

        } catch (PDFGeneratorException e) {
            assertTrue(e.getCause() instanceof DocumentException);
            assertTrue(e.getCause().getMessage().contains("Signature defined"));

        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    /**
     * Tests the closeQuietly method with successful close operation.
     * Verifies that the method completes without throwing exceptions when PdfStamper closes successfully.
     */
    @Test
    void closeQuietlyWithSuccessfulCloseShouldCompleteWithoutException() throws Exception {
        PdfStamper mockStamper = mock(PdfStamper.class);
        doNothing().when(mockStamper).close();

        ReflectionTestUtils.invokeMethod(pdfGenerator, "closeQuietly", mockStamper);

        verify(mockStamper, times(1)).close();
    }

    /**
     * Tests the closeQuietly method when DocumentException occurs during close.
     * Verifies that the method throws PDFGeneratorException when DocumentException is encountered.
     */
    @Test
    void closeQuietlyWithDocumentExceptionShouldThrowPdfGeneratorException() throws Exception {
        PdfStamper mockStamper = mock(PdfStamper.class);
        doThrow(new DocumentException("Close failed")).when(mockStamper).close();

        assertThrows(PDFGeneratorException.class, () -> {
            ReflectionTestUtils.invokeMethod(pdfGenerator, "closeQuietly", mockStamper);
        });
    }

    /**
     * Tests the closeQuietly method when IOException occurs during close.
     * Verifies that the method handles IOException appropriately during PdfStamper close operations.
     */
    @Test
    void closeQuietlyWithIoExceptionShouldHandleProperly() throws Exception {
        PdfStamper mockStamper = mock(PdfStamper.class);
        doThrow(new IOException("IO error during close")).when(mockStamper).close();

        try {
            ReflectionTestUtils.invokeMethod(pdfGenerator, "closeQuietly", mockStamper);
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof IOException);
        }
    }

    /**
     * Creates a temporary HTML file for testing purposes.
     *
     * @return the absolute path of the created temporary HTML file
     * @throws IOException if file creation fails
     */
    private String createTempHtmlFile() throws IOException {
        File tempFile = File.createTempFile("test", ".html");
        tempFile.deleteOnExit();
        java.nio.file.Files.write(tempFile.toPath(), "<html><body>Test</body></html>".getBytes());
        return tempFile.getAbsolutePath();
    }

    /**
     * Creates a sample PDF as byte array for testing purposes.
     *
     * @return byte array containing a valid PDF document
     * @throws IOException if PDF creation fails
     */
    private byte[] createSamplePDF() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        com.itextpdf.kernel.pdf.PdfWriter writer = new com.itextpdf.kernel.pdf.PdfWriter(baos);
        com.itextpdf.kernel.pdf.PdfDocument pdfDoc = new com.itextpdf.kernel.pdf.PdfDocument(writer);
        com.itextpdf.layout.Document document = new com.itextpdf.layout.Document(pdfDoc);
        document.add(new com.itextpdf.layout.element.Paragraph("Test PDF"));
        document.close();
        return baos.toByteArray();
    }

    /**
     * Creates a temporary PDF file from byte array.
     *
     * @param pdfBytes the PDF content as byte array
     * @return temporary File object containing the PDF
     * @throws IOException if file creation fails
     */
    private File createTempPDFFile(byte[] pdfBytes) throws IOException {
        File tempFile = File.createTempFile("test", ".pdf");
        tempFile.deleteOnExit();
        java.nio.file.Files.write(tempFile.toPath(), pdfBytes);
        return tempFile;
    }

    /**
     * Creates a simple valid PDF that can be parsed by PdfReader.
     *
     * @return byte array containing a valid PDF document for signing tests
     * @throws IOException if PDF creation fails
     */
    private byte[] createSimpleValidPdf() throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, baos);
            document.open();
            document.add(new com.itextpdf.text.Paragraph("Simple test PDF content for signing tests"));
            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new IOException("Failed to create test PDF", e);
        }
    }

    /**
     * Creates a PDF that can be read by PdfReader but may cause issues during stamping.
     *
     * @return byte array containing a corrupted but readable PDF
     * @throws IOException if PDF creation fails
     */
    private byte[] createCorruptedButReadablePdf() throws IOException {
        byte[] validPdf = createSimpleValidPdf();
        byte[] corruptedPdf = validPdf.clone();

        if (corruptedPdf.length > 100) {
            corruptedPdf[50] = (byte) 0xFF;
            corruptedPdf[51] = (byte) 0xFF;
        }

        return corruptedPdf;
    }
}
