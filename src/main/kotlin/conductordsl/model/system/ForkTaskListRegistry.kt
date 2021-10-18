package conductordsl.model.system

import conductordsl.model.TaskInWorkflow
import conductordsl.model.scope.TaskListScope

class ForkTaskListRegistry {

    var tasks = mutableListOf<List<TaskInWorkflow>>()
    var joinLastOfForkTasks = false

    fun addForkTasks(
        block: TaskListScope.() -> Unit
    ) {
        tasks.add(
            TaskListScope().let {
                it.block()
                it.tasks
            })
    }

    val lastOfForkTasks
        get() = tasks
            .filter { it.isNotEmpty() }
            .map { it.last() }
}