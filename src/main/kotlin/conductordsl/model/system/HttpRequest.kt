package conductordsl.model.system

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import conductordsl.config.HttpSystemTaskConfig

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(
    *[
        "uri",
        "method",
        "connectionTimeOut",
        "readTimeOut",
        "body",
    ]
)
data class HttpRequest(
    var uri: String,
    var method: String,
    var connectionTimeOut: Long? = null,
    var readTimeOut: Long? = null,
    var body: Any? = null,
) {

    private fun setDefaultSetting() {
        HttpSystemTaskConfig.singleton?.also { config ->
            config.connectionTimeOut?.let { connectionTimeOut = it }
            config.readTimeOut?.let { readTimeOut = it }
        }
    }

    companion object {

        fun Get(
            uri: String
        ): HttpRequest {
            return HttpRequest(
                uri = uri,
                method = "GET",
            ).also { it.setDefaultSetting() }
        }

        fun Post(
            uri: String,
            body: Any? = null
        ): HttpRequest {
            return HttpRequest(
                uri = uri,
                method = "POST",
            ).also { it.body = body }
                .also { it.setDefaultSetting() }
        }
    }

    var timeouts: Long
        @JsonIgnore
        get() = throw IllegalAccessError()
        set(mills) {
            connectionTimeOut = mills
            readTimeOut = mills
        }
}