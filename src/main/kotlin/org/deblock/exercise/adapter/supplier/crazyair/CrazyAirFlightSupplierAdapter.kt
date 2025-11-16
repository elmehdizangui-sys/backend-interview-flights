package org.deblock.exercise.adapter.supplier.crazyair

import org.deblock.exercise.application.port.outbound.FlightSupplier
import org.deblock.exercise.domain.model.AirportCode
import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchCriteria
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody
import java.math.RoundingMode

/**
 * Adapter for the CrazyAir flight supplier API.
 * Implements the FlightSupplier interface to provide flight search functionality.
 */
@Component
class CrazyAirFlightSupplierAdapter(
    private val webClientBuilder: WebClient.Builder,
    @Value("\${supplier.crazyair.url}") private val crazyAirUrl: String,
    @Value("\${supplier.crazyair.endpoint.flights}") private val flightsEndpoint: String
) : FlightSupplier {
    private val logger = LoggerFactory.getLogger(CrazyAirFlightSupplierAdapter::class.java)
    private val webClient by lazy { webClientBuilder.baseUrl(crazyAirUrl).build() }

    override val supplierName: String = "CrazyAir"

    /**
     * @return A list of flights matching the search criteria, or empty list if no flights found or an error occurs
     */
    override suspend fun searchFlights(request: FlightSearchCriteria): List<Flight> {
        logger.info("Searching flights from CrazyAir: origin={}, destination={}, departureDate={}, passengers={}",
            request.origin, request.destination, request.departureDate, request.numberOfPassengers)

        val crazyAirRequest = createCrazyAirRequest(request)

        return try {
            val responses = fetchFlightsFromCrazyAir(crazyAirRequest)
            mapResponsesToFlights(responses)
        } catch (e: WebClientResponseException) {
            logger.error("HTTP error from CrazyAir API: status={}, message={}", e.statusCode, e.message)
            emptyList()
        } catch (e: Exception) {
            logger.error("Error searching flights from CrazyAir: {}", e.message, e)
            emptyList()
        }
    }


    private fun createCrazyAirRequest(request: FlightSearchCriteria): CrazyAirRequest = 
        CrazyAirRequest(
            origin = request.origin.code,
            destination = request.destination.code,
            departureDate = request.departureDate,
            returnDate = request.returnDate,
            passengerCount = request.numberOfPassengers
        )

    private suspend fun fetchFlightsFromCrazyAir(request: CrazyAirRequest): List<CrazyAirResponse> =
        webClient.post()
            .uri(flightsEndpoint)
            .bodyValue(request)
            .retrieve()
            .awaitBody()

    private fun mapResponsesToFlights(responses: List<CrazyAirResponse>): List<Flight> =
        responses.map { response ->
            Flight(
                airline = response.airline,
                supplier = supplierName,
                fare = response.price.setScale(2, RoundingMode.HALF_UP),
                departureAirportCode = AirportCode.create(response.departureAirportCode),
                destinationAirportCode = AirportCode.create(response.destinationAirportCode),
                departureDate = response.departureDate,
                arrivalDate = response.arrivalDate
            )
        }
}
