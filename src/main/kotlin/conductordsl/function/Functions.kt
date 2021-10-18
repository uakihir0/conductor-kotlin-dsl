package conductordsl.function

import conductordsl.model.TaskInWorkflow
import conductordsl.model.WorkflowDef
import conductordsl.model.scope.TaskListScope

inline fun workflowDefine(
    name: String,
    block: WorkflowDef.() -> Unit
): WorkflowDef {

    return WorkflowDef(
        name = name,
        tasks = listOf(),
    ).also { it.block() }
}

inline fun tasks(
    block: TaskListScope.() -> Unit
): List<TaskInWorkflow> {
    return TaskListScope()
        .let {
            it.block()
            it.tasks
        }
}
