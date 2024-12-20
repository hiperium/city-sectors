package hiperium.city.read.function.configurations;

import hiperium.city.functions.common.loggers.HiperiumLogger;
import hiperium.city.functions.common.requests.FunctionRequest;
import hiperium.city.functions.common.utils.DeserializerUtil;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.util.MimeType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * CustomMessageConverter is a custom implementation of {@link AbstractMessageConverter}
 * that is responsible for converting message payloads within the messaging system.
 */
public class CustomMessageConverter extends AbstractMessageConverter {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(CustomMessageConverter.class);

    public CustomMessageConverter() {
        super(new MimeType("application", "json"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean supports(@NonNull Class<?> clazz) {
        return FunctionRequest.class.isAssignableFrom(clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object convertToInternal(@NonNull Object payload, MessageHeaders headers, Object conversionHint) {
        return payload;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object convertFromInternal(@NonNull Message<?> message, @NonNull Class<?> targetClass, Object conversionHint) {
        LOGGER.debug("Converting message to internal: {}", message);

        Object payload = message.getPayload();
        if (payload instanceof FunctionRequest) {
            return payload;
        } else if (payload instanceof byte[]) {
            String json = new String((byte[]) payload, StandardCharsets.UTF_8);
            try {
                return DeserializerUtil.fromJson(json);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException("Unsupported payload type: " + payload.getClass());
        }
    }
}
