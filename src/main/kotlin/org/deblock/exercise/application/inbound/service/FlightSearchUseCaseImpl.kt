package org.deblock.exercise.application.inbound.service

import org.deblock.exercise.application.inbound.port.FlightSearchUseCase
import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchCriteria
import org.deblock.exercise.domain.port.FlightSupplier
import org.springframework.stereotype.Service


@Service
class FlightSearchUseCaseImpl(private val suppliers: List<FlightSupplier>) : FlightSearchUseCase {
    override suspend fun searchFlights(request: FlightSearchCriteria): List<Flight>  = emptyList()
}
