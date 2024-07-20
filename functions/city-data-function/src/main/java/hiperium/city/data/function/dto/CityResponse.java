package hiperium.city.data.function.dto;

/**
 * Represents a response object that contains information about a city.
 */
public record CityResponse(

    String id,
    String name,
    String timezone,
    Integer httpStatus,
    String errorMessage) {

    public static class Builder {
        private String id;
        private String name;
        private String timezone;
        private Integer httpStatus;
        private String errorMessage;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }

        public Builder httpStatus(Integer httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public CityResponse build() {
            return new CityResponse(id, name, timezone, httpStatus, errorMessage);
        }
    }
}
