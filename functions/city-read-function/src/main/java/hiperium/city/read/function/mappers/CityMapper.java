package hiperium.city.read.function.mappers;

import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.city.read.function.dto.CityDataResponse;
import hiperium.city.read.function.entities.City;
import hiperium.city.read.function.entities.CityStatus;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

/**
 * CityMapper is an interface used for mapping attribute values of a City object to a Map object and vice versa.
 * It provides methods for converting City objects to Map objects and Map objects to City objects.
 * This interface uses annotations from the MapStruct library for mapping.
 */
@Mapper(componentModel = "spring")
public interface CityMapper {

    HiperiumLogger LOGGER = new HiperiumLogger(CityMapper.class);

    /**
     * Converts a map of attribute values to a City object.
     *
     * @param itemAttributesMap A map containing attribute values of a City object.
     *                          The keys represent the column names of the City table,
     *                          and the values represent the corresponding attribute values.
     * @return A City object with the attribute values mapped from the itemAttributesMap.
     */
    @Mapping(target = "id",       expression = "java(getStringValueFromAttributesMap(itemAttributesMap, City.ID_COLUMN_NAME))")
    @Mapping(target = "name",     expression = "java(getStringValueFromAttributesMap(itemAttributesMap, City.NAME_COLUMN_NAME))")
    @Mapping(target = "status",   expression = "java(getStatusEnumFromAttributesMap(itemAttributesMap))")
    @Mapping(target = "country",  expression = "java(getStringValueFromAttributesMap(itemAttributesMap, City.COUNTRY_COLUMN_NAME))")
    @Mapping(target = "timezone", expression = "java(getStringValueFromAttributesMap(itemAttributesMap, City.TIMEZONE_COLUMN_NAME))")
    City mapToCity(Map<String, AttributeValue> itemAttributesMap);

    /**
     * Converts a City object, HTTP status code, and error message to a CityDataResponse object.
     *
     * @param city The City object to convert.
     * @return A CityDataResponse object with the converted data.
     */
    @Mapping(target = "error", ignore = true)
    CityDataResponse mapToCityResponse(City city);

    /**
     * Retrieves the string value associated with the given key from the attribute map.
     * If the key is present in the map, the corresponding value is returned as a string.
     * If the key is not present, null is returned.
     *
     * @param attributesMap The map containing attribute values.
     * @param key The key used to retrieve the value from the map.
     * @return The string value associated with the given key, or null if the key is not present in the map.
     */
    default String getStringValueFromAttributesMap(Map<String, AttributeValue> attributesMap, String key) {
        return attributesMap.containsKey(key) ? attributesMap.get(key).s() : null;
    }

    /**
     * Retrieves the CityStatus enum value from the attribute map based on the specified column name.
     *
     * @param itemAttributesMap A map containing attribute values of a City object. The keys represent the column names of the City table, and the values represent the corresponding
     *  attribute values.
     * @return The CityStatus enum value retrieved from the attributes map.
     */
    default CityStatus getStatusEnumFromAttributesMap(Map<String, AttributeValue> itemAttributesMap) {
        return CityStatus.valueOf(this.getStringValueFromAttributesMap(itemAttributesMap, City.STATUS_COLUMN_NAME));
    }

    /**
     * This method is an implementation of the `@AfterMapping` annotation and is used to perform additional operations after mapping a City object from a map of attribute values.
     *
     * @param city The City object that has been mapped.
     * @param itemAttributesMap A map containing the attribute values of the City object. The keys represent the column names of the City table, and the values represent the corresponding
     *  attribute values.
     */
    @AfterMapping
    default void afterMapToCity(@MappingTarget City city, Map<String, AttributeValue> itemAttributesMap) {
        LOGGER.debug("Mapped city", city);
    }

    /**
     * Performs additional operations after mapping a City object to a CityDataResponse object.
     *
     * @param response The CityDataResponse object after mapping.
     * @param city The City object before mapping.
     */
    @AfterMapping
    default void afterMapToResponse(@MappingTarget CityDataResponse response, City city) {
        LOGGER.debug("Mapped response", response);
    }
}
