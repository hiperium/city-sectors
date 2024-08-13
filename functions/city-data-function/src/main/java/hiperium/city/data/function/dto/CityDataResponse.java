package hiperium.city.data.function.dto;

/**
 * Represents a response object that contains information about a city.
 */
public record CityDataResponse(

    String cityId,
    String name,
    String timezone,
    Integer httpStatus,
    String errorMessage) {

    /**
     * Represents a builder for constructing CityDataResponse objects.
     */
    public static class Builder {
        private String cityId;
        private String name;
        private String timezone;
        private Integer httpStatus;
        private String errorMessage;

        /**
         * Sets the city ID for the CityDataResponse Builder.
         *
         * @param id The ID of the city to set.
         * @return The CityDataResponse Builder object.
         */
        public Builder cityId(String id) {
            this.cityId = id;
            return this;
        }

        /**
         * Sets the name of the city in the CityDataResponse object being built.
         *
         * @param name the name of the city
         * @return the Builder object to allow for method chaining
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the timezone for the CityDataResponse Builder.
         *
         * @param timezone the timezone to set
         * @return the CityDataResponse Builder object
         */
        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        /**
         * Sets the HTTP status for the CityDataResponse object being built.
         *
         * @param httpStatus The HTTP status code to set.
         * @return The Builder object.
         */
        public Builder httpStatus(Integer httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        /**
         * Sets the error message for the CityDataResponse Builder.
         *
         * @param errorMessage The error message to be set.
         * @return The CityDataResponse Builder.
         */
        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        /**
         * Builds a CityDataResponse object with the provided data.
         *
         * @return The constructed CityDataResponse object.
         */
        public CityDataResponse build() {
            return new CityDataResponse(cityId, name, timezone, httpStatus, errorMessage);
        }
    }
}
