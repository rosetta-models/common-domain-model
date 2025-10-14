package cdm.legaldocumentation.csa.processor;

import cdm.base.staticdata.asset.common.ISOCurrencyCodeEnum;
import cdm.legaldocumentation.csa.MinimumTransferAmount;
import cdm.legaldocumentation.csa.ElectiveAmountEnum;
import cdm.legaldocumentation.csa.MinimumTransferAmount;
import cdm.observable.asset.Money;
import com.regnosys.rosetta.common.translation.Mapping;
import com.regnosys.rosetta.common.translation.Path;
import com.regnosys.rosetta.common.translation.SynonymToEnumMap;
import com.rosetta.model.lib.path.RosettaPath;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.regnosys.rosetta.common.translation.MappingProcessorUtils.setValueAndOptionallyUpdateMappings;
import static com.regnosys.rosetta.common.translation.MappingProcessorUtils.setValueAndUpdateMappings;
import static org.isda.cdm.processor.CreateiQMappingProcessorUtils.*;

public class MinimumTransferAmountMappingHelper {

    private static final String ZERO = "zero";

    private final RosettaPath path;
    private final List<Mapping> mappings;
    private final SynonymToEnumMap synonymToEnumMap;

    public MinimumTransferAmountMappingHelper(RosettaPath path, List<Mapping> mappings, SynonymToEnumMap synonymToEnumMap) {
        this.path = path;
        this.mappings = mappings;
        this.synonymToEnumMap = synonymToEnumMap;
    }

    public Optional<MinimumTransferAmount> getElectiveAmountElection(Path synonymPath, String party) {
        MinimumTransferAmount.MinimumTransferAmountBuilder minimumTransferAmountBuilder = MinimumTransferAmount.builder();
        Money.MoneyBuilder moneyBuilder = Money.builder();

        setValueAndUpdateMappings(synonymPath.addElement(party + "_amount"),
                (value) -> moneyBuilder.setValue(new BigDecimal(value)), mappings, path);

        setValueAndOptionallyUpdateMappings(synonymPath.addElement(party + "_currency"),
                (value) -> setIsoCurrency(synonymToEnumMap.getEnumValue(ISOCurrencyCodeEnum.class, value),
                        cur -> moneyBuilder.getOrCreateUnit().setCurrency(cur)),
                mappings,
                path);

        if (moneyBuilder.hasData()) {
            minimumTransferAmountBuilder.set
        }

        setValueAndUpdateMappings(synonymPath.addElement(party + "_" + synonymPath.getLastElement().getPathName()),
                (value) -> {
                    minimumTransferAmountBuilder.setParty(toCounterpartyRoleEnum(party));
                    if (ZERO.equals(value)) {
                        minimumTransferAmountBuilder.setElectiveAmount(ElectiveAmountEnum.ZERO);
                    }
                }, mappings, path);

        setValueAndUpdateMappings(synonymPath.addElement(party + "_specify"),
                value -> minimumTransferAmountBuilder.setCustomElection(removeHtml(value)), mappings, path);

        return minimumTransferAmountBuilder.hasData() ? Optional.of(minimumTransferAmountBuilder.build()) : Optional.empty();
    }
}
