package service;

public record Either<T, U>(T error, U result) {

    public boolean isError(){ return error != null; }

    public static <T, U> Either<T, U> ofError(T error) { return new Either<>(error, null); }
    public static <T, U> Either<T, U> ofResult(U result) { return new Either<>(null, result); }
}
