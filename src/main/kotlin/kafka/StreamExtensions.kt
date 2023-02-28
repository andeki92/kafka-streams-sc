package kafka

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler
import utils.LoggingContext

context(LoggingContext)
fun KafkaStreams.startAndWait() {
    setUncaughtExceptionHandler { exception ->
        logger.warn("Uncaught exception handled - replacing thread", exception)
        StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD
    }

    // log state transitions
    setStateListener(LoggingStateListener())

    start()

    while (isHealthy() && !isRunning()) {
        Thread.sleep(200)
    }
}

fun KafkaStreams.isHealthy(): Boolean = (state() in listOf(
    KafkaStreams.State.CREATED,
    KafkaStreams.State.RUNNING,
    KafkaStreams.State.REBALANCING
))

fun KafkaStreams.isRunning(): Boolean =
    state() == KafkaStreams.State.RUNNING

