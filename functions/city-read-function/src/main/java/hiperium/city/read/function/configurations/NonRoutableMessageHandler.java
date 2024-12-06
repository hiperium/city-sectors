package hiperium.city.read.function.configurations;

import hiperium.cities.common.loggers.HiperiumLogger;
import org.springframework.cloud.function.context.DefaultMessageRoutingHandler;
import org.springframework.messaging.Message;

/**
 * NonRoutableMessageHandler is a specialized handler for processing messages that cannot be routed
 * to any specified function. It extends the DefaultMessageRoutingHandler to provide custom handling
 * for non-routable messages within the messaging system.
 */
public class NonRoutableMessageHandler extends DefaultMessageRoutingHandler {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(NonRoutableMessageHandler.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(Message<?> message) {
        // TODO: send the message to a dead letter queue.
        LOGGER.warn("The following message couldn't be routed: {}", message);
    }
}
