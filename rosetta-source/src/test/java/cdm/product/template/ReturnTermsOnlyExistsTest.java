package cdm.product.template;

import cdm.observable.asset.PriceSchedule;
import cdm.observable.asset.PriceTypeEnum;
import cdm.product.asset.DividendAmountTypeEnum;
import cdm.product.asset.DividendReturnTerms;
import cdm.product.asset.PriceReturnTerms;
import cdm.product.asset.ReturnTypeEnum;
import com.regnosys.rosetta.common.validation.RosettaTypeValidator;
import com.regnosys.rosetta.common.validation.ValidationReport;
import com.rosetta.model.lib.validation.ValidationResult;
import org.isda.cdm.functions.AbstractFunctionTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ReturnTermsOnlyExistsTest extends AbstractFunctionTest {

    @Inject
    private RosettaTypeValidator validator;

    @Test
    void shouldPassConditionReturnTermsExists_ReturnTerms() {
        ReturnTerms.ReturnTermsBuilder returnTermsBuilder =
                ReturnTerms.builder()
                        .setPriceReturnTerms(PriceReturnTerms.builder().setReturnType(ReturnTypeEnum.PRICE));

        ValidationReport validationReport = validator.runProcessStep(ReturnTerms.class, returnTermsBuilder);
        ValidationResult<?> returnTermsExistsResult = getReturnTermsExistsValidationResult(validationReport);
        assertTrue(returnTermsExistsResult.isSuccess());
        assertEquals("ReturnTermsReturnTermsExists", returnTermsExistsResult.getName());
    }

    @Test
    void shouldFailConditionReturnTermsExistsBecause2AttributesAreSet_ReturnTerms() {
        ReturnTerms.ReturnTermsBuilder returnTermsBuilder =
                ReturnTerms.builder()
                        .setPriceReturnTerms(PriceReturnTerms.builder().setReturnType(ReturnTypeEnum.PRICE))
                        .setDividendReturnTerms(DividendReturnTerms.builder().setDividendAmountType(DividendAmountTypeEnum.EX_AMOUNT));

        ValidationReport validationReport = validator.runProcessStep(ReturnTerms.class, returnTermsBuilder);
        ValidationResult<?> returnTermsExistsResult = getReturnTermsExistsValidationResult(validationReport);
        assertFalse(returnTermsExistsResult.isSuccess());
        assertEquals("ReturnTermsReturnTermsExists", returnTermsExistsResult.getName());
        assertEquals("[[priceReturnTerms]] should only be set.  Set fields: [dividendReturnTerms, priceReturnTerms]", returnTermsExistsResult.getFailureReason().get());
    }

    @Test
    void shouldFailConditionReturnTermsExistsBecauseNoAttributesAreSet_ReturnTerms() {
        ReturnTerms.ReturnTermsBuilder returnTermsBuilder = ReturnTerms.builder();

        ValidationReport validationReport = validator.runProcessStep(ReturnTerms.class, returnTermsBuilder);
        ValidationResult<?> returnTermsExistsResult = getReturnTermsExistsValidationResult(validationReport);
        assertFalse(returnTermsExistsResult.isSuccess());
        assertEquals("ReturnTermsReturnTermsExists", returnTermsExistsResult.getName());
    }

    @Test
    void shouldPassConditionReturnTermsExists_PortfolioReturnTerms() {
        PortfolioReturnTerms.PortfolioReturnTermsBuilder portfolioReturnTermsBuilder =
                PortfolioReturnTerms.builder()
                        // set one attribute on super type ReturnTerms
                        .setPriceReturnTerms(PriceReturnTerms.builder().setReturnType(ReturnTypeEnum.PRICE))
                        // set one attribute on sub type PortfolioReturnTerms
                        .setInitialValuationPriceValue(getPrice());

        ValidationReport validationReport = validator.runProcessStep(PortfolioReturnTerms.class, portfolioReturnTermsBuilder);
        ValidationResult<?> returnTermsExistsResult = getReturnTermsExistsValidationResult(validationReport);

        assertFalse(returnTermsExistsResult.isSuccess());
        assertEquals("ReturnTermsReturnTermsExists", returnTermsExistsResult.getName());
        assertEquals("[[priceReturnTerms]] should only be set.  Set fields: [initialValuationPrice, priceReturnTerms]", returnTermsExistsResult.getFailureReason().get());
    }

    private static ValidationResult<?> getReturnTermsExistsValidationResult(ValidationReport validationReport) {
        return validationReport.getValidationResults().stream()
                .filter(f -> f.getName().equals("ReturnTermsReturnTermsExists"))
                .findFirst()
                .orElse(null);
    }

    private static PriceSchedule.PriceScheduleBuilder getPrice() {
        return PriceSchedule.builder()
                .setValue(BigDecimal.valueOf(0.0))
                .setPriceType(PriceTypeEnum.CASH_PRICE);
    }
}
