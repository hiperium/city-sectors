package hiperium.city.read.function.entities;

/**
 * Represents a response object that contains information about a city.
 */
public record City(

    String id,
    String name,
    String timezone,
    CityStatus status,
    String countryCode) {

    public static final String ID_COLUMN_NAME = "id";
    public static final String NAME_COLUMN_NAME = "name";
    public static final String STATUS_COLUMN_NAME = "status";
    public static final String TIMEZONE_COLUMN_NAME = "timezone";
    public static final String COUNTRY_CODE_COLUMN_NAME = "countryCode";
}
