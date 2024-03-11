package service;

import java.time.LocalDate;

public record BookingRequestInterval(LocalDate startDate, LocalDate endDate) {
}