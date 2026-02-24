package org.finos.cdm.testpack;

import cdm.ingest.fpml.confirmation.message.functions.Ingest_FpmlConfirmationToTradeState;
import cdm.ingest.fpml.confirmation.message.functions.Ingest_FpmlConfirmationToWorkflowStep;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.io.Resources;
import com.google.inject.Injector;
import com.regnosys.functions.FunctionCreator;
import org.finos.cdm.CdmRuntimeModule;
import org.finos.cdm.functions.FunctionInputCreator;
import org.finos.cdm.functions.SecLendingFunctionInputCreator;
import com.regnosys.rosetta.common.transform.TransformType;
import com.regnosys.runefpml.RuneFpmlModelConfig;
import com.regnosys.testing.pipeline.PipelineConfigWriter;
import com.regnosys.testing.pipeline.PipelineTestPackFilter;
import com.regnosys.testing.pipeline.PipelineTreeConfig;
import jakarta.inject.Inject;
import org.finos.cdm.CdmRuntimeModuleTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

            testPackConfigCreator.run();

           // testPackConfigCreator.runIngestion();
            testPackConfigCreator.runFunctionCreators();

            System.exit(0);
        } catch (Exception e) {
            LOGGER.error("Error executing {}.main()", CdmTestPackCreator.class.getName(), e);
            System.exit(1);
        }
    }

   /* private void runIngestion() throws Exception {

        IngestionTestPackCreator fisIngestionTest = new IngestionTestPackCreator();
        CdmRuntimeModule runtimeModule = new CdmRuntimeModule();

        List<URL> envfile = Collections.singletonList(Resources.getResource("ingestions/isla-ingestions.json"));
        String fisSampleFilesDir = "cdm-sample-files/fis/";
        ImmutableList<URL> fisExpectationFiles = ImmutableList.<URL>builder()
                .add(Resources.getResource(fisSampleFilesDir + "expectations.json"))
                .build();

        //    fisIngestionTest.writeExpectations("target/ISLA", envfile, runtimeModule, "FIS_TRADE" , fisExpectationFiles);

        IngestionTestPackCreator oreIngestionTest = new IngestionTestPackCreator();
        envfile = Collections.singletonList(Resources.getResource("ingestions/ingestions.json"));

        String oreSampleFilesDir = "cdm-sample-files/ore-1-0-39/";
        ImmutableList<URL> oreExpectationFiles = ImmutableList.<URL>builder()
                .add(Resources.getResource(oreSampleFilesDir + "expectations.json"))
                .build();

        // Disabled for now due to NPE in underlying framework when env params are null
        oreIngestionTest.writeExpectations("target/ORE", envfile, runtimeModule, "ORE_1_0_39", oreExpectationFiles);

    }*/


    private void runFunctionCreators() throws Exception {
        FunctionInputCreator functionInputCreator = new FunctionInputCreator();
        functionInputCreator.run(Optional.ofNullable(System.getenv("TEST_WRITE_BASE_PATH")).map(Paths::get));

        SecLendingFunctionInputCreator secLendingFunctionInputCreator = new SecLendingFunctionInputCreator();
        secLendingFunctionInputCreator.run(Optional.ofNullable(System.getenv("TEST_WRITE_BASE_PATH")).map(Paths::get));

        FunctionCreator functionCreator = new FunctionCreator();
        functionCreator.run();
    }


    private void run() throws IOException {
        pipelineConfigWriter.writePipelinesAndTestPacks(createTreeConfig());
    }

    private PipelineTreeConfig createTreeConfig() {
        PipelineTestPackFilter filter = PipelineTestPackFilter.create()
                .withTestPacksSpecificToFunctions(getEventsTestPackFilter());

        return new PipelineTreeConfig()
                .starting(TransformType.TRANSLATE, Ingest_FpmlConfirmationToTradeState.class)
                .starting(TransformType.TRANSLATE, Ingest_FpmlConfirmationToWorkflowStep.class)
                .withInputSerialisationFormatMap(RuneFpmlModelConfig.TYPE_TO_FORMAT_MAP)
                .withXmlConfigMap(RuneFpmlModelConfig.TYPE_TO_XML_CONFIG_MAP)
                .withTestPackFilter(filter)
                .strictUniqueIds()
                .withSortJsonPropertiesAlphabetically(false);
    }

    private ImmutableMultimap<String, Class<?>> getEventsTestPackFilter() {
        ImmutableMultimap.Builder<String, Class<?>> builder = ImmutableMultimap.builder();
        for (String key : EVENT_TEST_PACKS) {
            builder.put(key, Ingest_FpmlConfirmationToWorkflowStep.class);
        }
        return builder.build();
    }
}
