package hiperium.city.data.function.exceptions;

/**
 * The ResourceNotFoundException class is an exception thrown when a resource is not found.
 */
public final class ResourceNotFoundException extends CityException {

    /**
     * Exception thrown when a resource is not found.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
