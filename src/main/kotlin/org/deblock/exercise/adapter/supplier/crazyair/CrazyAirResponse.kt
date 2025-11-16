package org.deblock.exercise.adapter.supplier.crazyair

import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.LocalDateTime

data class CrazyAirResponse(
    val airline: String,
    val price: BigDecimal,
    val cabinclass: String,
    val departureAirportCode: String,
    val destinationAirportCode: String,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val departureDate: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val arrivalDate: LocalDateTime
)