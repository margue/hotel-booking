package service;

import org.jmolecules.ddd.annotation.ValueObject;

import java.time.LocalDate;

// BookingRequestInterval ist sicher keine Domänensprache?
// Braucht es an dieser Stelle überhaupt so ein küsntliches Objekt?
// Reichen nicht ArrivalDate und DepartureDate
@ValueObject
public record BookingRequestInterval(LocalDate startDate, LocalDate endDate) {
}
