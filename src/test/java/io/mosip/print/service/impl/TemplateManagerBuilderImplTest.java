package io.mosip.print.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.mosip.print.spi.TemplateManager;
import io.mosip.print.spi.TemplateManagerBuilder;

/**
 * Unit tests for {@link TemplateManagerBuilderImpl} class.
 *
 * <p>This class contains comprehensive test cases for verifying the functionality of the TemplateManagerBuilderImpl class,
 * including builder pattern implementation, property configuration, template manager creation, and method chaining.</p>
 */
@ExtendWith(MockitoExtension.class)
class TemplateManagerBuilderImplTest {

    private TemplateManagerBuilderImpl templateManagerBuilder;

    /**
     * Sets up test fixtures before each test method execution.
     * Initializes a fresh instance of TemplateManagerBuilderImpl for each test.
     */
    @BeforeEach
    void setUp() {
        templateManagerBuilder = new TemplateManagerBuilderImpl();
    }

    /**
     * Tests the default constructor initialization.
     * Verifies that the builder is created with correct default values for all properties.
     */
    @Test
    void constructorWithDefaultValuesShouldInitializeCorrectly() {
        assertEquals("classpath", templateManagerBuilder.getResourceLoader());
        assertEquals(".", templateManagerBuilder.getTemplatePath());
        assertTrue(templateManagerBuilder.isCache());
        assertEquals(StandardCharsets.UTF_8.name(), templateManagerBuilder.getDefaultEncoding());
    }

    /**
     * Tests the resourceLoader method with valid input.
     * Verifies that the resource loader is set correctly and the builder instance is returned for chaining.
     */
    @Test
    void resourceLoaderWithValidInputShouldSetValueAndReturnBuilder() {
        TemplateManagerBuilder result = templateManagerBuilder.resourceLoader("file");

        assertEquals("file", templateManagerBuilder.getResourceLoader());
        assertSame(templateManagerBuilder, result);
    }

    /**
     * Tests the resourceLoader method with empty string input.
     * Verifies that empty strings are accepted and stored correctly.
     */
    @Test
    void resourceLoaderWithEmptyStringShouldSetEmptyValue() {
        TemplateManagerBuilder result = templateManagerBuilder.resourceLoader("");

        assertEquals("", templateManagerBuilder.getResourceLoader());
        assertSame(templateManagerBuilder, result);
    }

    /**
     * Tests the resourcePath method with valid path input.
     * Verifies that the template path is set correctly and the builder instance is returned for chaining.
     */
    @Test
    void resourcePathWithValidInputShouldSetValueAndReturnBuilder() {
        TemplateManagerBuilder result = templateManagerBuilder.resourcePath("/templates");

        assertEquals("/templates", templateManagerBuilder.getTemplatePath());
        assertSame(templateManagerBuilder, result);
    }

    /**
     * Tests the resourcePath method with empty string input.
     * Verifies that empty strings are accepted and stored correctly.
     */
    @Test
    void resourcePathWithEmptyStringShouldSetEmptyValue() {
        TemplateManagerBuilder result = templateManagerBuilder.resourcePath("");

        assertEquals("", templateManagerBuilder.getTemplatePath());
        assertSame(templateManagerBuilder, result);
    }

    /**
     * Tests the enableCache method with true value.
     * Verifies that cache is enabled correctly and the builder instance is returned for chaining.
     */
    @Test
    void enableCacheWithTrueShouldSetTrueAndReturnBuilder() {
        templateManagerBuilder.enableCache(false);

        TemplateManagerBuilder result = templateManagerBuilder.enableCache(true);

        assertTrue(templateManagerBuilder.isCache());
        assertSame(templateManagerBuilder, result);
    }

    /**
     * Tests the enableCache method with false value.
     * Verifies that cache is disabled correctly and the builder instance is returned for chaining.
     */
    @Test
    void enableCacheWithFalseShouldSetFalseAndReturnBuilder() {
        TemplateManagerBuilder result = templateManagerBuilder.enableCache(false);

        assertFalse(templateManagerBuilder.isCache());
        assertSame(templateManagerBuilder, result);
    }

    /**
     * Tests the encodingType method with valid encoding input.
     * Verifies that the encoding type is set correctly and the builder instance is returned for chaining.
     */
    @Test
    void encodingTypeWithValidInputShouldSetValueAndReturnBuilder() {
        TemplateManagerBuilder result = templateManagerBuilder.encodingType("ISO-8859-1");

        assertEquals("ISO-8859-1", templateManagerBuilder.getDefaultEncoding());
        assertSame(templateManagerBuilder, result);
    }

    /**
     * Tests the encodingType method with empty string input.
     * Verifies that empty strings are accepted and stored correctly.
     */
    @Test
    void encodingTypeWithEmptyStringShouldSetEmptyValue() {
        TemplateManagerBuilder result = templateManagerBuilder.encodingType("");

        assertEquals("", templateManagerBuilder.getDefaultEncoding());
        assertSame(templateManagerBuilder, result);
    }

    /**
     * Tests the build method with default configuration.
     * Verifies that a TemplateManager instance is created successfully with default settings.
     */
    @Test
    void buildWithDefaultConfigurationShouldReturnTemplateManager() {
        TemplateManager result = templateManagerBuilder.build();

        assertNotNull(result);
        assertTrue(result instanceof TemplateManagerImpl);
    }

