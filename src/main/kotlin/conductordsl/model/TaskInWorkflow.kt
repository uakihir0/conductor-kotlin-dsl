package conductordsl.model

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Task in Workflow
 * @link https://netflix.github.io/conductor/configuration/workflowdef/#tasks-within-workflow
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TaskInWorkflow(
    var name: String,
    var type: String,
    var taskReferenceName: String,

    // Options
    var optional: Boolean? = null,
    var description: String? = null,
    var inputParameters: Map<String, Any>? = null,
    var domain: Map<String, Any>? = null,

    // System Options
    var joinOn: List<String>? = null, // JOIN
    var forkTasks: List<List<TaskInWorkflow>>? = null, // FORK_JOIN
    var caseExpression: String? = null,  // DECISION
    var decisionCases: Map<String, List<TaskInWorkflow>>? = null, // DECISION
) {

    companion object {

    }
}