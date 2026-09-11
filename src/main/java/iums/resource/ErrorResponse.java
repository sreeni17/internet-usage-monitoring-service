package iums.resource;

public record ErrorResponse(boolean ok, ErrorBody error) {

    public record ErrorBody(String message) {
    }

    public static ErrorResponse of(String message) {
        return new ErrorResponse(false, new ErrorBody(message));
    }
}
