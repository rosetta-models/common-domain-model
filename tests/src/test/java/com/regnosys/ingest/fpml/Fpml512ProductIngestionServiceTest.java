package com.regnosys.ingest.fpml;

import cdm.event.common.TradeState;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import com.regnosys.ingest.test.framework.ingestor.IngestionTest;
import com.regnosys.ingest.test.framework.ingestor.IngestionTestUtil;
import com.regnosys.ingest.test.framework.ingestor.service.IngestionService;
import org.finos.cdm.CdmRuntimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.provider.Arguments;

import java.net.URL;
import java.util.stream.Stream;

import static com.regnosys.ingest.IngestionEnvUtil.getFpml5ConfirmationToTradeState;

public class Fpml512ProductIngestionServiceTest extends IngestionTest<TradeState> {

	private static final String COMMODITY_DIR = "/fpml-5-12/products/commodity/";
	private static final String CREDIT_DIR = "/fpml-5-12/products/credit/";
	private static final String RATES_DIR = "/fpml-5-12/products/rates/";
	private static final String EQUITY_DIR = "/fpml-5-12/products/equity/";
	private static final String FX_DIR = "/fpml-5-12/products/fx/";
	private static final String REPO_DIR = "/fpml-5-12/products/repo/";

	private static ImmutableList<URL> EXPECTATION_FILES = ImmutableList.<URL>builder()
		.add(expectationsFilesURL(COMMODITY_DIR))
		.add(expectationsFilesURL(CREDIT_DIR))
		.add(expectationsFilesURL(RATES_DIR))
		.add(expectationsFilesURL(EQUITY_DIR))
		.add(expectationsFilesURL(FX_DIR))
		.add(expectationsFilesURL(REPO_DIR))
		.build();

	private static IngestionService ingestionService;

	@BeforeAll
	static void setup() {
		CdmRuntimeModule runtimeModule = new CdmRuntimeModule();
		initialiseIngestionFactory(runtimeModule, IngestionTestUtil.getPostProcessors(runtimeModule));
		ingestionService = getFpml5ConfirmationToTradeState();
	}
	
	@Override
	protected Class<TradeState> getClazz() {
		return TradeState.class;
	}

	@Override
	protected IngestionService ingestionService() {
		return ingestionService;
	}

	@SuppressWarnings("unused")//used by the junit parameterized test
	private static Stream<Arguments> fpMLFiles() {
		return readExpectationsFrom(EXPECTATION_FILES);
	}
}
