package hiperium.city.read.function.requests;

import hiperium.city.functions.common.annotations.ValidUUID;
import jakarta.validation.constraints.NotBlank;

/**
 * The CityDataRequest record represents a request to execute a function, encapsulating
 * the required parameters for processing within the function.
 */
public record CityDataRequest(
    @ValidUUID(message = "City ID must have a valid format.")
    @NotBlank(message = "City ID must not be blank.")
    String cityId
) {
}
