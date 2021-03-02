package uk.nhs.nhsx.emptysubmission;

import uk.nhs.nhsx.core.Environment;
import uk.nhs.nhsx.core.HttpResponses;
import uk.nhs.nhsx.core.SystemClock;
import uk.nhs.nhsx.core.auth.ApiName;
import uk.nhs.nhsx.core.auth.Authenticator;
import uk.nhs.nhsx.core.events.Events;
import uk.nhs.nhsx.core.events.PrintingJsonEvents;
import uk.nhs.nhsx.core.routing.Routing;
import uk.nhs.nhsx.core.routing.RoutingHandler;

import static uk.nhs.nhsx.core.auth.StandardAuthentication.awsAuthentication;
import static uk.nhs.nhsx.core.routing.Routing.Method.POST;
import static uk.nhs.nhsx.core.routing.Routing.path;
import static uk.nhs.nhsx.core.routing.Routing.routes;
import static uk.nhs.nhsx.core.routing.StandardHandlers.authorisedBy;
import static uk.nhs.nhsx.core.routing.StandardHandlers.withoutSignedResponses;

@SuppressWarnings("unused")
public class EmptySubmissionHandler extends RoutingHandler {

    private final Routing.Handler handler;

    @SuppressWarnings("unused")
    public EmptySubmissionHandler() {
        this(Environment.fromSystem(), new PrintingJsonEvents(SystemClock.CLOCK));
    }

    public EmptySubmissionHandler(Environment environment, Events events) {
        this(
            environment,
            awsAuthentication(ApiName.Mobile, events),
            new PrintingJsonEvents(SystemClock.CLOCK)
        );
    }

    public EmptySubmissionHandler(Environment environment,
                                  Authenticator authenticator,
                                  Events events) {
        handler = withoutSignedResponses(
            events,
            environment,
            routes(
                authorisedBy(authenticator, path(POST, "/submission/empty-submission", r -> HttpResponses.ok()))
            )
        );
    }

    @Override
    public Routing.Handler handler() {
        return handler;
    }
}
