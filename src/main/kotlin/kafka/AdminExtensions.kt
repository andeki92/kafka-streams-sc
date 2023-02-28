package kafka

import org.apache.kafka.clients.admin.AdminClient


fun adminClient(settings: AdminSettings, block: AdminClient.() -> Unit): Unit =
    AdminClient.create(settings.properties()).use { block(it) }
