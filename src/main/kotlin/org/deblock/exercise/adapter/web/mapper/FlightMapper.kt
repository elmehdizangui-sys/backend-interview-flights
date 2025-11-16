package org.deblock.exercise.adapter.web.mapper

import org.deblock.exercise.adapter.web.dto.FlightDto
import org.deblock.exercise.adapter.web.dto.FlightSearchRequestDto
import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchCriteria
import org.springframework.stereotype.Component

/**
 * Mapper for converting between domain models and DTOs.
 */
@Component
class FlightMapper {

    fun toFlightSearchRequest(dto: FlightSearchRequestDto): FlightSearchCriteria {
        return FlightSearchCriteria(
            origin = dto.origin,
            destination = dto.destination,
            departureDate = dto.departureDate,
            returnDate = dto.returnDate,
            numberOfPassengers = dto.numberOfPassengers
        )
    }


    fun toFlightDto(flight: Flight): FlightDto {
        return FlightDto(
            airline = flight.airline,
            supplier = flight.supplier,
            fare = flight.fare,
            departureAirportCode = flight.departureAirportCode,
            destinationAirportCode = flight.destinationAirportCode,
            departureDate = flight.departureDate,
            arrivalDate = flight.arrivalDate
        )
    }
}