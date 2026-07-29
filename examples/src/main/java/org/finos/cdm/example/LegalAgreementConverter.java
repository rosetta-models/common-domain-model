package org.finos.cdm.example;

import cdm.legaldocumentation.common.LegalAgreement;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;
import org.finos.rune.mapper.RuneJsonObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Utility class to convert LegalAgreement samples from Legacy JSON format to Rune JSON format.
 */
public class LegalAgreementConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegalAgreementConverter.class);

    private static final ObjectMapper LEGACY_JSON_OBJECT_MAPPER =
            RosettaObjectMapper.getNewMinimalRosettaObjectMapper();

    private static final RuneJsonObjectMapper RUNE_JSON_OBJECT_MAPPER =
            new RuneJsonObjectMapper();

    /**
     * Converts a Legacy JSON string to a Rune JSON string.
     *
     * @param legacyJson The JSON string in Legacy format.
     * @return The JSON string in Rune format.
     * @throws IOException If deserialization or serialization fails.
     */
    public String convertToRuneJson(String legacyJson) throws IOException {
        LegalAgreement agreement = deserializeLegacy(legacyJson);
        return serializeRune(agreement);
    }

    /**
     * Deserializes a LegalAgreement from Legacy JSON.
     */
    public LegalAgreement deserializeLegacy(String legacyJson) throws IOException {
        return LEGACY_JSON_OBJECT_MAPPER.readValue(legacyJson, LegalAgreement.class);
    }

    /**
     * Serializes a LegalAgreement to Rune JSON.
     */
    public String serializeRune(LegalAgreement agreement) throws JsonProcessingException {
        return RUNE_JSON_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(agreement);
    }

    /**
     * Deserializes a LegalAgreement from Rune JSON.
     */
    public LegalAgreement deserializeRune(String runeJson) throws IOException {
        return RUNE_JSON_OBJECT_MAPPER.readValue(runeJson, LegalAgreement.class);
    }

    /**
     * Checks if the content is already in Rune JSON format (based on the presence of @type).
     */
    public boolean isRuneJson(String content) {
        return content != null && content.contains("\"@type\"");
    }
}
