package hiperium.city.data.function.entities;

import hiperium.city.data.function.dto.RecordStatus;

/**
 * Represents a response object that contains information about a city.
 */
public record City(

    String id,
    String name,
    String country,
    String timezone,
    RecordStatus status) {

    public static final String CITY_TABLE_NAME = "Cities";
    public static final String ID_COLUMN_NAME = "id";
    public static final String NAME_COLUMN_NAME = "name";
    public static final String COUNTRY_COLUMN_NAME = "name";
    public static final String STATUS_COLUMN_NAME = "status";
    public static final String TIMEZONE_COLUMN_NAME = "timezone";
}
