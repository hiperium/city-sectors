package hiperium.city.read.function.requests;

import hiperium.cities.common.annotations.ValidUUID;
import jakarta.validation.constraints.NotBlank;

/**
 * The FunctionRequest record represents a request to execute a function, encapsulating
 * the required parameters for processing within the function. The parameters include
 * the name of the function to be executed and the identifier of the city, both represented
 * as strings.
 */
public record FunctionRequest(
    @NotBlank(message = "Function name must not be blank.")
    String functionName,

    @ValidUUID(message = "City ID must have a valid format.")
    @NotBlank(message = "City ID must not be blank.")
    String cityId
) {
}
