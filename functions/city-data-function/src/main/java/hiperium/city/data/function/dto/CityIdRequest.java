package hiperium.city.data.function.dto;

import hiperium.city.data.function.annotations.ValidUUID;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents a request to retrieve information about a city using its unique identifier.
 */
public record CityIdRequest(@NotBlank @ValidUUID String id) {
}
