package org.deblock.exercise.adapter.web.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.LocalDateTime

data class FlightDto(
    val airline: String,
    val supplier: String,
    val fare: BigDecimal,
    val departureAirportCode: String,
    val destinationAirportCode: String,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val departureDate: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val arrivalDate: LocalDateTime
)