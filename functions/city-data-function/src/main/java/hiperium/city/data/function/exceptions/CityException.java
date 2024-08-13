package hiperium.city.data.function.exceptions;

/**
 * The CityException class is an exception thrown when there is an error related to a city.
 */
public sealed class CityException extends RuntimeException
    permits DisabledCityException, ResourceNotFoundException {

    /**
     * The CityException class is an exception thrown when there is an error related to a city.
     */
    public CityException(String message) {
        super(message);
    }
}
