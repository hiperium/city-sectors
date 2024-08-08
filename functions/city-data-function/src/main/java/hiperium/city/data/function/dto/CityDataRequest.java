package hiperium.city.data.function.dto;

import hiperium.cities.commons.annotations.ValidUUID;
import jakarta.validation.constraints.NotBlank;

/**
 * Represents a request to retrieve information about a city using its unique identifier.
 */
public record CityDataRequest(@NotBlank @ValidUUID String cityId) {
}
