package hiperium.city.read.function.mappers;

import hiperium.cities.commons.enums.RecordStatus;
import hiperium.cities.commons.exceptions.CityException;
import hiperium.cities.commons.loggers.HiperiumLogger;
import hiperium.cities.commons.utils.DateTimeUtils;
import hiperium.city.read.function.commons.CommonAttributes;
import hiperium.city.read.function.commons.MetadataAttributes;
import hiperium.city.read.function.entities.CityEntity;
import hiperium.city.read.function.entities.SectorEntity;
import hiperium.city.read.function.utils.FunctionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface CityMapper {

    @Mapping(target = "commonAttributes",   expression = "java(mapCommonAttributes(item))")
    @Mapping(target = "timezone",           expression = "java(getAttributeValue(item, \"timezone\"))")
    @Mapping(target = "languageCode",       expression = "java(getAttributeValue(item, \"languageCode\"))")
    @Mapping(target = "countryCode",        expression = "java(getAttributeValue(item, \"countryCode\"))")
    @Mapping(target = "metadataAttributes", expression = "java(mapMetadataAttributes(item))")
    CityEntity mapCityDataResponse(Map<String, AttributeValue> item);

    @Mapping(target = "commonAttributes",   expression = "java(mapCommonAttributes(item))")
    @Mapping(target = "latitude",           expression = "java(getAttributeValue(item, \"latitude\"))")
    @Mapping(target = "longitude",          expression = "java(getAttributeValue(item, \"longitude\"))")
    @Mapping(target = "metadataAttributes", expression = "java(mapMetadataAttributes(item))")
    SectorEntity mapSectorsDataResponse(Map<String, AttributeValue> item);

    default String getAttributeValue(Map<String, AttributeValue> item, String attributeName) {
        if (item == null) {
            return null;
        }
        AttributeValue attr = item.get(attributeName);
        return attr != null ? attr.s() : null;
    }

    @Named("mapCommonAttributes")
    default CommonAttributes mapCommonAttributes(Map<String, AttributeValue> item) {
        if (item == null) {
            return null;
        }
        return new CommonAttributes(
            this.getAttributeValue(item, "name"),
            this.getAttributeValue(item, "description"),
            this.getEnumValue(item, "status", RecordStatus.class)
        );
    }

    @Named("mapMetadataAttributes")
    default MetadataAttributes mapMetadataAttributes(Map<String, AttributeValue> item) {
        if (item == null) {
            return null;
        }
        return new MetadataAttributes(
            this.getDateTimeValue(item, "createdAt"),
            this.getDateTimeValue(item, "updatedAt")
        );
    }

    default <T extends Enum<T>> T getEnumValue(Map<String, AttributeValue> item, String attributeName, Class<T> enumClass) {
        String value = this.getAttributeValue(item, attributeName);
        return value != null ? Enum.valueOf(enumClass, value) : null;
    }

    default ZonedDateTime getDateTimeValue(Map<String, AttributeValue> item, String attributeName) {
        String dateTimeString = this.getAttributeValue(item, attributeName);
        if (Objects.isNull(dateTimeString) || dateTimeString.isEmpty()) {
            throw new CityException("Date time value cannot be null or empty in attribute: " + attributeName);
        }
        return DateTimeUtils.getZonedDateTimeUsingISO8601(dateTimeString);
    }
}
