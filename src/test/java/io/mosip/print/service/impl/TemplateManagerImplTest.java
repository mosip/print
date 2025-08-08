package io.mosip.print.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.mosip.print.exception.TemplateMethodInvocationException;
import io.mosip.print.exception.TemplateParsingException;
import io.mosip.print.exception.TemplateResourceNotFoundException;
import io.mosip.print.util.TemplateManagerUtil;

/**
 * Unit tests for {@link TemplateManagerImpl} class.
 *
 * <p>This class contains comprehensive test cases for verifying the functionality of the TemplateManagerImpl class,
 * including template merging operations, exception handling, null parameter validation, and Velocity engine integration.</p>
 */
@ExtendWith(MockitoExtension.class)
class TemplateManagerImplTest {

    private TemplateManagerImpl templateManager;

    @Mock
    private VelocityEngine velocityEngine;

    @Mock
    private Template template;

    @Mock
    private VelocityContext velocityContext;

    private Map<String, Object> testValues;
    private InputStream testInputStream;
    private StringWriter testWriter;

    /**
     * Sets up test fixtures before each test method execution.
     * Initializes the TemplateManagerImpl instance, test data, and mock objects required for testing.
     */
    @BeforeEach
    void setUp() {
        templateManager = new TemplateManagerImpl(velocityEngine);
        testValues = new HashMap<>();
        testValues.put("name", "TestUser");
        testValues.put("id", "12345");

        String testContent = "Hello $name, your ID is $id";
        testInputStream = new ByteArrayInputStream(testContent.getBytes());
        testWriter = new StringWriter();
    }

    /**
     * Tests the merge method with InputStream and valid parameters.
     * Verifies that template merging returns a valid InputStream when provided with proper input stream and values.
     */
    @Test
    void mergeWithInputStreamAndValidParametersShouldReturnInputStream() throws IOException {
        try (MockedStatic<TemplateManagerUtil> mockedUtil = Mockito.mockStatic(TemplateManagerUtil.class)) {
            mockedUtil.when(() -> TemplateManagerUtil.bindInputToContext(testValues))
                    .thenReturn(velocityContext);

            when(velocityEngine.evaluate(eq(velocityContext), any(StringWriter.class),
                    eq("templateManager-mergeTemplate"), any(InputStreamReader.class))).thenReturn(true);

            InputStream result = templateManager.merge(testInputStream, testValues);

            assertNotNull(result);
            verify(velocityEngine, times(1)).evaluate(eq(velocityContext), any(StringWriter.class),
                    eq("templateManager-mergeTemplate"), any(InputStreamReader.class));
        }
    }

    /**
     * Tests the merge method with InputStream when context is null.
     * Verifies that the method returns null when the velocity context cannot be created from input values.
     */
    @Test
    void mergeWithInputStreamAndNullContextShouldReturnNull() throws IOException {
        try (MockedStatic<TemplateManagerUtil> mockedUtil = Mockito.mockStatic(TemplateManagerUtil.class)) {
            mockedUtil.when(() -> TemplateManagerUtil.bindInputToContext(testValues))
                    .thenReturn(null);

            InputStream result = templateManager.merge(testInputStream, testValues);

            assertNull(result);
        }
    }

    /**
     * Tests the merge method with InputStream when evaluate returns false.
     * Verifies that the method returns null when the Velocity engine evaluation fails.
     */
    @Test
    void mergeWithInputStreamAndEvaluateReturnsFalseShouldReturnNull() throws IOException {
        try (MockedStatic<TemplateManagerUtil> mockedUtil = Mockito.mockStatic(TemplateManagerUtil.class)) {
            mockedUtil.when(() -> TemplateManagerUtil.bindInputToContext(testValues))
                    .thenReturn(velocityContext);

            when(velocityEngine.evaluate(eq(velocityContext), any(StringWriter.class),
                    eq("templateManager-mergeTemplate"), any(InputStreamReader.class))).thenReturn(false);

            InputStream result = templateManager.merge(testInputStream, testValues);

            assertNull(result);
        }
    }

