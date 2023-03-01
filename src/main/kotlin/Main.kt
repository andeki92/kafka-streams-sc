import arrow.continuations.SuspendApp
import kafka.admin.AdminSettings.Companion.defaultAdminSettings
import kafka.admin.createTopics
import kafka.streams.StreamsSettings.Companion.defaultStreamSettings
import kafka.streams.kafkaStreams
import kafka.topologies.addTestTopology
import org.apache.kafka.clients.admin.NewTopic
import utils.resourceScopeWithLogging


fun main() = SuspendApp {

    /**
     * Even better would be a gitOps approach to storing kafka-topics as code.
     * This is just to configure local topics.
     */
    createTopics(
        defaultAdminSettings(),
        NewTopic("foo", 1, 1),
        NewTopic("bar", 1, 1),
    )

    resourceScopeWithLogging {
        kafkaStreams(defaultStreamSettings()) {
            addTestTopology()
        }
    }
}
