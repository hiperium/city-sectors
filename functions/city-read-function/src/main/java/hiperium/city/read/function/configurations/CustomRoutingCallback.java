package hiperium.city.read.function.configurations;

import hiperium.city.functions.common.utils.FunctionsUtil;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.messaging.Message;

/**
 * CustomRoutingCallback is a class that implements the {@link MessageRoutingCallback} interface
 * to provide custom routing logic for processing messages in a Spring Cloud Function context.
 * <p>
 * This implementation retrieves a routing key from the message headers and uses it to determine
 * the routing destination for the message. The key used for routing is identified by a constant
 * defined in {@link FunctionsUtil#ROUTING_PARAMETER}.
 * <p>
 * This class is primarily intended for integration with Spring Cloud Function's message routing
 * mechanism and is typically configured as a bean.
 */
public class CustomRoutingCallback implements MessageRoutingCallback {

    /**
     * Determines the routing result for a given message based on the message headers.
     *
     * @param message The message to be routed.
     * @return the routing result for the message.
     */
    @Override
    public String routingResult(Message<?> message) {
        return message.getHeaders().get(FunctionsUtil.ROUTING_PARAMETER, String.class);
    }
}
