package hiperium.city.read.function.utils;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import hiperium.city.functions.common.enums.ErrorCode;
import hiperium.city.functions.common.exceptions.CityException;
import hiperium.city.functions.common.exceptions.ValidationException;
import hiperium.city.functions.common.loggers.HiperiumLogger;
import hiperium.city.functions.common.utils.FunctionUtils;
import hiperium.city.read.function.requests.CityDataRequest;

import java.io.IOException;
import java.util.Objects;

/**
 * Utility class for converting function-specific objects and handling
 * related operations. This class provides methods for deserializing
 * request events into business objects.
 * This class is not meant to be instantiated.
 */
public final class UnmarshallUtils {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(UnmarshallUtils.class);

    private UnmarshallUtils() {
        throw new UnsupportedOperationException("Utility classes should not be instantiated.");
    }

    /**
     * Converts an APIGatewayProxyRequestEvent to a CityDataRequest by deserializing the request body.
     *
     * @param event the APIGatewayProxyRequestEvent containing the request body to be deserialized into a CityDataRequest object
     * @return a CityDataRequest object constructed from the deserialized request body
     * @throws CityException if the deserialization process fails
     */
    public static CityDataRequest deserializeRequest(final APIGatewayProxyRequestEvent event){
        LOGGER.debug("Deserializing request body: {}", event.getBody());
        if (Objects.isNull(event.getBody()) || event.getBody().isBlank()) {
            throw new ValidationException("Request body is missing or empty.", event.getRequestContext().getRequestId());
        }
        try {
            return FunctionUtils.OBJECT_MAPPER.readValue(event.getBody(), CityDataRequest.class);
        } catch (IOException exception) {
            throw new CityException("Couldn't deserialize request body: " + event.getBody(),
                ErrorCode.INTERNAL_002, exception);
        }
    }
}
