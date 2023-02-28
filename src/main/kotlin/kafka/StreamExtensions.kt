package kafka

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler
import utils.LoggingContext
import java.time.Duration

context(LoggingContext)
fun KafkaStreams.startAndWait() {
    setUncaughtExceptionHandler { exception ->
        logger.warn("Uncaught exception handled - replacing thread", exception)
        StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse.REPLACE_THREAD
    }

    // log state transitions
    setStateListener(LoggingStateListener())

    logger.info("Starting Streams...")
    start()

    while (isHealthy() && !isRunning()) {
        Thread.sleep(200)
    }
    logger.info("Streams started")
}


context(LoggingContext)
fun KafkaStreams.closeAndWait(
    leaveGroup: Boolean = true,
    timeout: Duration = Duration.ofMinutes(2),
) = runCatching {
    logger.info("Closing Streams [leaveGroup=$leaveGroup] [timeout=$timeout] ...")
    close(
        KafkaStreams.CloseOptions().leaveGroup(leaveGroup).timeout(timeout)
    )
    Thread.sleep(10)
    logger.info("Streams closed.")
}.onFailure {
    logger.warn("Closing Streams failed: ${it.message}")
    throw it
}

fun KafkaStreams.isHealthy(): Boolean = (state() in listOf(
    KafkaStreams.State.CREATED, KafkaStreams.State.RUNNING, KafkaStreams.State.REBALANCING
))

fun KafkaStreams.isRunning(): Boolean = state() == KafkaStreams.State.RUNNING

