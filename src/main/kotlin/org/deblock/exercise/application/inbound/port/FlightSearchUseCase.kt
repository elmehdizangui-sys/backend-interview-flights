package org.deblock.exercise.application.inbound.port

import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchCriteria


interface FlightSearchUseCase {
    /**
     * Search for flights from all suppliers based on the given request.
     *
     * @param request The flight search request.
     * @return A list of flights matching the search criteria, ordered by fare.
     */
    suspend fun searchFlights(searchCriteria: FlightSearchCriteria): List<Flight>
}