    /**
     * Tests the merge method with null InputStream parameter.
     * Verifies that the method throws NullPointerException when InputStream is null.
     */
    @Test
    void mergeWithNullInputStreamShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            templateManager.merge((InputStream) null, testValues);
        });
    }

    /**
     * Tests the merge method with null values parameter.
     * Verifies that the method throws NullPointerException when values map is null.
     */
    @Test
    void mergeWithNullValuesShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            templateManager.merge(testInputStream, null);
        });
    }

    /**
     * Tests the merge method with InputStream when ResourceNotFoundException occurs.
     * Verifies that ResourceNotFoundException is wrapped and thrown as TemplateResourceNotFoundException.
     */
    @Test
    void mergeWithInputStreamAndResourceNotFoundExceptionShouldThrowTemplateResourceNotFoundException() {
        try (MockedStatic<TemplateManagerUtil> mockedUtil = Mockito.mockStatic(TemplateManagerUtil.class)) {
            mockedUtil.when(() -> TemplateManagerUtil.bindInputToContext(testValues))
                    .thenReturn(velocityContext);

            when(velocityEngine.evaluate(eq(velocityContext), any(StringWriter.class),
                    eq("templateManager-mergeTemplate"), any(InputStreamReader.class)))
                    .thenThrow(new ResourceNotFoundException("Template not found"));

            assertThrows(TemplateResourceNotFoundException.class, () -> {
                templateManager.merge(testInputStream, testValues);
            });
        }
    }

    /**
     * Tests the merge method with InputStream when ParseErrorException occurs.
     * Verifies that ParseErrorException is wrapped and thrown as TemplateParsingException.
     */
    @Test
    void mergeWithInputStreamAndParseErrorExceptionShouldThrowTemplateParsingException() {
        try (MockedStatic<TemplateManagerUtil> mockedUtil = Mockito.mockStatic(TemplateManagerUtil.class)) {
            mockedUtil.when(() -> TemplateManagerUtil.bindInputToContext(testValues))
                    .thenReturn(velocityContext);

            when(velocityEngine.evaluate(eq(velocityContext), any(StringWriter.class),
                    eq("templateManager-mergeTemplate"), any(InputStreamReader.class)))
                    .thenThrow(new ParseErrorException("Parse error"));

            assertThrows(TemplateParsingException.class, () -> {
                templateManager.merge(testInputStream, testValues);
            });
        }
    }

    /**
     * Tests the merge method with InputStream when MethodInvocationException occurs.
     * Verifies that MethodInvocationException is wrapped and thrown as TemplateMethodInvocationException.
     */
    @Test
    void mergeWithInputStreamAndMethodInvocationExceptionShouldThrowTemplateMethodInvocationException() {
        try (MockedStatic<TemplateManagerUtil> mockedUtil = Mockito.mockStatic(TemplateManagerUtil.class)) {
            mockedUtil.when(() -> TemplateManagerUtil.bindInputToContext(testValues))
                    .thenReturn(velocityContext);

            when(velocityEngine.evaluate(eq(velocityContext), any(StringWriter.class),
                    eq("templateManager-mergeTemplate"), any(InputStreamReader.class)))
                    .thenThrow(new MethodInvocationException("Method invocation error", new Exception(), "test", "template", 1, 1));

            assertThrows(TemplateMethodInvocationException.class, () -> {
                templateManager.merge(testInputStream, testValues);
            });
        }
    }

    /**
     * Tests the merge method with template name and writer using default encoding.
     * Verifies that template merging works correctly when using template name with StringWriter.
     */
    @Test
    void mergeWithTemplateNameAndWriterShouldReturnTrue() throws IOException {
        try (MockedStatic<TemplateManagerUtil> mockedUtil = Mockito.mockStatic(TemplateManagerUtil.class)) {
            mockedUtil.when(() -> TemplateManagerUtil.bindInputToContext(testValues))
                    .thenReturn(velocityContext);

            when(velocityEngine.getTemplate(eq("test-template"), eq("UTF-8"))).thenReturn(template);
            doNothing().when(template).merge(velocityContext, testWriter);

            boolean result = templateManager.merge("test-template", testWriter, testValues);

            assertTrue(result);
            verify(velocityEngine, times(1)).getTemplate("test-template", "UTF-8");
            verify(template, times(1)).merge(velocityContext, testWriter);
        }
    }

    /**
     * Tests the merge method with template name, writer, and custom encoding.
     * Verifies that template merging works correctly when using custom encoding type.
     */
    @Test
    void mergeWithTemplateNameWriterAndEncodingShouldReturnTrue() throws IOException {
        try (MockedStatic<TemplateManagerUtil> mockedUtil = Mockito.mockStatic(TemplateManagerUtil.class)) {
            mockedUtil.when(() -> TemplateManagerUtil.bindInputToContext(testValues))
                    .thenReturn(velocityContext);

            when(velocityEngine.getTemplate(eq("test-template"), eq("ISO-8859-1"))).thenReturn(template);
            doNothing().when(template).merge(velocityContext, testWriter);

            boolean result = templateManager.merge("test-template", testWriter, testValues, "ISO-8859-1");

            assertTrue(result);
            verify(velocityEngine, times(1)).getTemplate("test-template", "ISO-8859-1");
            verify(template, times(1)).merge(velocityContext, testWriter);
        }
    }

    /**
     * Tests the merge method with null template name parameter.
     * Verifies that the method throws NullPointerException when template name is null.
     */
    @Test
    void mergeWithNullTemplateNameShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            templateManager.merge((String) null, testWriter, testValues, "UTF-8");
        });
    }

    /**
     * Tests the merge method with null writer parameter.
     * Verifies that the method throws NullPointerException when StringWriter is null.
     */
    @Test
    void mergeWithNullWriterShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            templateManager.merge("test-template", null, testValues, "UTF-8");
        });
    }

    /**
     * Tests the merge method with null encoding type parameter.
     * Verifies that the method throws NullPointerException when encoding type is null.
     */
    @Test
    void mergeWithNullEncodingTypeShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            templateManager.merge("test-template", testWriter, testValues, null);
        });
    }

    /**
     * Tests the merge method with null values parameter for template name variant.
     * Verifies that the method throws NullPointerException when values map is null.
     */
    @Test
    void mergeWithNullValuesForTemplateNameShouldThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> {
            templateManager.merge("test-template", testWriter, null, "UTF-8");
        });
    }

    /**
     * Tests the merge method with template name when ResourceNotFoundException occurs.
     * Verifies that ResourceNotFoundException is wrapped and thrown as TemplateResourceNotFoundException.
     */
    @Test
    void mergeWithTemplateNameAndResourceNotFoundExceptionShouldThrowTemplateResourceNotFoundException() {
        when(velocityEngine.getTemplate(eq("test-template"), eq("UTF-8")))
                .thenThrow(new ResourceNotFoundException("Template not found"));

        assertThrows(TemplateResourceNotFoundException.class, () -> {
            templateManager.merge("test-template", testWriter, testValues, "UTF-8");
        });
    }

    /**
     * Tests the merge method with template name when ParseErrorException occurs during template merge.
     * Verifies that ParseErrorException is wrapped and thrown as TemplateParsingException.
     */
    @Test
    void mergeWithTemplateNameAndParseErrorExceptionShouldThrowTemplateParsingException() {
        try (MockedStatic<TemplateManagerUtil> mockedUtil = Mockito.mockStatic(TemplateManagerUtil.class)) {
            mockedUtil.when(() -> TemplateManagerUtil.bindInputToContext(testValues))
                    .thenReturn(velocityContext);

            when(velocityEngine.getTemplate(eq("test-template"), eq("UTF-8"))).thenReturn(template);
            doThrow(new ParseErrorException("Parse error")).when(template).merge(velocityContext, testWriter);

            assertThrows(TemplateParsingException.class, () -> {
                templateManager.merge("test-template", testWriter, testValues, "UTF-8");
            });
        }
    }

    /**
     * Tests the merge method with template name when MethodInvocationException occurs during template merge.
     * Verifies that MethodInvocationException is wrapped and thrown as TemplateMethodInvocationException.
     */
    @Test
    void mergeWithTemplateNameAndMethodInvocationExceptionShouldThrowTemplateMethodInvocationException() {
        try (MockedStatic<TemplateManagerUtil> mockedUtil = Mockito.mockStatic(TemplateManagerUtil.class)) {
            mockedUtil.when(() -> TemplateManagerUtil.bindInputToContext(testValues))
                    .thenReturn(velocityContext);

            when(velocityEngine.getTemplate(eq("test-template"), eq("UTF-8"))).thenReturn(template);
            doThrow(new MethodInvocationException("Method invocation error", new Exception(), "test", "template", 1, 1))
                    .when(template).merge(velocityContext, testWriter);

            assertThrows(TemplateMethodInvocationException.class, () -> {
                templateManager.merge("test-template", testWriter, testValues, "UTF-8");
            });
        }
    }

    /**
     * Tests the merge method with template name and writer using default encoding.
     * Verifies that the three-parameter merge method correctly defaults to UTF-8 encoding.
     */
    @Test
    void mergeWithTemplateNameAndWriterWithDefaultEncodingShouldReturnTrue() throws IOException {
        try (MockedStatic<TemplateManagerUtil> mockedUtil = Mockito.mockStatic(TemplateManagerUtil.class)) {
            mockedUtil.when(() -> TemplateManagerUtil.bindInputToContext(testValues))
                    .thenReturn(velocityContext);

            when(velocityEngine.getTemplate(eq("test-template"), eq("UTF-8"))).thenReturn(template);
            doNothing().when(template).merge(velocityContext, testWriter);

            boolean result = templateManager.merge("test-template", testWriter, testValues);

            assertTrue(result);
            verify(velocityEngine, times(1)).getTemplate("test-template", "UTF-8");
            verify(template, times(1)).merge(velocityContext, testWriter);
        }
    }

    /**
     * Tests the constructor with VelocityEngine parameter.
     * Verifies that a TemplateManagerImpl instance is created successfully when provided with a VelocityEngine.
     */
    @Test
    void constructorWithVelocityEngineShouldCreateInstance() {
        TemplateManagerImpl newManager = new TemplateManagerImpl(velocityEngine);

        assertNotNull(newManager);
    }
}