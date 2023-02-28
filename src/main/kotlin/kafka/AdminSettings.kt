package kafka

import org.apache.kafka.clients.admin.AdminClientConfig
import java.util.*

data class AdminSettings(
    val boostrapServer: String, private val props: Properties? = null
) {
    companion object {
        fun adminSettings(): AdminSettings = AdminSettings(
            boostrapServer = "localhost:9092",
        )
    }

    fun properties(): Properties = Properties().apply {
        props?.let { putAll(it) }
        put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, boostrapServer)
    }
}
