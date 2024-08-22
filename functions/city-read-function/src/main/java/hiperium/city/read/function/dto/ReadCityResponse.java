package hiperium.city.read.function.dto;

import hiperium.cities.commons.dto.ErrorResponse;

/**
 * Represents a response object that contains information about a city.
 */
public record ReadCityResponse(

    String id,
    String name,
    String timezone,
    ErrorResponse error) {
}
