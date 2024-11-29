package hiperium.city.read.function.requests;

import hiperium.cities.commons.annotations.ValidUUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

/**
 * Represents a request to retrieve information about a city using its unique identifier.
 */
public record FunctionRequest(
    @ValidUUID(message = "City ID must have a valid format.")
    @NotEmpty(message = "City ID must not be empty.")
    @NotBlank(message = "City ID must not be blank.")
    String cityId) {
}
