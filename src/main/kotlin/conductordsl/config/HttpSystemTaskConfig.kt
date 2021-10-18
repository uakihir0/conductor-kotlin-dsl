package conductordsl.config

class HttpSystemTaskConfig(
    var connectionTimeOut: Long? = null,
    var readTimeOut: Long? = null,
) {

    companion object {
        var singleton: HttpSystemTaskConfig? = null

        fun config(block: HttpSystemTaskConfig.() -> Unit) {
            if (singleton == null) {
                singleton = HttpSystemTaskConfig()
            }
            singleton?.block()
        }
    }
}