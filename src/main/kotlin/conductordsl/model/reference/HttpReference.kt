package conductordsl.model.reference

class HttpReference(
    val ref: String
) {
    val statusCode: StatusCode
        get() = StatusCode(this)

    class StatusCode(private val http: HttpReference) {
        override fun toString(): String {
            return "\${${http.ref}.output.response.statusCode}"
        }
    }
}