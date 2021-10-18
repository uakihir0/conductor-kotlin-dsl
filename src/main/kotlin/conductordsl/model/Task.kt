package conductordsl.model

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Task
 * @link https://netflix.github.io/conductor/configuration/taskdef/
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Task(
    var name: String,
    var description: String? = null,
    var retryCount: Long,
    var timeoutSeconds: Long,
    var pollTimeoutSeconds: Long,
    var inputKeys: List<String>,
    var outputKeys: List<String>,
    var timeoutPolicy: String,
    var retryLogic: String,
    var retryDelaySeconds: Long,
    var responseTimeoutSeconds: Long,
    var concurrentExecLimit: Long,
    var rateLimitFrequencyInSeconds: Long,
    var rateLimitPerFrequency: Long,
) {
}