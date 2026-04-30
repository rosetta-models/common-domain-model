package org.finos.cdm.testpack;

import cdm.fixml.components.base.Abstract_message_t;
import cdm.fixml.order.base.ExecRpt;
import cdm.ingest.fpml.confirmation.message.functions.Ingest_FixML_MessageToTradeState;
import cdm.ingest.fpml.confirmation.message.functions.Ingest_FpmlConfirmationToTradeState;
import cdm.ingest.fpml.confirmation.message.functions.Ingest_FpmlConfirmationToWorkflowStep;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Injector;
import com.regnosys.rosetta.common.transform.PipelineModel;
import fpml.consolidated.doc.Document;
import org.finos.cdm.functions.FunctionCreator;
import com.regnosys.rosetta.common.transform.TransformType;
import com.regnosys.runefpml.RuneFpmlModelConfig;
import com.regnosys.testing.TestingExpectationUtil;
import com.regnosys.testing.pipeline.PipelineConfigWriter;
import com.regnosys.testing.pipeline.PipelineTestPackFilter;
import com.regnosys.testing.pipeline.PipelineTreeConfig;
import jakarta.inject.Inject;
import org.finos.cdm.CdmRuntimeModuleTesting;
import org.finos.cdm.functions.FunctionInputCreator;
import org.finos.cdm.functions.SecLendingFunctionInputCreationTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.regnosys.testing.pipeline.PipelineFilter.startsWith;

public class CdmTestPackCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CdmTestPackCreator.class);

    /**
     * For this function, only run the test pack specified.
     * This set of test packs need to be excluded for all test packs except the one defined.
     */
    public static final List<String> EVENT_TEST_PACKS =
            List.of("fpml-5-10-native-cdm-events",
                    "fpml-5-10-incomplete-processes",
                    "fpml-5-10-processes",
                    "fpml-5-12-processes",
                    "fpml-5-13-incomplete-processes-execution-advice",
                    "fpml-5-13-processes-execution-advice");

    @Inject
    PipelineConfigWriter pipelineConfigWriter;

    public static void main(String[] args) {
        try {
            CdmTestPackCreator testPackConfigCreator = new CdmTestPackCreator();
            Injector injector = new CdmRuntimeModuleTesting.InjectorProvider().getInjector();
            injector.injectMembers(testPackConfigCreator);

            testPackConfigCreator.runIngest();

            //testPackConfigCreator.runFunctionCreators();

            System.exit(0);
        } catch (Exception e) {
            LOGGER.error("Error executing {}.main()", CdmTestPackCreator.class.getName(), e);
            System.exit(1);
        }
    }

    private void runFunctionCreators() throws Exception {

        LOGGER.info(" ** Updating Function Input Samples");

        FunctionInputCreator functionInputCreator = new FunctionInputCreator();
        functionInputCreator.run(TestingExpectationUtil.TEST_WRITE_BASE_PATH);

        SecLendingFunctionInputCreationTest SecLendingFunctionInputCreationTest = new SecLendingFunctionInputCreationTest();
        SecLendingFunctionInputCreationTest.run();

        LOGGER.info(" ** Updating Function Output Samples");

        FunctionCreator functionCreator = new FunctionCreator();
        functionCreator.run();
    }

    private void runIngest() throws IOException {
        //pipelineConfigWriter.writePipelinesAndTestPacks(createFpmlConfig());
        pipelineConfigWriter.writePipelinesAndTestPacks(createFixmlConfig());
    }

    private PipelineTreeConfig createFpmlConfig() {
        PipelineTestPackFilter filter = PipelineTestPackFilter.create()
                .withTestPacksSpecificToFunctions(getEventsTestPackFilter());

        return addXMLAndSchemaMap(new PipelineTreeConfig()
                .withTestPackIdFilter(startsWith("fpml"))
                .withTestPackFilter(filter)
                .starting(TransformType.TRANSLATE, Ingest_FpmlConfirmationToTradeState.class)
                .starting(TransformType.TRANSLATE, Ingest_FpmlConfirmationToWorkflowStep.class));
    }

    private PipelineTreeConfig createFixmlConfig() {
        return addXMLAndSchemaMap(new PipelineTreeConfig()
                .withTestPackIdFilter(startsWith("fixml"))
                .starting(TransformType.TRANSLATE, Ingest_FixML_MessageToTradeState.class));
    }

    private ImmutableMultimap<String, Class<?>> getEventsTestPackFilter() {
        ImmutableMultimap.Builder<String, Class<?>> builder = ImmutableMultimap.builder();
        for (String key : EVENT_TEST_PACKS) {
            builder.put(key, Ingest_FpmlConfirmationToWorkflowStep.class);
        }
        return builder.build();
    }

    private PipelineTreeConfig addXMLAndSchemaMap(PipelineTreeConfig pipelineTreeConfig) {
        ImmutableMap<Class<?>, String> fixmlTypeToXmlConfigMap = ImmutableMap.<Class<?>, String>builder()
//                .put(Abstract_message_t.class, "xml-config/fixml-xml-config.json")
                .put(ExecRpt.class, "xml-config/fixml-xml-config.json")
                .build();
        ImmutableMap<Class<?>, PipelineModel.Serialisation.Format> fixmlTypeToFormatMap = ImmutableMap.<Class<?>, PipelineModel.Serialisation.Format>builder()
//                .put(Abstract_message_t.class, PipelineModel.Serialisation.Format.XML)
                .put(ExecRpt.class, PipelineModel.Serialisation.Format.XML)
                .build();

        return pipelineTreeConfig
                .withXmlConfigMap(mergeMaps(fixmlTypeToXmlConfigMap, RuneFpmlModelConfig.TYPE_TO_XML_CONFIG_MAP))
                .withXmlSchemaMap(RuneFpmlModelConfig.TYPE_TO_SCHEMA_MAP)
                .withInputSerialisationFormatMap(mergeMaps(fixmlTypeToFormatMap, RuneFpmlModelConfig.TYPE_TO_FORMAT_MAP))
                .withSortJsonPropertiesAlphabetically(false)
                .strictUniqueIds();
    }

    private static <T> ImmutableMap<Class<?>, T> mergeMaps(Map<Class<?>, ? extends T> map1, Map<Class<?>, ? extends T> map2) {
        return ImmutableMap.<Class<?>, T>builder()
                .putAll(map1)
                .putAll(map2)
                .build();
    }
}
