package service;

public record Pair<T, U>(T first, U second) {
    public boolean isError(){ return first != null; }
}
