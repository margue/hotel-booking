package service;

public record Either<T, U>(T error, U result) {
    public boolean isError(){ return error != null; }
}
