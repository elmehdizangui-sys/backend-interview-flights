package org.deblock.exercise.adapter.web.dto

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.time.LocalDate


data class FlightSearchRequestDto(
    @field:NotBlank(message = "Origin is required")
    @field:Pattern(regexp = "[A-Z]{3}", message = "Origin must be a 3-letter IATA code")
    val origin: String,
    
    @field:NotBlank(message = "Destination is required")
    @field:Pattern(regexp = "[A-Z]{3}", message = "Destination must be a 3-letter IATA code")
    val destination: String,
    
    @field:NotNull(message = "Departure date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val departureDate: LocalDate,
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    val returnDate: LocalDate?,
    
    @field:NotNull(message = "Number of passengers is required")
    @field:Min(value = 1, message = "Number of passengers must be at least 1")
    @field:Max(value = 4, message = "Number of passengers must be at most 4")
    val numberOfPassengers: Int
)