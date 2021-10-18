package conductordsl

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import conductordsl.config.HttpSystemTaskConfig
import conductordsl.function.workflowDefine
import conductordsl.model.WorkflowDef
import conductordsl.model.reference.HttpReference
import conductordsl.model.system.HttpRequest
import io.kotest.core.spec.style.DescribeSpec

class WorkflowDefTest : DescribeSpec() {


    init {
        describe("作成テスト") {
            it("作成") {
                println(
                    message = jacksonObjectMapper()
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(makeWorkflowDef())
                )
            }
        }
    }

    private fun makeWorkflowDef(): WorkflowDef {
        HttpSystemTaskConfig.config {
            connectionTimeOut = 10000L
            readTimeOut = 10000L
        }

        return workflowDefine("purchase") {
            description = "purchase item orchestration"

            addTasks {
                /**
                 * TRY
                 */
                fork("try_fork") { fork ->
                    fork.joinLastOfForkTasks = true

                    fork.addForkTasks {
                        http("try_payment") {
                            optional = true
                            HttpRequest.Post(
                                uri = "http://localhost/payment/try",
                                body = "\${workflow.input.body.payment}",
                            )
                        }
                    }
                    fork.addForkTasks {
                        http("try_right") {
                            optional = true
                            HttpRequest.Post(
                                uri = "http://localhost/rights/try",
                                body = "\${workflow.input.body.right}"
                            )
                        }
                    }
                }

                fork("fork_after_try") { forkAfter ->
                    forkAfter.addForkTasks {
                        http("try_callback") {
                            optional = true
                            HttpRequest.Post(
                                uri = "http://localhost/callback/\${workflow.input.txId}",
                                body = mapOf("status" to "tried")
                            )
                        }
                    }

                    forkAfter.addForkTasks {
                        decision("is_try_success") { cases ->

                            // Make Variables
                            inputParameters = mapOf(
                                "sc_payment" to "${HttpReference("try_payment").statusCode}",
                                "sc_right" to "${HttpReference("try_right").statusCode}",
                            )

                            // Decision Expression
                            caseExpression = """
                                ($.sc_payment == 200 && $.sc_right == 200) ? "OK" : "NG"
                            """.trimIndent()

                            cases.addCase("OK") {

                                /**
                                 * Confirm
                                 */
                                fork("confirm_fork") { fork ->
                                    fork.joinLastOfForkTasks = true

                                    fork.addForkTasks {
                                        http("confirm_payment") {
                                            HttpRequest.Post(
                                                uri = "http://localhost/payment/confirm",
                                                body = "\${workflow.input.body.payment}",
                                            )
                                        }
                                    }
                                    fork.addForkTasks {
                                        http("confirm_right") {
                                            HttpRequest.Post(
                                                uri = "http://localhost/rights/confirm",
                                                body = "\${workflow.input.body.right}"
                                            )
                                        }
                                    }
                                }
                            }
                            cases.addCase("NG") {

                                /**
                                 * Cancel
                                 */
                                fork("cancel_fork") { fork ->
                                    fork.joinLastOfForkTasks = true

                                    fork.addForkTasks {
                                        http("cancel_payment") {
                                            HttpRequest.Post(
                                                uri = "http://localhost/payment/cancel",
                                                body = "\${workflow.input.body.payment}",
                                            )
                                        }
                                    }
                                    fork.addForkTasks {
                                        http("cancel_right") {
                                            HttpRequest.Post(
                                                uri = "http://localhost/rights/cancel",
                                                body = "\${workflow.input.body.right}"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Wait for all ends
                join("fork_after_try") {
                    listOf("fork_after_try")
                }
            }
        }
    }
}