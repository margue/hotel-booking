package service;

import persistence.ArrivalDate;
import persistence.DepartureDate;

// BookingRequestInterval ist sicher keine Domänensprache?
// Braucht es an dieser Stelle überhaupt so ein küsntliches Objekt?
// Reichen nicht ArrivalDate und DepartureDate
public record BookingRequestInterval(ArrivalDate startDate, DepartureDate endDate) {
}
