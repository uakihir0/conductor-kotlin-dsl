package conductordsl.model.scope

import conductordsl.model.TaskInWorkflow
import conductordsl.model.system.DecisionTaskCaseRegistry
import conductordsl.model.system.ForkTaskListRegistry
import conductordsl.model.system.HttpRequest

class TaskListScope {

    var tasks = mutableListOf<TaskInWorkflow>()

    /**
     * Add a TaskInWorkflow
     * タスクを追加
     */
    fun of(
        ref: String,
        type: String,
        block: TaskInWorkflow.() -> Unit
    ): TaskInWorkflow {

        return TaskInWorkflow(
            name = ref,
            type = type,
            taskReferenceName = ref,
        ).also {
            it.block()
            tasks.add(it)
        }
    }

    /**
     * Add a SimpleTask
     * シンプルタスクを追加
     */
    fun simple(
        ref: String,
        block: TaskInWorkflow.() -> Unit
    ): TaskInWorkflow {
        return of(
            ref = ref,
            type = "SIMPLE",
            block = block,
        )
    }

    /**
     * Add a HTTP SystemTask
     * HTTP リクエストのシステムタスクを作成
     */
    fun http(
        ref: String,
        block: TaskInWorkflow.() -> HttpRequest
    ): TaskInWorkflow {

        return TaskInWorkflow(
            name = ref,
            type = "HTTP",
            taskReferenceName = ref,
        ).also {
            it.inputParameters =
                mapOf("http_request" to it.block())
            tasks.add(it)
        }
    }

    /**
     * Add a Fork SystemTask
     * Fork システムタスクを作成
     */
    fun fork(
        ref: String,
        block: TaskInWorkflow.(ForkTaskListRegistry) -> Unit
    ): TaskInWorkflow {

        return TaskInWorkflow(
            name = ref,
            type = "FORK_JOIN",
            taskReferenceName = ref,
        ).also { t ->
            val registry =
                ForkTaskListRegistry()
                    .also { t.block(it) }
            t.forkTasks = registry.tasks
            tasks.add(t)

            // Add join system task
            if (registry.joinLastOfForkTasks) {
                val name = (ref + "_join")
                TaskInWorkflow(
                    name = name,
                    type = "JOIN",
                    taskReferenceName = name,
                ).also { j ->
                    j.joinOn = registry.lastOfForkTasks
                        .map { it.taskReferenceName }
                    tasks.add(j)
                }
            }
        }
    }

    /**
     * Add a Join SystemTask
     * Join システムタスクを作成
     */
    fun join(
        ref: String,
        block: TaskInWorkflow.() -> List<String>
    ): TaskInWorkflow {

        return TaskInWorkflow(
            name = ref,
            type = "JOIN",
            taskReferenceName = ref,
        ).also {
            it.joinOn = it.block()
            tasks.add(it)
        }
    }

    /**
     * Add a Decision SystemTask
     * Decision システムタスクを作成
     */
    fun decision(
        ref: String,
        block: TaskInWorkflow.(DecisionTaskCaseRegistry) -> Unit
    ): TaskInWorkflow {

        return TaskInWorkflow(
            name = ref,
            type = "DECISION",
            taskReferenceName = ref,
        ).also { t ->
            val registry =
                DecisionTaskCaseRegistry()
                    .also { t.block(it) }
            t.decisionCases = registry.cases
            tasks.add(t)
        }
    }
}