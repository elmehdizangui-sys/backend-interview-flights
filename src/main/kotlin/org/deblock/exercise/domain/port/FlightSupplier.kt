package org.deblock.exercise.domain.port

import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchCriteria

interface FlightSupplier {
    /**
     * Name of the supplier.
     */
    val name: String

    /**
     * Search for flights based on the given request.
     *
     * @param request The flight search request.
     * @return A list of flights matching the search criteria.
     */
    suspend fun searchFlights(request: FlightSearchCriteria): List<Flight>
}