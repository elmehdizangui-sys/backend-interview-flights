package org.deblock.exercise.adapter.supplier.toughjet

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
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.ZoneId


@Component
class ToughJetFlightSupplierAdapter(
    private val webClientBuilder: WebClient.Builder,
    @Value("\${supplier.toughjet.url}") private val toughJetUrl: String,
    @Value("\${supplier.toughjet.endpoint.flights}") private val flightsEndpoint: String
) : FlightSupplier {
    private val logger = LoggerFactory.getLogger(ToughJetFlightSupplierAdapter::class.java)
    private val webClient by lazy { webClientBuilder.baseUrl(toughJetUrl).build() }

    override val supplierName: String = "ToughJet"

    override suspend fun searchFlights(request: FlightSearchCriteria): List<Flight> {
        logger.info("Searching flights from ToughJet: origin={}, destination={}, departureDate={}, passengers={}",
            request.origin, request.destination, request.departureDate, request.numberOfPassengers)

        val toughJetRequest = createToughJetRequest(request)

        return try {
            val responses = fetchFlightsFromToughJet(toughJetRequest)
            mapResponsesToFlights(responses)
        } catch (e: WebClientResponseException) {
            logger.error("HTTP error from ToughJet API: status={}, message={}", e.statusCode, e.message)
            emptyList()
        } catch (e: Exception) {
            logger.error("Error searching flights from ToughJet: {}", e.message, e)
            emptyList()
        }
    }

    private fun createToughJetRequest(request: FlightSearchCriteria): ToughJetRequest =
        ToughJetRequest(
            from = request.origin.code,
            to = request.destination.code,
            outboundDate = request.departureDate,
            inboundDate = request.returnDate,
            numberOfAdults = request.numberOfPassengers
        )


    private suspend fun fetchFlightsFromToughJet(request: ToughJetRequest): List<ToughJetResponse> =
        webClient.post()
            .uri(flightsEndpoint)
            .bodyValue(request)
            .retrieve()
            .awaitBody()

    private fun mapResponsesToFlights(responses: List<ToughJetResponse>): List<Flight> =
        responses.map { response ->
            // Calculate the final price: basePrice + tax - discount
            val discountAmount =  response.basePrice.multiply(response.discount.divide(BigDecimal(100)))
            val totalPrice =    response.basePrice.add( response.tax).subtract(discountAmount)


            Flight(
                airline = response.carrier,
                supplier = supplierName,
                fare = totalPrice.setScale(2, RoundingMode.HALF_UP),
                departureAirportCode = AirportCode.create(response.departureAirportName),
                destinationAirportCode = AirportCode.create(response.arrivalAirportName),
                departureDate = LocalDateTime.ofInstant(response.outboundDateTime, ZoneId.of("UTC")),
                arrivalDate = LocalDateTime.ofInstant(response.inboundDateTime, ZoneId.of("UTC"))
            )
        }

}
