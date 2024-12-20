package hiperium.city.read.function.requests;

import hiperium.city.functions.common.requests.CityIdRequest;
import jakarta.validation.Valid;

/**
 * The CityDataRequest record represents a request to execute a function, encapsulating
 * the required parameters for processing within the function.
 */
public record CityDataRequest(
    @Valid
    CityIdRequest cityIdRequest,
    String requestId
) {
}
