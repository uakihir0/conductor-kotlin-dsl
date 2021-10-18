package conductordsl.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import conductordsl.model.scope.TaskListScope

/**
 * WorkflowDef
 * @link https://netflix.github.io/conductor/configuration/workflowdef/
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    *[
        "name",
        "description",
        "version",
        "schemaVersion",
    ]
)
data class WorkflowDef(
    var name: String,
    var version: Int = 1,
    var schemaVersion: Int = 2,
    var tasks: List<TaskInWorkflow>,

    // Options
    var description: String? = null,
    var inputParameters: Map<String, Any>? = null,
    var outputParameters: Map<String, Any>? = null,
    var failureWorkflow: String? = null,
    var restartable: Boolean? = null,
) {

    fun addTasks(
        block: TaskListScope.() -> Unit
    ): WorkflowDef {
        this.tasks = this.tasks +
                TaskListScope()
                    .also { it.block() }
                    .tasks
        return this
    }
}