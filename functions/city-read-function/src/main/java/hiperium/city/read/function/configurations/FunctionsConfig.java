package hiperium.city.read.function.configurations;

import hiperium.city.functions.common.loggers.HiperiumLogger;
import org.springframework.cloud.function.context.DefaultMessageRoutingHandler;
import org.springframework.cloud.function.context.MessageRoutingCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MessageConverter;

/**
 * Configuration class for defining beans related to message routing and handling
 * in the messaging system.
 */
@Configuration(proxyBeanMethods = false)
public class FunctionsConfig {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(FunctionsConfig.class);

    /**
     * Creates and registers a custom {@link MessageConverter} bean for converting messages
     * in the messaging system.
     *
     * @return an instance of {@link MessageConverter} configured as {@link CustomMessageConverter}.
     */
    @Bean
    public MessageConverter customMessageConverter() {
        LOGGER.debug("Creating Custom Message Converter.");
        return new CustomMessageConverter();
    }

    /**
     * Creates a custom {@link MessageRoutingCallback} bean to determine the routing result
     * of incoming messages based on a specific header value.
     *
     * @return a {@link MessageRoutingCallback} instance that provides custom routing logic
     * based on the message header.
     */
    @Bean
    public MessageRoutingCallback customRoutingCallback() {
        LOGGER.debug("Creating Custom Routing Callback.");
        return new CustomRoutingCallback();
    }

    /**
     * Creates and registers a bean for handling non-routable messages in the messaging system.
     * This method returns an instance of {@link NonRoutableMessageHandler}, which is a custom
     * implementation of {@link DefaultMessageRoutingHandler} designed to process messages
     * that cannot be routed to a specified function.
     *
     * @return an instance of {@link DefaultMessageRoutingHandler} configured to handle non-routable messages.
     */
    @Bean
    public DefaultMessageRoutingHandler defaultMessageRoutingHandler() {
        LOGGER.debug("Creating Non-Routable Message Handler.");
        return new NonRoutableMessageHandler();
    }
}
