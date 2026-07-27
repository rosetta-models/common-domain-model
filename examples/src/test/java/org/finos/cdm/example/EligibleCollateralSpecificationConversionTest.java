package org.finos.cdm.example;

import cdm.product.collateral.EligibleCollateralSpecification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;
import org.finos.cdm.example.util.ResourcesUtils;
import org.finos.rune.mapper.RuneJsonObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Converts EligibleCollateralSpecification CDM objects between Legacy JSON
 * (CDM v5/v6) and Rune JSON (CDM v7+) formats.
 */
public class EligibleCollateralSpecificationConversionTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EligibleCollateralSpecificationConversionTest.class);

    private static final String SAMPLE_RESOURCE =
            "legal-agreements/eligible-collateral-schedules/01-eligible-collateral-schedule-example/01-collateral-schedule-example-1.json";

    private static final ObjectMapper LEGACY_JSON_OBJECT_MAPPER =
            RosettaObjectMapper.getNewMinimalRosettaObjectMapper();

    private static final RuneJsonObjectMapper RUNE_JSON_OBJECT_MAPPER =
            new RuneJsonObjectMapper();

    @Test
    void shouldConvertEligibleCollateralSpecificationFromLegacyJsonToRuneJson() throws JsonProcessingException {
        String originalRuneJson = ResourcesUtils.getJson(SAMPLE_RESOURCE);

        String legacyJson = convertFromRuneJsonToLegacyJson(originalRuneJson);
        LOGGER.info("EligibleCollateralSpecification serialized in Legacy JSON: {}", legacyJson);

        String convertedRuneJson = convertFromLegacyJsonToRuneJson(legacyJson);
        LOGGER.info("EligibleCollateralSpecification serialized in Rune JSON: {}", convertedRuneJson);

        EligibleCollateralSpecification deserializedFromRuneJson =
                RUNE_JSON_OBJECT_MAPPER.readValue(convertedRuneJson, EligibleCollateralSpecification.class);

        assertNotNull(deserializedFromRuneJson);
        assertEquals(originalRuneJson, convertedRuneJson);
    }

    public static String convertFromLegacyJsonToRuneJson(String legacyJson) throws JsonProcessingException {
        EligibleCollateralSpecification eligibleCollateralSpecification =
                LEGACY_JSON_OBJECT_MAPPER.readValue(legacyJson, EligibleCollateralSpecification.class);

        assertNotNull(eligibleCollateralSpecification);

        return RUNE_JSON_OBJECT_MAPPER
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(eligibleCollateralSpecification);
    }

    public static String convertFromRuneJsonToLegacyJson(String runeJson) throws JsonProcessingException {
        EligibleCollateralSpecification eligibleCollateralSpecification =
                RUNE_JSON_OBJECT_MAPPER.readValue(runeJson, EligibleCollateralSpecification.class);

        assertNotNull(eligibleCollateralSpecification);

        return LEGACY_JSON_OBJECT_MAPPER
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(eligibleCollateralSpecification);
    }
}