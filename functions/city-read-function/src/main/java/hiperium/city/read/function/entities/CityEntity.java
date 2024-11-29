package hiperium.city.read.function.entities;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import hiperium.city.read.function.commons.CommonAttributes;
import hiperium.city.read.function.commons.MetadataAttributes;

/**
 * A record that represents the response data for a city, containing common attributes, timezone, language code,
 * country code, and metadata attributes.
 * <p>
 * This class is used to encapsulate information related to a city, including its common details such as name and description,
 * as well as the timezone, language, and country specifics.
 * <p>
 * Attributes from {@link CommonAttributes} provide general data about the city, while {@link MetadataAttributes}
 * offer additional metadata, such as creation and update timestamps.
 * <p>
 * The usage of the {@link JsonUnwrapped} annotation implies that fields from the wrapped records
 * ({@link CommonAttributes} and {@link MetadataAttributes}) are serialized as if they were part of
 * this record, without nesting.
 *
 * @param commonAttributes the common attributes shared across entities, like name and description
 * @param timezone the timezone in which the city is located
 * @param languageCode the language code representing the primary language used in the city
 * @param countryCode the country code indicating the country the city belongs to
 * @param metadataAttributes the metadata attributes providing additional information such as timestamps
 */
public record CityEntity(
    @JsonUnwrapped
    CommonAttributes commonAttributes,

    String timezone,
    String languageCode,
    String countryCode,

    @JsonUnwrapped
    MetadataAttributes metadataAttributes
) {
    public static final String CITY_PK_PREFIX = "CITY#";
}
