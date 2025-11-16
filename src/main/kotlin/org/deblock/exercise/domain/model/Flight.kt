package org.deblock.exercise.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Flight(
    val airline: String,
    val supplier: String,
    val fare: BigDecimal,
    val departureAirportCode: String,
    val destinationAirportCode: String,
    val departureDate: LocalDateTime,
    val arrivalDate: LocalDateTime
)