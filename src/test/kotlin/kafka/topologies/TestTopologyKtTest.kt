package kafka.topologies

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kafka.streams.StreamsSettings
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.TopologyTestDriver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import utils.LoggingContext
import java.lang.invoke.MethodHandles

fun topologyWithLogging(block: context (LoggingContext) StreamsBuilder.() -> Unit): Topology {
    val loggingContext = object : LoggingContext {
        override val logger: Logger
            get() = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    }
    return StreamsBuilder().apply { block(loggingContext, this) }.build()
}

class TestTopologyKtTest : FunSpec({
    val properties = StreamsSettings.defaultStreamSettings().properties()
    val topology = topologyWithLogging {
        addTestTopology()
    }

    val testDriver = TopologyTestDriver(topology, properties)

    test("records are forwarded from 'foo' to 'bar'") {
        val fooTopic = testDriver.createInputTopic("foo", StringSerializer(), StringSerializer())
        val barTopic = testDriver.createOutputTopic("bar", StringDeserializer(), StringDeserializer())

        fooTopic.pipeInput("key:1", "value:1")
        fooTopic.pipeInput("key:2", "value:2")

        val record1 = barTopic.readKeyValue()
        val record2 = barTopic.readKeyValue()

        record1.key shouldBe "key:1"
        record1.value shouldBe "value:1"
        record2.key shouldBe "key:2"
        record2.value shouldBe "value:2"
    }

})