package org.deblock.exercise.application.inbound.service

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.deblock.exercise.application.inbound.port.FlightSearchUseCase
import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchCriteria
import org.deblock.exercise.domain.port.FlightSupplier
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class FlightSearchUseCaseImpl(private val suppliers: List<FlightSupplier>) : FlightSearchUseCase {
    private val logger = LoggerFactory.getLogger(FlightSearchUseCaseImpl::class.java)

    companion object {
        // Could be externalized as a configuration
        private const val MIN_PASSENGERS = 1
        private const val MAX_PASSENGERS = 4
    }

    /**
       Validate input, fetch flights, then apply post-fetch processing (here: sort by fare)
     */
    override suspend fun searchFlights(searchCriteria: FlightSearchCriteria): List<Flight> = coroutineScope {
        validateSearchCriteria(searchCriteria)

        val flightResults = fetchFlightsFromSuppliers(searchCriteria)

        sortFlightsByFare(flightResults)
    }

    /**
     * @throws IllegalArgumentException if the criteria is invalid. --> Could be replaced with a custom exception.
     */
    private fun validateSearchCriteria(criteria: FlightSearchCriteria) {
        if (criteria.numberOfPassengers < MIN_PASSENGERS || criteria.numberOfPassengers > MAX_PASSENGERS) {
            throw IllegalArgumentException("Number of passengers must be between $MIN_PASSENGERS and $MAX_PASSENGERS")
        }

        if (criteria.origin.isBlank()) {
            throw IllegalArgumentException("Origin airport code cannot be empty")
        }

        if (criteria.destination.isBlank()) {
            throw IllegalArgumentException("Destination airport code cannot be empty")
        }

        if (criteria.origin == criteria.destination) {
            throw IllegalArgumentException("Origin and destination cannot be the same")
        }
    }


    private suspend fun fetchFlightsFromSuppliers(criteria: FlightSearchCriteria): List<Flight> = coroutineScope {
        val deferredResults = suppliers.map { supplier ->
            async {
                try {
                    supplier.searchFlights(criteria)
                } catch (e: Exception) {
                    logger.error("Error searching flights from ${supplier.name}: ${e.message}", e)
                    emptyList()
                }
            }
        }

        deferredResults.awaitAll().flatten()
    }

    private fun sortFlightsByFare(flights: List<Flight>): List<Flight> {
        return flights.sortedBy { it.fare }
    }
}