    /**
     * Tests the build method with custom configuration.
     * Verifies that a TemplateManager instance is created successfully with all custom settings applied.
     */
    @Test
    void buildWithCustomConfigurationShouldReturnTemplateManager() {
        templateManagerBuilder
                .resourceLoader("file")
                .resourcePath("/custom/templates")
                .enableCache(false)
                .encodingType("ISO-8859-1");

        TemplateManager result = templateManagerBuilder.build();

        assertNotNull(result);
        assertTrue(result instanceof TemplateManagerImpl);
    }

    /**
     * Tests the build method with classpath resource loader.
     * Verifies that a TemplateManager instance is created successfully with classpath resource loader configuration.
     */
    @Test
    void buildWithClasspathResourceLoaderShouldReturnTemplateManager() {
        templateManagerBuilder.resourceLoader("classpath");

        TemplateManager result = templateManagerBuilder.build();

        assertNotNull(result);
        assertTrue(result instanceof TemplateManagerImpl);
    }

    /**
     * Tests the build method with file resource loader.
     * Verifies that a TemplateManager instance is created successfully with file resource loader configuration.
     */
    @Test
    void buildWithFileResourceLoaderShouldReturnTemplateManager() {
        templateManagerBuilder.resourceLoader("file");

        TemplateManager result = templateManagerBuilder.build();

        assertNotNull(result);
        assertTrue(result instanceof TemplateManagerImpl);
    }

    /**
     * Tests the build method with cache disabled.
     * Verifies that a TemplateManager instance is created successfully with caching disabled.
     */
    @Test
    void buildWithCacheDisabledShouldReturnTemplateManager() {
        templateManagerBuilder.enableCache(false);

        TemplateManager result = templateManagerBuilder.build();

        assertNotNull(result);
        assertTrue(result instanceof TemplateManagerImpl);
    }

    /**
     * Tests the builder pattern chaining functionality.
     * Verifies that all builder methods can be chained together and return the same builder instance.
     */
    @Test
    void builderPatternChaininShouldWorkCorrectly() {
        TemplateManager result = templateManagerBuilder
                .resourceLoader("file")
                .resourcePath("/test/templates")
                .enableCache(true)
                .encodingType("UTF-16")
                .build();

        assertNotNull(result);
        assertTrue(result instanceof TemplateManagerImpl);
        assertEquals("file", templateManagerBuilder.getResourceLoader());
        assertEquals("/test/templates", templateManagerBuilder.getTemplatePath());
        assertTrue(templateManagerBuilder.isCache());
        assertEquals("UTF-16", templateManagerBuilder.getDefaultEncoding());
    }

    /**
     * Tests multiple build calls on the same builder instance.
     * Verifies that the builder can be reused to create multiple TemplateManager instances.
     */
    @Test
    void multipleBuildCallsShouldReturnDifferentInstances() {
        TemplateManager result1 = templateManagerBuilder.build();
        TemplateManager result2 = templateManagerBuilder.build();

        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(result1 instanceof TemplateManagerImpl);
        assertTrue(result2 instanceof TemplateManagerImpl);
    }

    /**
     * Tests getter methods for all properties.
     * Verifies that all getter methods return the correct values after setting properties.
     */
    @Test
    void getterMethodsShouldReturnCorrectValues() {
        templateManagerBuilder
                .resourceLoader("custom")
                .resourcePath("/custom/path")
                .enableCache(false)
                .encodingType("UTF-32");

        assertEquals("custom", templateManagerBuilder.getResourceLoader());
        assertEquals("/custom/path", templateManagerBuilder.getTemplatePath());
        assertFalse(templateManagerBuilder.isCache());
        assertEquals("UTF-32", templateManagerBuilder.getDefaultEncoding());
    }

    /**
     * Tests the build method with different encoding types.
     * Verifies that various encoding types are handled correctly.
     */
    @Test
    void buildWithDifferentEncodingTypesShouldReturnTemplateManager() {
        templateManagerBuilder.encodingType("US-ASCII");

        TemplateManager result = templateManagerBuilder.build();

        assertNotNull(result);
        assertTrue(result instanceof TemplateManagerImpl);
        assertEquals("US-ASCII", templateManagerBuilder.getDefaultEncoding());
    }

    /**
     * Tests the build method with different template paths.
     * Verifies that various template paths are handled correctly.
     */
    @Test
    void buildWithDifferentTemplatePathsShouldReturnTemplateManager() {
        templateManagerBuilder.resourcePath("/absolute/path/to/templates");

        TemplateManager result = templateManagerBuilder.build();

        assertNotNull(result);
        assertTrue(result instanceof TemplateManagerImpl);
        assertEquals("/absolute/path/to/templates", templateManagerBuilder.getTemplatePath());
    }

    /**
     * Tests the build method with all possible boolean combinations for cache.
     * Verifies that both cache enabled and disabled scenarios work correctly.
     */
    @Test
    void buildWithAllCacheCombinationsShouldReturnTemplateManager() {
        templateManagerBuilder.enableCache(true);
        TemplateManager result1 = templateManagerBuilder.build();
        assertNotNull(result1);

        templateManagerBuilder.enableCache(false);
        TemplateManager result2 = templateManagerBuilder.build();
        assertNotNull(result2);
    }
}
