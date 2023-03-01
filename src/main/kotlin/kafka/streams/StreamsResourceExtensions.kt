package kafka.streams

import arrow.fx.coroutines.ExitCase
import arrow.fx.coroutines.ResourceScope
import kotlinx.datetime.Clock
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler
import utils.LoggingContext
import kotlin.time.toJavaDuration


/**
 * This method provides a way to install (start) and tear down a kafka-streams instance.
 *
 * Using Kotlin DSLs it provides a way to build the streams-topology before starting and handling
 * gracefully shutdown down the stream again. Configuration parameters are handled by the [StreamsSettings].
 */
context(LoggingContext)
suspend fun ResourceScope.kafkaStreams(settings: StreamsSettings, block: StreamsBuilder.() -> Unit): KafkaStreams =
    install({
        val topology: Topology = StreamsBuilder().also { block(it) }.build()
        KafkaStreams(topology, settings.properties()).also { it.startAndWait(settings) }
    }) { streams, e: ExitCase ->
        when (e) {
            is ExitCase.Failure -> logger.warn("ExitCode ${e.failure.message} received. Closing kafka-streams.")
            is ExitCase.Cancelled -> logger.warn("ExitCode ${e.exception.message} received. Closing kafka-streams.")
            is ExitCase.Completed -> logger.info("ExitCode $e received. Closing kafka-streams")
        }
        streams.closeAndWait(settings)
    }

context(LoggingContext)
private fun KafkaStreams.startAndWait(settings: StreamsSettings) {
    configure(settings)

    val streamsStartupTime  = Clock.System.now()

    logger.info("Starting Kafka Streams @$streamsStartupTime")

    start()

    while (isHealthy() && !isRunning()) {
        if (streamsStartupTime.plus(settings.startupTimeout) <= Clock.System.now()) {
            throw IllegalStateException("Failed to start kafka-streams within ${settings.startupTimeout.inWholeSeconds} seconds")
        }
        Thread.sleep(50)
    }

    if (!isRunning()) {
        throw IllegalStateException("Failed to start kafka-streams. Streams-state is ${state()}")
    }

    logger.info("Kafka Streams started in ${Clock.System.now().minus(streamsStartupTime).inWholeMilliseconds}ms")
}

context(LoggingContext)
private fun KafkaStreams.configure(settings: StreamsSettings) {
    // ensure stream thread exceptions result in replacing the failing thread.
    setUncaughtExceptionHandler { exception ->
        logger.warn("Uncaught exception handled - replacing thread", exception)
        StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD
    }

    setStateListener(LoggingStateListener())
}

context(LoggingContext)
private fun KafkaStreams.closeAndWait(settings: StreamsSettings) = runCatching {
    logger.info("Closing kafka-streams [leaveGroup=${settings.leaveGroup}] [timeout=${settings.gracefulShutdownTimeout}]")
    close(
        KafkaStreams.CloseOptions().leaveGroup(settings.leaveGroup).timeout(settings.gracefulShutdownTimeout.toJavaDuration())
    )
    Thread.sleep(10) // this is only to provide correctly ordered logging
    logger.info("Kafka-streams successfully closed.")
}.onFailure {
    logger.warn("Closing kafka-streams failed: ${it.message}")
    throw it
}

/**
 * Check if the stream is in a healthy state.
 */
private fun KafkaStreams.isHealthy(): Boolean = (state() in listOf(
    KafkaStreams.State.CREATED, KafkaStreams.State.RUNNING, KafkaStreams.State.REBALANCING
))

/**
 * Check if the stream is in a [KafkaStreams.State.RUNNING] state
 */
private fun KafkaStreams.isRunning(): Boolean = state() == KafkaStreams.State.RUNNING

