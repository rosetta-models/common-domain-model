# _Business Events - Cancelled workflow step_

_Background_
In CDM, there is the function `Create_AcceptedWorkflowStepFromInstruction` under `cdm.event.workflow:func` for creating default accepted workflow steps. This function is currently being used in DRR, along with an analog function for the cancelled workflow steps, `Create_CancelledWorkflowStepFromInstruction`, which is located in DRR. This contribution adds the `Create_CancelledWorkflowStepFromInstruction` function from DRR to CDM, since it fits the `cdm.event.workflow:func` namespace better and allows to have all these workflowStep functions located in a single place in CDM, while also making it available for CDM-only usage if necessary.

_What is being released?_

_Functions_

- Added new `Create_CancelledWorkflowStepFromInstruction` function.

_Review directions_

In CDM, select the Textual Browser and inspect each of the changes identified above.

The changes can be reviewed in PR: [#3195](https://github.com/finos/common-domain-model/pull/3195)
