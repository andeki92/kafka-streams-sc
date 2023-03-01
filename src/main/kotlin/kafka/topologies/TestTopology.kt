package kafka.topologies

import org.apache.kafka.streams.StreamsBuilder
import utils.LoggingContext

context(LoggingContext)
fun StreamsBuilder.addTestTopology() {
    stream<String, String>("foo")
        .peek { key, value ->
            logger.info("Received record on 'foo': [key=$key] [value=$value]")
        }.to("bar")
}