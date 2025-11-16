package org.deblock.exercise.adapter.supplier.crazyair

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate


data class CrazyAirRequest(
    val origin: String,
    val destination: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val departureDate: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val returnDate: LocalDate?,
    val passengerCount: Int
)