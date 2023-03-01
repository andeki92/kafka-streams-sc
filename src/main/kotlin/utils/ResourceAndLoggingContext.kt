package utils

import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.resourceScope
import kotlinx.coroutines.awaitCancellation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

class ResourceAndLoggingContext(
    private val scope: ResourceScope,
    private val logging: LoggingContext,
) : ResourceScope by scope, LoggingContext by logging


/**
 * Wrapper method to provide both a resourceScope and a loggingContext within the same block.
 */
suspend fun <A> resourceScopeWithLogging(block: suspend ResourceAndLoggingContext.() -> A): A =
    resourceScope {
        val loggingContext = object : LoggingContext {
            override val logger: Logger
                get() = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
        }

        block(ResourceAndLoggingContext(this, loggingContext))

        // ensure we await coroutine cancellations
        awaitCancellation()
    }
