package kafka

import arrow.core.Either
import arrow.core.raise.either
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig
import java.util.*

data class StreamsSettings(
    val applicationId: String,
    val boostrapServer: String,
    val defaultKeySerde: Serde<*> = Serdes.String(),
    val defaultValueSerde: Serde<*> = Serdes.String(),
    private val props: Properties? = null
) {
    companion object {
        fun streamsSettings(): Either<String, StreamsSettings> = either {
            StreamsSettings(
                applicationId = "kafka-poc-sc",
                boostrapServer = "localhost:9092",
                defaultKeySerde = Serdes.String(),
                defaultValueSerde = Serdes.String(),
            )
        }
    }

    fun properties(): Properties = Properties().apply {
        props?.let { putAll(it) }
        put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId)
        put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, boostrapServer)
        put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, defaultKeySerde::class.java)
        put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, defaultValueSerde::class.java)
    }
}