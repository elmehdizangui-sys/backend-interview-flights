package org.deblock.exercise.adapter.web

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.deblock.exercise.adapter.web.controller.FlightController
import org.deblock.exercise.adapter.web.dto.FlightSearchRequestDto
import org.deblock.exercise.adapter.web.dto.FlightSearchResponseDto
import org.deblock.exercise.adapter.web.mapper.FlightMapper
import org.deblock.exercise.application.port.inbound.FlightSearchUseCase
import org.deblock.exercise.domain.model.AirportCode
import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchCriteria
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class FlightControllerTest {

    private val flightSearchUseCase = mockk<FlightSearchUseCase>()
    private val flightMapper = mockk<FlightMapper>()
    private val controller = FlightController(flightSearchUseCase, flightMapper)
    private val lhr = "LHR"
    private val ams = "AMS"

    @Test
    fun `should return flights when search is successful`() {
        val britishAirways = "British Airways"
        val crazyAir = "CrazyAir"
        runBlocking {
            // Given
            val requestDto = FlightSearchRequestDto(
                origin = lhr,
                destination = ams,
                departureDate = LocalDate.now(),
                returnDate = LocalDate.now().plusDays(1),
                numberOfPassengers = 2
            )

            val request = FlightSearchCriteria(
                origin = AirportCode.create(lhr),
                destination = AirportCode.create(ams),
                departureDate = LocalDate.now(),
                returnDate = LocalDate.now().plusDays(1),
                numberOfPassengers = 2
            )

            val flight = Flight(
                airline = britishAirways,
                supplier = crazyAir,
                fare = BigDecimal("100.00"),
                departureAirportCode = AirportCode.create(lhr),
                destinationAirportCode =AirportCode.create( ams),
                departureDate = LocalDateTime.now(),
                arrivalDate = LocalDateTime.now().plusHours(2)
            )

            val flightSearchResponseDto = FlightSearchResponseDto(
                airline = britishAirways,
                supplier = crazyAir,
                fare = BigDecimal("100.00"),
                departureAirportCode = lhr,
                destinationAirportCode = ams,
                departureDate = LocalDateTime.now(),
                arrivalDate = LocalDateTime.now().plusHours(2)
            )

            coEvery { flightMapper.toFlightSearchRequest(requestDto) } returns request
            coEvery { flightSearchUseCase.searchFlights(request) } returns listOf(flight)
            coEvery { flightMapper.toFlightDto(flight) } returns flightSearchResponseDto

            // When
            val response = controller.searchFlights(requestDto)

            // Then
            assertEquals(HttpStatus.OK, response.statusCode)
            assertEquals(1, response.body?.size)
            assertEquals(britishAirways, response.body?.get(0)?.airline)
            assertEquals(crazyAir, response.body?.get(0)?.supplier)
        }
    }

    @Test
    fun `should return empty list when no flights found`() {
        runBlocking {
            // Given
            val requestDto = FlightSearchRequestDto(
                origin = lhr,
                destination = ams,
                departureDate = LocalDate.now(),
                returnDate = LocalDate.now().plusDays(1),
                numberOfPassengers = 2
            )

            val request = FlightSearchCriteria(
                origin = AirportCode.create(lhr),
                destination = AirportCode.create(ams),
                departureDate = LocalDate.now(),
                returnDate = LocalDate.now().plusDays(1),
                numberOfPassengers = 2
            )

            coEvery { flightMapper.toFlightSearchRequest(requestDto) } returns request
            coEvery { flightSearchUseCase.searchFlights(request) } returns emptyList()

            // When
            val response = controller.searchFlights(requestDto)

            // Then
            assertEquals(HttpStatus.OK, response.statusCode)
            assertEquals(0, response.body?.size)
        }
    }

    @Test
    fun `should throw exception when search criteria is invalid`() {
        runBlocking {
            // Given
            val requestDto = FlightSearchRequestDto(
                origin = lhr,
                destination = lhr, // Same origin and destination
                departureDate = LocalDate.now(),
                returnDate = LocalDate.now().plusDays(1),
                numberOfPassengers = 2
            )

            val errorMessage = "Origin and destination cannot be the same"

            // Mock that the mapper will throw the exception when creating the FlightSearchCriteria
            coEvery { flightMapper.toFlightSearchRequest(requestDto) } throws IllegalArgumentException(errorMessage)

            // When & Then
            val exception = assertThrows(IllegalArgumentException::class.java) {
                runBlocking {
                    controller.searchFlights(requestDto)
                }
            }

            assertEquals(errorMessage, exception.message)
        }
    }
}
