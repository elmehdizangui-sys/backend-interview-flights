package org.deblock.exercise.domain.model

import java.time.LocalDate

data class FlightSearchCriteria(
    val origin: AirportCode,
    val destination: AirportCode,
    val departureDate: LocalDate,
    val returnDate: LocalDate?,
    val numberOfPassengers: Int
) {
    init {
        if (origin.code == destination.code) {
            throw IllegalArgumentException("Origin and destination cannot be the same")
        }
    }
}
