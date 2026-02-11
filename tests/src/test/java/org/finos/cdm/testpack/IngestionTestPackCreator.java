/*
 * Copyright (C) 2022 REGnosys Limited. All rights reserved This software is the confidential and proprietary information of REGnosys Limited.
 *
 * You shall not disclose such confidential information and shall use it only in accordance with the terms of the license agreement you entered into with REGnosys Limited.
 *
 * See https://ui.rc.rosetta-technology.io/#/documents/rosetta-core/RC-1
 */
package org.finos.cdm.testpack;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.google.inject.Module;
import com.regnosys.ingest.test.framework.ingestor.ExpectationManager;
import com.regnosys.ingest.test.framework.ingestor.IngestionReport;
import com.regnosys.ingest.test.framework.ingestor.IngestionTest;
import com.regnosys.ingest.test.framework.ingestor.IngestionTestUtil;
import com.regnosys.ingest.test.framework.ingestor.service.IngestionFactory;
import com.regnosys.ingest.test.framework.ingestor.service.IngestionService;
import com.regnosys.ingest.test.framework.ingestor.testing.Expectation;
import com.regnosys.rosetta.RosettaRuntimeModule;
import com.regnosys.rosetta.common.serialisation.RosettaObjectMapper;
import org.junit.jupiter.params.provider.Arguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IngestionTestPackCreator extends IngestionTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestionTestPackCreator.class);
    private static IngestionService ingestionService;


    static void setup(String envInstanceName, List<URL> envFile, Module runtimeModule, String serviceName) {
        if (envInstanceName != null && envFile != null && !envFile.isEmpty()) {
            initialiseIngestionFactory(envInstanceName, envFile, runtimeModule, IngestionTestUtil.getPostProcessors(runtimeModule));
            ingestionService = IngestionFactory.getInstance(envInstanceName).getService(serviceName);
        }
        else{
            initialiseIngestionFactory(runtimeModule, IngestionTestUtil.getPostProcessors(runtimeModule));
            ingestionService = IngestionFactory.getInstance().getService(serviceName);
        }

    }

    public static void main(String[] args) {
        try {
            if (args.length < 4) {
                throw new IllegalArgumentException("Usage: IngestionTestPackCreator <envInstanceName|-> <envFilesCsv|-> <serviceName> <expectationFilesCsv>");
            }
            String envInstanceName = dashToNull(args[0]);
            ImmutableList<URL> envFiles = ImmutableList.copyOf(parseCsvToUrls(dashToEmpty(args[1])));
            String serviceName = args[2];
            ImmutableList<URL> expectationFiles = ImmutableList.copyOf(parseCsvToUrls(args[3]));

            Module runtimeModule = new RosettaRuntimeModule();
            IngestionTestPackCreator testPackConfigCreator = new IngestionTestPackCreator();
            testPackConfigCreator.writeExpectations(envInstanceName, envFiles, runtimeModule ,serviceName, expectationFiles);

            System.exit(0);
        } catch (Exception e) {
            LOGGER.error("Error executing {}.main()", IngestionTestPackCreator.class.getName(), e);
            System.exit(1);
        }
    }


    public void writeExpectations(String envInstanceName, List<URL> envFile, Module runtimeModule, String serviceName, ImmutableList<URL> expectationFiles) {
        LOGGER.info("updating expectations for {}", serviceName);

        setup(envInstanceName, envFile,runtimeModule, serviceName);
        try {
            writeIngestionTestPacks(expectationFiles);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected IngestionService ingestionService() {
        return ingestionService;
    }

	@Override
	protected Class getClazz() {
		return null;
	}


	private static String dashToNull(String s) {
        return (s == null || s.equals("-")) ? null : s;
    }

    private static String dashToEmpty(String s) {
        return (s == null || s.equals("-")) ? "" : s;
    }

    private static List<URL> parseCsvToUrls(String csv) {
        if (csv == null || csv.isBlank()) return new ArrayList<>();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(p -> !p.isEmpty())
                .map(IngestionTestPackCreator::toUrl)
                .collect(Collectors.toList());
    }

    private static URL toUrl(String pathOrResource) {
        try {
            return Resources.getResource(pathOrResource);
        } catch (IllegalArgumentException e) {
            try {
                return Paths.get(pathOrResource).toUri().toURL();
            } catch (MalformedURLException ex) {
                throw new RuntimeException("Cannot resolve to URL: " + pathOrResource, ex);
            }
        }
    }

 public void writeIngestionTestPacks(ImmutableList<URL> expectationFiles) throws Throwable {
		ExpectationManager manager = new ExpectationManager(true);
		ObjectWriter writer = RosettaObjectMapper.getNewRosettaObjectMapper().writerWithDefaultPrettyPrinter();

		Stream<Arguments> expectations = readExpectationsFrom(expectationFiles);
		expectations.forEach(arguments -> {
			Object[] argsArray = arguments.get();
			String expectationFilePath = (String) argsArray[0];
			Expectation expectation = (Expectation) argsArray[1];
			try {
				LOGGER.info("---------------------- Writing Expectations for file: {} ----------------------", expectation.getFileName());

				URL url = Resources.getResource(expectation.getFileName());
				IngestionReport<?> ingested = postIngestIngestionReport(ingest(expectation, url));

				String json = writer.writeValueAsString(ingested.getRosettaModelInstance());

				manager.captureExpectation(expectationFilePath, json, ingested, expectation);
				manager.captureMappingReport(expectationFilePath, expectation, ingested.getMappingReport());

				LOGGER.info("-----------------------------------------------------------------");

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		try {
			manager.printExpectations();
			manager.writeMappingReportsSummary();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
