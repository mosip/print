package io.mosip.print.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.templatemanager.exception.TemplateMethodInvocationException;
import io.mosip.kernel.core.templatemanager.exception.TemplateParsingException;
import io.mosip.kernel.core.templatemanager.exception.TemplateResourceNotFoundException;
import io.mosip.print.constant.ApiName;
import io.mosip.print.core.http.ResponseWrapper;
import io.mosip.print.dto.TemplateDto;
import io.mosip.print.dto.TemplateResponseDto;
import io.mosip.print.exception.ApisResourceAccessException;
import io.mosip.print.exception.TemplateProcessingFailureException;
import io.mosip.print.logger.PrintLogger;
import io.mosip.print.service.PrintRestClientService;
import io.mosip.print.spi.TemplateManager;

/**
 * Unit tests for {@link TemplateGenerator} class.
 *
 * <p>This class contains comprehensive test cases for verifying the functionality of the TemplateGenerator class,
 * including template retrieval, template processing, template merging operations, exception handling scenarios,
 * and template manager configuration with various resource loader settings.</p>
 *
 */
@ExtendWith(MockitoExtension.class)
class TemplateGeneratorTest {

    @Mock
    private PrintRestClientService<Object> restClientService;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private TemplateGenerator templateGenerator;

    private static final String TEMPLATE_TYPE_CODE = "test-template";
    private static final String LANG_CODE = "en";
    private static final String TEMPLATE_CONTENT = "Hello ${name}";
    private static final String MERGED_CONTENT = "Hello John";

