package conductordsl.model.system

import conductordsl.model.TaskInWorkflow
import conductordsl.model.scope.TaskListScope

class DecisionTaskCaseRegistry {
    
    var cases = mutableMapOf<String, List<TaskInWorkflow>>()

    fun addCase(
        case: String,
        block: TaskListScope.() -> Unit,
    ) {
        cases[case] =
            TaskListScope().let {
                it.block()
                it.tasks
            }
    }
}