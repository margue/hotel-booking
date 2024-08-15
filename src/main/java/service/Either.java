package service;

public class Either<T, U> {
    private final T error;
    private final U result;
    private Either(T error, U result) {
        this.error = error;
        this.result = result;
    }
    public T error() { return error; }
    public U result() { return result; }

    public static <T, U> Either<T, U> ofError(T error) { return new Either<>(error, null); }
    public static <T, U> Either<T, U> ofResult(U result) { return new Either<>(null, result); }
    public boolean isError(){ return error != null; }
}
