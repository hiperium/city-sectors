package hiperium.city.read.function.utils;

import hiperium.city.functions.common.exceptions.ValidationException;
import hiperium.city.functions.common.loggers.HiperiumLogger;
import hiperium.city.functions.common.requests.CityIdRequest;
import hiperium.city.functions.common.requests.FunctionRequest;
import hiperium.city.functions.common.utils.DeserializerUtil;
import hiperium.city.read.function.requests.CityDataRequest;

import java.util.Objects;

/**
 * Utility class for handling the deserialization of API Gateway requests into specific data objects.
 * This class provides methods to transform raw request payloads into domain-specific objects,
 * ensuring that the required validation is properly applied during the deserialization process.
 */
// TODO: change class name
public final class UnmarshallUtils {

    private static final HiperiumLogger LOGGER = new HiperiumLogger(UnmarshallUtils.class);

    private UnmarshallUtils() {
        throw new UnsupportedOperationException("Utility classes should not be instantiated.");
    }

    /**
     * Deserializes the given API Gateway request to construct a {@link CityDataRequest}.
     * The method ensures that the request body is not null or empty and extracts
     * the city ID and request identifier for further processing.
     *
     * @param functionRequest The API Gateway request containing the raw input data.
     * @return A {@link CityDataRequest} object containing the city ID and request identifier.
     * @throws ValidationException if the request body is missing or empty.
     */
    public static CityDataRequest deserializeRequest(final FunctionRequest functionRequest){
        LOGGER.debug("Deserializing request body: {}", functionRequest.body());
        if (Objects.isNull(functionRequest.body()) || functionRequest.body().isBlank()) {
            throw new ValidationException("Request body is missing or empty.",
                functionRequest.requestContext().requestId());
        }
        CityIdRequest cityIdRequest = DeserializerUtil.deserializeCityId(functionRequest);
        return new CityDataRequest(cityIdRequest, functionRequest.requestContext().requestId());
    }
}
