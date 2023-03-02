package kafka.admin

import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import java.util.concurrent.TimeUnit


fun createTopics(
    settings: AdminSettings = AdminSettings.defaultAdminSettings(),
    vararg topics: NewTopic,
) {
    AdminClient.create(settings.properties()).use { adminClient ->
        adminClient.createTopics(topics.toList())
            .all().get(settings.timeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
    }
}