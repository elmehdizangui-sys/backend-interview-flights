package org.deblock.exercise.application.port.outbound

import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchCriteria

interface FlightSupplier {

    val supplierName: String

    suspend fun searchFlights(request: FlightSearchCriteria): List<Flight>
}