package cdm.legaldocumentation.csa.processor;

import cdm.legaldocumentation.csa.MinimumTransferAmountAmendment;
import com.regnosys.rosetta.common.translation.MappingContext;
import com.regnosys.rosetta.common.translation.MappingProcessor;
import com.regnosys.rosetta.common.translation.Path;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.path.RosettaPath;

import java.util.List;

import static org.isda.cdm.processor.CreateiQMappingProcessorUtils.PARTIES;

/**
 * CreateiQ mapping processor.
 */
@SuppressWarnings("unused")
public class MinimumTransferAmountAmendmentMappingProcessor extends MappingProcessor {

	private final ElectiveAmountElectionMappingHelper helper;

	public MinimumTransferAmountAmendmentMappingProcessor(RosettaPath modelPath, List<Path> synonymPaths, MappingContext mappingContext) {
		super(modelPath, synonymPaths, mappingContext);
		this.helper = new ElectiveAmountElectionMappingHelper(getModelPath(), getMappings(), mappingContext.getSynonymToEnumMap());
	}

	@Override
	public void map(Path synonymPath, RosettaModelObjectBuilder builder, RosettaModelObjectBuilder parent) {
		MinimumTransferAmountAmendment.MinimumTransferAmountAmendmentBuilder minimumTransferAmountAmendmentBuilder = (MinimumTransferAmountAmendment.MinimumTransferAmountAmendmentBuilder) builder;
		PARTIES.forEach(party -> helper.getElectiveAmountElection(synonymPath, party).ifPresent(minimumTransferAmountAmendmentBuilder::addPartyElections));
	}
}