    /**
     * Sets up test fixtures before each test method execution.
     * Initializes the TemplateGenerator instance with default configuration settings
     * including resource loader, template path, cache settings, and encoding.
     */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(templateGenerator, "resourceLoader", "classpath");
        ReflectionTestUtils.setField(templateGenerator, "templatePath", ".");
        ReflectionTestUtils.setField(templateGenerator, "cache", Boolean.TRUE);
        ReflectionTestUtils.setField(templateGenerator, "defaultEncoding", "UTF-8");
    }

    /**
     * Tests successful template generation and merging process.
     * Verifies that the method correctly retrieves template data, processes it through
     * the template manager, and returns a merged InputStream with attribute values.
     */
    @Test
    void getTemplateShouldSucceedWithValidTemplateAndAttributes() throws Exception {
        ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>();
        TemplateResponseDto templateResponseDto = createTemplateResponseDto();
        responseWrapper.setResponse(templateResponseDto);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "John");

        InputStream mergedStream = new ByteArrayInputStream(MERGED_CONTENT.getBytes());
        TemplateManager mockTemplateManager = mock(TemplateManager.class);

        when(restClientService.getApi(
                eq(ApiName.TEMPLATES),
                any(List.class),
                anyString(),
                anyString(),
                eq(ResponseWrapper.class)
        )).thenReturn(responseWrapper);

        when(mapper.writeValueAsString(templateResponseDto)).thenReturn("template-json");
        when(mapper.readValue("template-json", TemplateResponseDto.class)).thenReturn(templateResponseDto);
        when(mockTemplateManager.merge(any(InputStream.class), eq(attributes))).thenReturn(mergedStream);

        TemplateGenerator spyTemplateGenerator = new TemplateGenerator() {
            @Override
            public TemplateManager getTemplateManager() {
                return mockTemplateManager;
            }
        };
        ReflectionTestUtils.setField(spyTemplateGenerator, "restClientService", restClientService);
        ReflectionTestUtils.setField(spyTemplateGenerator, "mapper", mapper);

        InputStream result = spyTemplateGenerator.getTemplate(TEMPLATE_TYPE_CODE, attributes, LANG_CODE);

        assertNotNull(result);
        assertEquals(mergedStream, result);
    }

    /**
     * Tests template generation when retrieved template is null.
     * Verifies that the method handles null template responses gracefully
     * and returns null when no template data is available.
     */
    @Test
    void getTemplateWhenTemplateIsNullShouldReturnNull() throws Exception {
        ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>();
        responseWrapper.setResponse(null);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "John");

        when(restClientService.getApi(
                eq(ApiName.TEMPLATES),
                any(List.class),
                anyString(),
                anyString(),
                eq(ResponseWrapper.class)
        )).thenReturn(responseWrapper);

        when(mapper.writeValueAsString(null)).thenReturn("null");
        when(mapper.readValue("null", TemplateResponseDto.class)).thenReturn(null);

        InputStream result = templateGenerator.getTemplate(TEMPLATE_TYPE_CODE, attributes, LANG_CODE);

        assertEquals(null, result);
    }

    /**
     * Tests template generation when TemplateResourceNotFoundException occurs.
     * Verifies that TemplateResourceNotFoundException is properly wrapped and thrown
     * as TemplateProcessingFailureException during template processing.
     */
    @Test
    void getTemplateWithTemplateResourceNotFoundExceptionShouldThrowTemplateProcessingFailureException() throws Exception {
        ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>();
        TemplateResponseDto templateResponseDto = createTemplateResponseDto();
        responseWrapper.setResponse(templateResponseDto);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "John");

        TemplateManager mockTemplateManager = mock(TemplateManager.class);

        when(restClientService.getApi(
                eq(ApiName.TEMPLATES),
                any(List.class),
                anyString(),
                anyString(),
                eq(ResponseWrapper.class)
        )).thenReturn(responseWrapper);

        when(mapper.writeValueAsString(templateResponseDto)).thenReturn("template-json");
        when(mapper.readValue("template-json", TemplateResponseDto.class)).thenReturn(templateResponseDto);
        when(mockTemplateManager.merge(any(InputStream.class), eq(attributes)))
                .thenThrow(new TemplateResourceNotFoundException("ERR-001", "Template not found"));

        TemplateGenerator spyTemplateGenerator = new TemplateGenerator() {
            @Override
            public TemplateManager getTemplateManager() {
                return mockTemplateManager;
            }
        };
        ReflectionTestUtils.setField(spyTemplateGenerator, "restClientService", restClientService);
        ReflectionTestUtils.setField(spyTemplateGenerator, "mapper", mapper);

        assertThrows(TemplateProcessingFailureException.class, () ->
                spyTemplateGenerator.getTemplate(TEMPLATE_TYPE_CODE, attributes, LANG_CODE)
        );
    }

    /**
     * Tests template generation when TemplateParsingException occurs.
     * Verifies that TemplateParsingException is properly wrapped and thrown
     * as TemplateProcessingFailureException during template parsing operations.
     */
    @Test
    void getTemplateWithTemplateParsingExceptionShouldThrowTemplateProcessingFailureException() throws Exception {
        ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>();
        TemplateResponseDto templateResponseDto = createTemplateResponseDto();
        responseWrapper.setResponse(templateResponseDto);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "John");

        TemplateManager mockTemplateManager = mock(TemplateManager.class);

        when(restClientService.getApi(
                eq(ApiName.TEMPLATES),
                any(List.class),
                anyString(),
                anyString(),
                eq(ResponseWrapper.class)
        )).thenReturn(responseWrapper);

        when(mapper.writeValueAsString(templateResponseDto)).thenReturn("template-json");
        when(mapper.readValue("template-json", TemplateResponseDto.class)).thenReturn(templateResponseDto);
        when(mockTemplateManager.merge(any(InputStream.class), eq(attributes)))
                .thenThrow(new TemplateParsingException("ERR-002", "Parsing error"));

        TemplateGenerator spyTemplateGenerator = new TemplateGenerator() {
            @Override
            public TemplateManager getTemplateManager() {
                return mockTemplateManager;
            }
        };
        ReflectionTestUtils.setField(spyTemplateGenerator, "restClientService", restClientService);
        ReflectionTestUtils.setField(spyTemplateGenerator, "mapper", mapper);

        assertThrows(TemplateProcessingFailureException.class, () ->
                spyTemplateGenerator.getTemplate(TEMPLATE_TYPE_CODE, attributes, LANG_CODE)
        );
    }

    /**
     * Tests template generation when TemplateMethodInvocationException occurs.
     * Verifies that TemplateMethodInvocationException is properly wrapped and thrown
     * as TemplateProcessingFailureException during template method execution.
     */
    @Test
    void getTemplateWithTemplateMethodInvocationExceptionShouldThrowTemplateProcessingFailureException() throws Exception {
        ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>();
        TemplateResponseDto templateResponseDto = createTemplateResponseDto();
        responseWrapper.setResponse(templateResponseDto);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "John");

        TemplateManager mockTemplateManager = mock(TemplateManager.class);

        when(restClientService.getApi(
                eq(ApiName.TEMPLATES),
                any(List.class),
                anyString(),
                anyString(),
                eq(ResponseWrapper.class)
        )).thenReturn(responseWrapper);

        when(mapper.writeValueAsString(templateResponseDto)).thenReturn("template-json");
        when(mapper.readValue("template-json", TemplateResponseDto.class)).thenReturn(templateResponseDto);
        when(mockTemplateManager.merge(any(InputStream.class), eq(attributes)))
                .thenThrow(new TemplateMethodInvocationException("ERR-003", "Method invocation error"));

        TemplateGenerator spyTemplateGenerator = new TemplateGenerator() {
            @Override
            public TemplateManager getTemplateManager() {
                return mockTemplateManager;
            }
        };
        ReflectionTestUtils.setField(spyTemplateGenerator, "restClientService", restClientService);
        ReflectionTestUtils.setField(spyTemplateGenerator, "mapper", mapper);

        assertThrows(TemplateProcessingFailureException.class, () ->
                spyTemplateGenerator.getTemplate(TEMPLATE_TYPE_CODE, attributes, LANG_CODE)
        );
    }

    /**
     * Tests template generation when ApisResourceAccessException is thrown.
     * Verifies that ApisResourceAccessException is properly propagated when
     * the REST client service fails to access the template API.
     */
    @Test
    void getTemplateWithApisResourceAccessExceptionShouldThrowException() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "John");

        when(restClientService.getApi(
                eq(ApiName.TEMPLATES),
                any(List.class),
                anyString(),
                anyString(),
                eq(ResponseWrapper.class)
        )).thenThrow(new ApisResourceAccessException("API Error"));

        assertThrows(ApisResourceAccessException.class, () ->
                templateGenerator.getTemplate(TEMPLATE_TYPE_CODE, attributes, LANG_CODE)
        );
    }

    /**
     * Tests template generation when IOException occurs during JSON processing.
     * Verifies that IOException is properly propagated when JSON serialization
     * or deserialization fails during template processing.
     */
    @Test
    void getTemplateWithIoExceptionShouldThrowException() throws Exception {
        ResponseWrapper<Object> responseWrapper = new ResponseWrapper<>();
        TemplateResponseDto templateResponseDto = createTemplateResponseDto();
        responseWrapper.setResponse(templateResponseDto);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("name", "John");

        when(restClientService.getApi(
                eq(ApiName.TEMPLATES),
                any(List.class),
                anyString(),
                anyString(),
                eq(ResponseWrapper.class)
        )).thenReturn(responseWrapper);

        when(mapper.writeValueAsString(templateResponseDto))
                .thenThrow(new JsonProcessingException("JSON Error") {});

        assertThrows(IOException.class, () ->
                templateGenerator.getTemplate(TEMPLATE_TYPE_CODE, attributes, LANG_CODE)
        );
    }

    /**
     * Tests the getTemplateManager method with default configuration.
     * Verifies that the method correctly creates and returns a TemplateManager instance
     * with the configured resource loader, path, cache, and encoding settings.
     */
    @Test
    void getTemplateManagerShouldReturnTemplateManagerInstance() {
        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            printLoggerMock.when(() -> PrintLogger.getLogger(TemplateGenerator.class))
                    .thenReturn(mock(org.slf4j.Logger.class));

            TemplateManager result = templateGenerator.getTemplateManager();

            assertNotNull(result);
        }
    }

    /**
     * Tests the getTemplateManager method with different resource loader settings.
     * Verifies that the method correctly handles various configuration settings
     * including file resource loader, custom template path, disabled cache, and different encoding.
     */
    @Test
    void getTemplateManagerWithDifferentSettingsShouldReturnTemplateManagerInstance() {
        ReflectionTestUtils.setField(templateGenerator, "resourceLoader", "file");
        ReflectionTestUtils.setField(templateGenerator, "templatePath", "/templates");
        ReflectionTestUtils.setField(templateGenerator, "cache", Boolean.FALSE);
        ReflectionTestUtils.setField(templateGenerator, "defaultEncoding", "ISO-8859-1");

        try (MockedStatic<PrintLogger> printLoggerMock = mockStatic(PrintLogger.class)) {
            printLoggerMock.when(() -> PrintLogger.getLogger(TemplateGenerator.class))
                    .thenReturn(mock(org.slf4j.Logger.class));

            TemplateManager result = templateGenerator.getTemplateManager();

            assertNotNull(result);
        }
    }

    /**
     * Helper method to create TemplateResponseDto for testing purposes.
     * Creates a mock template response containing template data with placeholder content
     * for use in template processing tests.
     *
     * @return TemplateResponseDto with test template data
     */
    private TemplateResponseDto createTemplateResponseDto() {
        TemplateResponseDto templateResponseDto = new TemplateResponseDto();
        List<TemplateDto> templates = new ArrayList<>();
        TemplateDto templateDto = new TemplateDto();
        templateDto.setFileText(TEMPLATE_CONTENT);
        templates.add(templateDto);
        templateResponseDto.setTemplates(templates);
        return templateResponseDto;
    }
}
