WIP Project.

# Conductor Kotlin DSL

This project is designed to create various definitions for [NetflixConductor](https://github.com/Netflix/conductor)
using Kotlin DSL, which makes it possible to write various definitions in a simple way.

## Sample

The following is a sample of how distributed transactions are implemented using the TCC pattern with HTTP requests.

You can actually convert this to Json using Jackson, etc., and then make a request to Conductor.

```kotlin
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
```

## Author

[@uakihir0](https://twitter.com/uakihir0)

## License

Apache License, Version 2.0

