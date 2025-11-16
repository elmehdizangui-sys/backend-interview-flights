package org.deblock.exercise.domain.model

import java.time.LocalDate

data class FlightSearchCriteria(
    // TODO introduce AirportCode with validation (“is this a valid IATA code?)
    val origin: String,
    val destination: String,
    val departureDate: LocalDate,
    val returnDate: LocalDate?,
    val numberOfPassengers: Int
)