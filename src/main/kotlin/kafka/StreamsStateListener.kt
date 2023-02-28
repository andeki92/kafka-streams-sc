package kafka

import org.apache.kafka.streams.KafkaStreams
import utils.LoggingContext

context(LoggingContext)
class LoggingStateListener : KafkaStreams.StateListener {

    override fun onChange(newState: KafkaStreams.State?, oldState: KafkaStreams.State?) {
        logger.info("Kafka Streams state changed [newState=$newState] [oldState=$oldState]")
    }
}