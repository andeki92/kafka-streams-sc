package kafka

import arrow.fx.coroutines.ExitCase
import arrow.fx.coroutines.ResourceScope
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import utils.LoggingContext

context(LoggingContext)
suspend fun ResourceScope.kafkaStreams(settings: StreamsSettings, block: StreamsBuilder.() -> Unit): KafkaStreams =
    install({
        val topology: Topology = StreamsBuilder().also { block(it) }.build()
        KafkaStreams(topology, settings.properties()).also { it.startAndWait() }
    }) { streams, e: ExitCase ->
        when (e) {
            is ExitCase.Failure -> logger.warn("ExitCode ${e.failure.message} received. Closing kafka-streams.")
            is ExitCase.Cancelled -> logger.warn("ExitCode ${e.exception.message} received. Closing kafka-streams.")
            is ExitCase.Completed -> logger.info("ExitCode $e received. Closing kafka-streams")
        }
        streams.closeAndWait()
    }