package hiperium.city.read.function.entities;

import hiperium.city.read.function.commons.EntityCommon;
import hiperium.city.read.function.commons.EntityMetadata;

/**
 * A record that represents the response data for a city, containing common attributes, timezone, language code,
 * country code, and metadata attributes.
 * <p>
 * This class is used to encapsulate information related to a city, including its common details such as name and description,
 * as well as the timezone, language, and country specifics.
 * <p>
 * Attributes from {@link EntityCommon} provide general data about the city, while {@link EntityMetadata}
 * offer additional metadata, such as creation and update timestamps.
 *
 * @param entityCommon the common attributes shared across entities, like name and description
 * @param timezone the timezone in which the city is located
 * @param languageCode the language code representing the primary language used in the city
 * @param countryCode the country code indicating the country the city belongs to
 * @param entityMetadata the metadata attributes providing additional information such as timestamps
 */
public record CityEntity(

    // Common attributes.
    EntityCommon entityCommon,

    String timezone,
    String languageCode,
    String countryCode,

    // Metadata attributes.
    EntityMetadata entityMetadata
) {
    public static final String CITY_PK_PREFIX = "CITY#";
}
