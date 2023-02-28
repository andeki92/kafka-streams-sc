import arrow.continuations.SuspendApp
import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.resourceScope
import kafka.AdminSettings
import kafka.AdminSettings.Companion.adminSettings
import kafka.StreamsSettings
import kafka.StreamsSettings.Companion.streamsSettings
import kafka.startAndWait
import kotlinx.coroutines.awaitCancellation
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.LoggingContext
import java.lang.invoke.MethodHandles
import java.time.Duration


fun main() = SuspendApp {
    val context = object : LoggingContext {
        override val logger: Logger
            get() = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    }

    adminSettings().onRight { settings ->
        adminClient(settings) {
            createTopics(listOf(
                NewTopic("foo", 1, 1),
                NewTopic("bar", 1, 1),
            ))
        }
    }

    with(context) {
        resourceScope {
            streamsSettings().onRight { settings ->
                kafkaStreams(settings) {
                    stream<String, String>("foo")
                        .peek { key, value -> println("Received record on 'foo': [key=$key] [value=$value]") }
                        .to("bar")
                }
            }
            awaitCancellation()
        }
    }
}

context(LoggingContext)
suspend fun ResourceScope.kafkaStreams(settings: StreamsSettings, builder: StreamsBuilder.() -> Unit): KafkaStreams =
    install({
        val topology: Topology = StreamsBuilder().also { builder(it) }.build()
        KafkaStreams(topology, settings.properties()).also {
            logger.info("Starting kafka-streams.")
            it.startAndWait()
        }.also {
            logger.info("Kafka-streams started.")
        }
    }) { k, e ->
        logger.info("ExitCode $e received, closing kafka-streams.")
        k.close(Duration.ofMillis(30_000))
        logger.info("Streams closed successfully")
    }

fun adminClient(settings: AdminSettings, block: AdminClient.() -> Unit): Unit =
    AdminClient.create(settings.properties()).use { block(it) }
