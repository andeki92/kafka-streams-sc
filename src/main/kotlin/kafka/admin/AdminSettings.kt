package kafka.admin

import org.apache.kafka.clients.admin.AdminClientConfig
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class AdminSettings(
    private val boostrapServer: String,
    private val props: Properties? = null,
    val timeout: Duration = 10.seconds,
) {
    companion object {
        fun defaultAdminSettings(): AdminSettings = AdminSettings(
            boostrapServer = "localhost:9092",
        )
    }

    fun properties(): Properties = Properties().apply {
        props?.let { putAll(it) }
        put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, boostrapServer)
    }
}
