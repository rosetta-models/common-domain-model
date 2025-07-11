# _Event Model - Qualification Functions Relocated_

_Background_

Event Qualification Functions belong in the `cdm.event.qualification:func` namespace. Several functions of this type were instead identified in the `cdm.event.common:func` namespace. These functions need to be relocated to the correct namespace.

_What is being released?_

The following functions are moving from `cdm.event.common:func` to cdm.event.qualification:func:

- Qualify_Repurchase
- Qualify_Roll
- Qualify_Cancellation
- Qualify_PairOff
- Qualify_Shaping
- Qualify_PartialDelivery
- Qualify_Reprice
- Qualify_Adjustment
- Qualify_Substitution
- Qualify_OnDemandPayment

No code changes were made to any of the functions.

Previously the functions in `cdm.event.qualification:func` namespace were in alphabetical order. This order has lapsed over the last few versions so as part of this update the functions have been reordered in alphabetical order.

_Review Directions_

Changes can be reviewed in PR: https://github.com/finos/common-domain-model/pull/3851

# _Product Model - AveragingFeature to Support Asian Options_

_Background_

Asian options were previously not covered in the model. The FpML mappings were pointing to the Asian type, which, however, was an orphan type and not referenced anywhere in the model.

To address this, it has been proposed to create a new `AveragingFeature` type that encapsulates `AveragingCalculation` and incorporates the attributes mapped from FpML. This new type is now referenced within `OptionFeature`.

_What is being released?_

The following type has been added to the model:

- `AveragingFeature`

The following type has been updated:

- `OptionFeature`
- `OptionPayout` 

The following FpML Mapping has been updated:

- `AveragingFeature` (formerly called `Asian`)

_Review Directions_

Changes can be reviewed in PR: [#3827](https://github.com/finos/common-domain-model/pull/3827)

# _Infrastructure - Release Process updates following Maven Central Migration_

_Background_

The Maven Central infrastructure is undergoing a major migration, from `oss.sonatype.org` to `central.sonatype.com`. This impacts the release process for all projects hosting artifacts using Maven Central.

Further information is provided by Sonatype:
https://central.sonatype.org/faq/what-is-different-between-central-portal-and-legacy-ossrh/#self-service-migration

_What is being released?_

The release process has been updated to build and deploy to `central.sonatype.com` instead of `oss.sonatype.org`.

_Review Directions_

No changes made to model.

Changes can be reviewed in PR: https://github.com/finos/common-domain-model/pull/3830
