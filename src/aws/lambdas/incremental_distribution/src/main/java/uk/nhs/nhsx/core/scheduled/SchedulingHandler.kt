package uk.nhs.nhsx.core.scheduled

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import uk.nhs.nhsx.core.events.Event
import uk.nhs.nhsx.core.events.EventCategory.Metric
import uk.nhs.nhsx.core.events.Events
import uk.nhs.nhsx.core.scheduled.StandardHandlers.catchException
import java.time.Duration
import java.time.Duration.ofMillis

abstract class SchedulingHandler(protected val events: Events) :
    RequestHandler<ScheduledEvent, String> {

    override fun handleRequest(request: ScheduledEvent, context: Context): String {
        val start = System.currentTimeMillis()
        events.emit(javaClass, ScheduledEventStarted(javaClass.simpleName))
        return catchException(events, handler())(request, context).also {
            events.emit(javaClass, it)
            events.emit(
                javaClass,
                ScheduledEventCompleted(javaClass.simpleName, ofMillis(System.currentTimeMillis() - start))
            )
        }.toString()
    }

    abstract fun handler(): Scheduling.Handler
}

data class ScheduledEventStarted(val handler: String) : Event(Metric)
data class ScheduledEventCompleted(val handler: String, val runtime: Duration) : Event(Metric)
