package hiperium.city.read.function.entities;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import hiperium.city.read.function.commons.CommonAttributes;
import hiperium.city.read.function.commons.MetadataAttributes;

/**
 * A record that represents the response data for a sector, containing common attributes, geographical
 * coordinates, and metadata attributes.
 * <p>
 * This class is used to encapsulate information related to a sector, including its common details
 * such as name and description, as well as its geographical location defined by latitude and longitude.
 * <p>
 * Attributes from {@link CommonAttributes} provide general data about the sector, while
 * {@link MetadataAttributes} offer additional metadata, such as creation and update timestamps.
 * <p>
 * The usage of the {@link JsonUnwrapped} annotation implies that fields from the wrapped records
 * ({@link CommonAttributes} and {@link MetadataAttributes}) are serialized as if they were part of
 * this record, without nesting.
 *
 * @param commonAttributes the common attributes shared across entities, like name and description
 * @param latitude the latitude coordinate of the sector
 * @param longitude the longitude coordinate of the sector
 * @param metadataAttributes the metadata attributes providing additional information such as timestamps
 */
public record SectorEntity(
    @JsonUnwrapped
    CommonAttributes commonAttributes,

    String latitude,
    String longitude,

    @JsonUnwrapped
    MetadataAttributes metadataAttributes
) {
    public static final String SECTOR_SK_PREFIX = "SECTOR#";
}
