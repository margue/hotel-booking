package service;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record GuestName(String guestName) {
}
