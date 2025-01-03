package hiperium.city.read.function.mappers;

import hiperium.city.functions.common.enums.ErrorCode;
import hiperium.city.functions.common.enums.RecordStatus;
import hiperium.city.functions.common.exceptions.CityException;
import hiperium.city.functions.common.utils.DateTimeUtil;
import hiperium.city.read.function.commons.EntityCommon;
import hiperium.city.read.function.commons.EntityMetadata;
import hiperium.city.read.function.commons.TimeZoneProvider;
import hiperium.city.read.function.entities.CityEntity;
import hiperium.city.read.function.entities.SectorEntity;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Interface for mapping data from a map of attributes to CityEntity and SectorEntity objects.
 * Uses MapStruct for mapping configuration and Spring for component modeling.
 */
@Mapper(componentModel = "spring")
public interface FunctionMapper {

    /**
     * Maps the provided data from a map of attribute values to a CityEntity object, extracting specific
     * fields such as common entity attributes, timezone, language code, country code, and metadata.
     *
     * @param item a map of attribute names to their corresponding values, representing the data associated
     *             with a city entity. The map must contain specific attributes like timezone, languageCode,
     *             and countryCode for successful mapping.
     * @return a CityEntity object constructed from the provided map, containing the mapped values of the
     *         city's common attributes, timezone, language code, country code, and metadata attributes.
     */
    @Mapping(target = "entityCommon",   expression = "java(mapCommonAttributes(item))")
    @Mapping(target = "timezone",       expression = "java(getAttributeValue(item, \"timezone\"))")
    @Mapping(target = "languageCode",   expression = "java(getAttributeValue(item, \"languageCode\"))")
    @Mapping(target = "countryCode",    expression = "java(getAttributeValue(item, \"countryCode\"))")
    @Mapping(target = "entityMetadata", expression = "java(mapMetadataAttributes(item, timeZoneProvider))")
    CityEntity mapCityDataResponse(Map<String, AttributeValue> item, @Context TimeZoneProvider timeZoneProvider);

    /**
     * Maps a data response from a map of attributes to a SectorEntity object. The mapping extracts
     * common entity attributes, geographical coordinates (latitude and longitude), and metadata attributes
     * from the provided map.
     *
     * @param item a map containing attribute values where keys are attribute names and values are
     *             AttributeValue objects. This map should contain necessary data for mapping to a SectorEntity.
     * @return a SectorEntity object populated with data from the provided map, including entity common attributes,
     *         latitude, longitude, and entity metadata.
     */
    @Mapping(target = "entityCommon",   expression = "java(mapCommonAttributes(item))")
    @Mapping(target = "latitude",       expression = "java(getAttributeValue(item, \"latitude\"))")
    @Mapping(target = "longitude",      expression = "java(getAttributeValue(item, \"longitude\"))")
    @Mapping(target = "entityMetadata", expression = "java(mapMetadataAttributes(item, timeZoneProvider))")
    SectorEntity mapSectorsDataResponse(Map<String, AttributeValue> item, @Context TimeZoneProvider timeZoneProvider);

    /**
     * Retrieves the string value of a specified attribute from a given map of attribute names to attribute values.
     *
     * @param item the map containing attribute names and their corresponding values; may be null
     * @param attributeName the name of the attribute whose value is to be retrieved; must not be null
     * @return the string value of the attribute if it exists in the map, otherwise null; returns null if the map is null
     */
    default String getAttributeValue(Map<String, AttributeValue> item, String attributeName) {
        if (item == null) {
            return null;
        }
        AttributeValue attr = item.get(attributeName);
        return attr != null ? attr.s() : null;
    }

    /**
     * Maps common attributes from a provided map of attribute values to an {@link EntityCommon} record.
     * This method extracts the 'name', 'description', and 'status' attributes from the map
     * and uses them to construct a new {@link EntityCommon} instance.
     *
     * @param item a map containing attribute names as keys and {@link AttributeValue} objects as values,
     *             representing the source from which common attributes are to be extracted
     * @return an instance of {@link EntityCommon} with values populated from the map, or null if the map is null
     */
    @Named("mapCommonAttributes")
    default EntityCommon mapCommonAttributes(Map<String, AttributeValue> item) {
        if (item == null) {
            return null;
        }
        return new EntityCommon(
            this.getAttributeValue(item, "name"),
            this.getAttributeValue(item, "description"),
            this.getEnumValue(item, "status", RecordStatus.class)
        );
    }

    /**
     * Maps a given map of attribute values to an EntityMetadata object, which includes
     * metadata attributes like creation and update timestamps.
     *
     * @param item a map containing attribute names as keys and their respective values
     *             encapsulated in AttributeValue objects; expected to include metadata
     *             attributes such as "createdAt" and "updatedAt".
     * @param timeZoneProvider the provider for the city's timezone.
     * @return an EntityMetadata object populated with the retrieved creation and update
     *         timestamps from the provided map, or null if the input map is null
     */
    @Named("mapMetadataAttributes")
    default EntityMetadata mapMetadataAttributes(final Map<String, AttributeValue> item,
                                                 final TimeZoneProvider timeZoneProvider) {
        if (item == null) {
            return null;
        }
        return new EntityMetadata(
            this.getDateTimeValue(item, "createdAt", timeZoneProvider),
            this.getDateTimeValue(item, "updatedAt", timeZoneProvider)
        );
    }

    /**
     * Retrieves the value of a specified attribute from the provided map and converts it to an Enum constant
     * of the specified enum class type.
     *
     * @param item          the map containing attribute names and their corresponding values
     * @param attributeName the name of the attribute whose value is to be retrieved and converted to an enum
     * @param enumClass     the class of the enumeration to which the attribute value will be converted
     * @param <T> the type of the enumeration
     * @return the enum constant corresponding to the attribute value, or null if the value is not present
     */
    default <T extends Enum<T>> T getEnumValue(final Map<String, AttributeValue> item,
                                               final String attributeName,
                                               final Class<T> enumClass) {
        String value = this.getAttributeValue(item, attributeName);
        return value != null ? Enum.valueOf(enumClass, value) : null;
    }

    /**
     * Retrieves the ZonedDateTime value from a map of attributes based on the specified attribute name.
     * This method expects the corresponding value to be in ISO 8601 format.
     *
     * @param item             the map of attributes containing the desired date-time value
     * @param attributeName    the name of the attribute within the map from which to extract the date-time value
     * @param timeZoneProvider the provider for the city's timezone
     * @return the parsed ZonedDateTime object from the specified attribute
     * @throws CityException if the date-time value is null or empty in the specified attribute
     */
    default ZonedDateTime getDateTimeValue(final Map<String, AttributeValue> item,
                                           final String attributeName,
                                           final TimeZoneProvider timeZoneProvider) {
        String dateTimeString = this.getAttributeValue(item, attributeName);
        if (Objects.isNull(dateTimeString) || dateTimeString.isEmpty()) {
            throw new CityException("Date time value cannot be null or empty in attribute: " + attributeName,
                ErrorCode.INTERNAL_002);
        }
        return DateTimeUtil.getZonedDateTimeUsingISO8601(dateTimeString, timeZoneProvider.getCityTimeZone());
    }
}
