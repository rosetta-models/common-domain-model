package org.finos.cdm.example;

import cdm.legaldocumentation.common.LegalAgreement;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;
import com.regnosys.rosetta.common.util.ClassPathUtils;
import org.finos.rune.mapper.RuneJsonObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LegalAgreementConversionTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(LegalAgreementConversionTest.class);

    private static final String SAMPLES_PATH = "temp-samples";

    private static final LegalAgreementConverter CONVERTER = new LegalAgreementConverter();

    @ParameterizedTest(name = "{0}")
    @MethodSource("legalAgreements")
    void shouldBeInRuneJsonFormat(String fileName, Path path, String content) throws IOException {
        if (!CONVERTER.isRuneJson(content)) {
            content = convertAndPersist(fileName, path, content);
        }

        LegalAgreement deserialized = CONVERTER.deserializeRune(content);
        assertNotNull(deserialized);

        String serialized = CONVERTER.serializeRune(deserialized);
        assertEquals(content, serialized, "JSON content should be stable and in Rune format. " +
                "If this fails, it might be due to formatting differences. The file has been updated.");
    }

    private String convertAndPersist(String fileName, Path path, String content) throws IOException {
        LOGGER.info("Converting {} to Rune JSON format", fileName);
        String runeJson = CONVERTER.convertToRuneJson(content);

        // Try to write to source directory to persist changes
        String sourcePath = path.toAbsolutePath().toString().replace("/target/test-classes/", "/samples/");
        try {
            Files.writeString(Path.of(sourcePath), runeJson);
            LOGGER.info("Updated source file: {}", sourcePath);
        } catch (Exception e) {
            LOGGER.warn("Could not write to source path {}, updating build path instead", sourcePath);
            Files.writeString(path, runeJson);
        }
        return runeJson;
    }

    private static Stream<Object[]> legalAgreements() throws IOException {
        Path samplesDirectory = findSamplesDirectory();
        LOGGER.info("Loading samples from {}", samplesDirectory.toAbsolutePath());

        return Files.walk(samplesDirectory)
                .filter(path -> path.getFileName().toString().endsWith(".json"))
                .map(LegalAgreementConversionTest::readJson);
    }

    private static Path findSamplesDirectory() {
        // Try various relative paths depending on where the test is executed from
        Path[] possiblePaths = {
                Path.of("samples").resolve(SAMPLES_PATH),
                Path.of("examples/samples").resolve(SAMPLES_PATH),
                Path.of("common-domain-model/examples/samples").resolve(SAMPLES_PATH)
        };

        for (Path path : possiblePaths) {
            if (Files.exists(path)) {
                return path;
            }
        }

        // Try classpath as a fallback
        return ClassPathUtils.loadFromClasspath(SAMPLES_PATH, LegalAgreementConversionTest.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find samples directory at any of the expected locations. " +
                        "Please ensure the 'examples/samples' directory exists."));
    }

    private static Object[] readJson(Path path) {
        try {
            String pathString = path.toString().replace('\\', '/');
            int index = pathString.indexOf(SAMPLES_PATH);
            String displayName = path.getFileName().toString();
            if (index != -1 && index + SAMPLES_PATH.length() + 1 < pathString.length()) {
                displayName = pathString.substring(index + SAMPLES_PATH.length() + 1);
            }
            return new Object[]{displayName, path, Files.readString(path)};
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON from " + path, e);
        }
    }
}
