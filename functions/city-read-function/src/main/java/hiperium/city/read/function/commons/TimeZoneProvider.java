package hiperium.city.read.function.commons;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Provides access to the time zone configuration for a city.
 */
@Component
public class TimeZoneProvider {

    @Value("${city.timezone}")
    private String cityTimeZone;

    /**
     * Retrieves the time zone associated with a city as specified in the application configuration.
     *
     * @return a string representing the city's time zone
     */
    public String getCityTimeZone() {
        return cityTimeZone;
    }
}
