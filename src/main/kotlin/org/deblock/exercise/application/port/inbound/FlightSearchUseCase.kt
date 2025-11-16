package org.deblock.exercise.application.port.inbound

import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchCriteria


interface FlightSearchUseCase {
    suspend fun searchFlights(searchCriteria: FlightSearchCriteria): List<Flight>
}