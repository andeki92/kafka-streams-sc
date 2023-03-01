package kafka.streams


import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class StreamsSettings(
    private val applicationId: String,
    private val boostrapServer: String,
    private val defaultKeySerde: Serde<*> = Serdes.String(),
    private val defaultValueSerde: Serde<*> = Serdes.String(),
    private val props: Properties? = null,

    /* Startup configurations */
    val startupTimeout: Duration = 30.seconds,

    /* Shutdown configurations */
    val gracefulShutdownTimeout: Duration = 2.minutes,
    val leaveGroup: Boolean = true,
) {
    companion object {

        // TODO: Load from application.yaml or similar
        fun defaultStreamSettings(): StreamsSettings = StreamsSettings(
            applicationId = "kafka-poc-sc",
            boostrapServer = "localhost:9092",
            defaultKeySerde = Serdes.String(),
            defaultValueSerde = Serdes.String(),
        )
    }

    fun properties(): Properties = Properties().apply {
        props?.let { putAll(it) }
        put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId)
        put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, boostrapServer)
        put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, defaultKeySerde::class.java)
        put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, defaultValueSerde::class.java)
    }
}