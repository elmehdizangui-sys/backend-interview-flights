package org.deblock.exercise.adapter.web.mapper

import org.deblock.exercise.adapter.web.dto.FlightSearchRequestDto
import org.deblock.exercise.adapter.web.dto.FlightSearchResponseDto
import org.deblock.exercise.domain.model.AirportCode
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
            origin = AirportCode.create(dto.origin),
            destination = AirportCode.create(dto.destination),
            departureDate = dto.departureDate,
            returnDate = dto.returnDate,
            numberOfPassengers = dto.numberOfPassengers
        )
    }


    fun toFlightDto(flight: Flight): FlightSearchResponseDto {
        return FlightSearchResponseDto(
            airline = flight.airline,
            supplier = flight.supplier,
            fare = flight.fare,
            departureAirportCode = flight.departureAirportCode.code,
            destinationAirportCode = flight.destinationAirportCode.code,
            departureDate = flight.departureDate,
            arrivalDate = flight.arrivalDate
        )
    }
}
