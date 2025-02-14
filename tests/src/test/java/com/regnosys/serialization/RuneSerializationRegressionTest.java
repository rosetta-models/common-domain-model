package com.regnosys.serialization;

import cdm.event.common.TradeState;
import cdm.event.workflow.WorkflowStep;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapperCreator;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import org.finos.rune.mapper.RuneJsonObjectMapper;
import org.finos.rune.mapper.processor.SerializationPreProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RuneSerializationRegressionTest {
    private ObjectMapper oldMapper;
    private ObjectMapper newMapper;

    @BeforeEach
    public void setUp() {
        oldMapper = RosettaObjectMapperCreator.forJSON().create();
        newMapper = new RuneJsonObjectMapper();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testCases")
    public void testSerializersForRegressions(String fileName, String fileContents, Class<? extends RosettaModelObject> rosettaRootType) {
        String initialContents = prune(fileContents, rosettaRootType);

        RosettaModelObject deserializeFromOld = fromJson(initialContents, rosettaRootType, oldMapper);
        String serializeToNew = toJson(deserializeFromOld, newMapper);
        RosettaModelObject deserializeFromNew = fromJson(serializeToNew, rosettaRootType, newMapper);
        String serializeBackToOld = toJson(prunedEmpty(deserializeFromNew), oldMapper);
        assertEquals(initialContents, serializeBackToOld);
    }

    private static RosettaModelObjectBuilder prunedEmpty(RosettaModelObject deserializeFromNew) {
        return deserializeFromNew.toBuilder().prune();
    }

    public static Stream<Arguments> testCases() {
        Path resourcesPath = Paths.get("..")
                .resolve("rosetta-source")
                .resolve("src")
                .resolve("main")
                .resolve("resources");

        return getJsonFileGroups().stream()
                .flatMap(jsonFileGroup -> {
                    Path topPath = resourcesPath
                            .resolve(jsonFileGroup.jsonFile);
                    try (Stream<Path> paths = Files.walk(topPath)) {
                        return paths.filter(Files::isRegularFile)
                                .map(filePath -> {
                                    String fileContents = readAsString(filePath);
                                    var name = resourcesPath.relativize(filePath).toString();
                                    return Arguments.of(name, fileContents, jsonFileGroup.ingestionType);
                                }).collect(Collectors.toList())
                                .stream();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private String prune(String fileContents, Class<? extends RosettaModelObject> rosettaRootType) {
        RosettaModelObject rosettaModelObject = fromJson(fileContents, rosettaRootType, oldMapper);
        SerializationPreProcessor serializationPreProcessor = new SerializationPreProcessor();
        RosettaModelObject processed = serializationPreProcessor.process(rosettaModelObject);
        return toJson(processed, oldMapper);
    }

    private static Set<JsonFileGroup> getJsonFileGroups() {
        return Sets.newHashSet(
                new JsonFileGroup("result-json-files/cme-cleared-confirm-1-17", WorkflowStep.class),
                new JsonFileGroup("result-json-files/cme-submission-irs-1-0", WorkflowStep.class),
                new JsonFileGroup("result-json-files/dtcc-9-0", WorkflowStep.class),
                new JsonFileGroup("result-json-files/dtcc-11-0", WorkflowStep.class),
                new JsonFileGroup("result-json-files/fis", WorkflowStep.class),
                new JsonFileGroup("result-json-files/fpml-5-10/incomplete-processes", WorkflowStep.class),
                new JsonFileGroup("result-json-files/fpml-5-10/incomplete-products", TradeState.class),
                new JsonFileGroup("result-json-files/fpml-5-10/invalid-products", TradeState.class),
                new JsonFileGroup("result-json-files/fpml-5-10/processes", WorkflowStep.class),
                new JsonFileGroup("result-json-files/fpml-5-10/products", TradeState.class),
                new JsonFileGroup("result-json-files/fpml-5-12/incomplete-products/equity", TradeState.class),
                new JsonFileGroup("result-json-files/fpml-5-12/processes", WorkflowStep.class),
                new JsonFileGroup("result-json-files/fpml-5-12/products", TradeState.class),
                new JsonFileGroup("result-json-files/fpml-5-13/incomplete-products", TradeState.class),
                new JsonFileGroup("result-json-files/fpml-5-13/incomplete-products/correlation-swaps", TradeState.class),
                new JsonFileGroup("result-json-files/fpml-5-13/incomplete-products/equity-options", TradeState.class),
                new JsonFileGroup("result-json-files/fpml-5-13/incomplete-products/equity-swaps", TradeState.class),
                new JsonFileGroup("result-json-files/fpml-5-13/incomplete-products/fx-derivatives", TradeState.class),
                new JsonFileGroup("result-json-files/fpml-5-13/incomplete-products/repo", TradeState.class),
                new JsonFileGroup("result-json-files/fpml-5-13/incomplete-products/variance-swaps", TradeState.class),
                new JsonFileGroup("result-json-files/fpml-5-13/processes/execution-advice", WorkflowStep.class),
                new JsonFileGroup("result-json-files/fpml-5-13/products", TradeState.class),
                new JsonFileGroup("result-json-files/native-cdm-events", WorkflowStep.class),
                new JsonFileGroup("result-json-files/ore-1-0-39", TradeState.class)
        );
    }

    private static String readAsString(Path jsonPath) {
        try {
            return new String(Files.readAllBytes(jsonPath));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static <T extends RosettaModelObject> T fromJson(String runeJson, Class<T> type, ObjectMapper objectMapper) {
        try {
            return objectMapper.readValue(runeJson, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T extends RosettaModelObject> String toJson(T runeObject, ObjectMapper objectMapper) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(runeObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static class JsonFileGroup {
        public final String jsonFile;
        public final Class<? extends RosettaModelObject> ingestionType;

        private JsonFileGroup(String jsonFile, Class<? extends RosettaModelObject> ingestionType) {
            this.jsonFile = jsonFile;
            this.ingestionType = ingestionType;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            JsonFileGroup that = (JsonFileGroup) o;
            return Objects.equals(jsonFile, that.jsonFile) && Objects.equals(ingestionType, that.ingestionType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(jsonFile, ingestionType);
        }
    }

}
