import arrow.continuations.SuspendApp
import arrow.fx.coroutines.resourceScope
import kafka.AdminSettings.Companion.adminSettings
import kafka.StreamsSettings.Companion.streamsSettings
import kafka.adminClient
import kafka.kafkaStreams
import kafka.topologies.addTestTopology
import kotlinx.coroutines.awaitCancellation
import org.apache.kafka.clients.admin.NewTopic
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.LoggingContext
import java.lang.invoke.MethodHandles

val loggingContext = object : LoggingContext {
    override val logger: Logger
        get() = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
}

fun main() = SuspendApp {
    adminClient(adminSettings()) {
        createTopics(
            listOf(
                NewTopic("foo", 1, 1),
                NewTopic("bar", 1, 1),
            )
        )
    }

    // TODO: Figure out a way to inline the resourceScope and loggingContext
    resourceScope {
        with(loggingContext) {
            kafkaStreams(streamsSettings()) {
                addTestTopology()
            }
        }

        awaitCancellation()
    }
}

