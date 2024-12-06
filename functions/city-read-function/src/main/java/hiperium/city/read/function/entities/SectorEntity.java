package hiperium.city.read.function.entities;

import hiperium.city.read.function.commons.EntityCommon;
import hiperium.city.read.function.commons.EntityMetadata;

/**
 * A record that represents the response data for a sector, containing common attributes, geographical
 * coordinates, and metadata attributes.
 * <p>
 * This class is used to encapsulate information related to a sector, including its common details
 * such as name and description, as well as its geographical location defined by latitude and longitude.
 * <p>
 * Attributes from {@link EntityCommon} provide general data about the sector, while
 * {@link EntityMetadata} offer additional metadata, such as creation and update timestamps.
 *
 * @param entityCommon the common attributes shared across entities, like name and description
 * @param latitude the latitude coordinate of the sector
 * @param longitude the longitude coordinate of the sector
 * @param entityMetadata the metadata attributes providing additional information such as timestamps
 */
public record SectorEntity(

    // Common attributes.
    EntityCommon entityCommon,

    String latitude,
    String longitude,

    // Metadata attributes.
    EntityMetadata entityMetadata
) {
    public static final String SECTOR_SK_PREFIX = "SECTOR#";
}
