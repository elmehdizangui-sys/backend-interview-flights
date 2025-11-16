package org.deblock.exercise.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.deblock.exercise.application.port.outbound.FlightSupplier
import org.deblock.exercise.application.service.FlightSearchUseCaseImpl
import org.deblock.exercise.domain.model.AirportCode
import org.deblock.exercise.domain.model.Flight
import org.deblock.exercise.domain.model.FlightSearchCriteria
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class FlightSearchUseCaseImplTest {

    private val supplier1 = mockk<FlightSupplier>()
    private val supplier2 = mockk<FlightSupplier>()
    private val service = FlightSearchUseCaseImpl(listOf(supplier1, supplier2))
    private val lhr = AirportCode.create("LHR")
    private val ams = AirportCode.create("AMS")
    @Test
    fun `should throw exception when number of passengers is less than 1`() {
        val request = FlightSearchCriteria(
            origin = AirportCode.create("LHR"),
            destination = AirportCode.create("AMS"),
            departureDate = LocalDate.now(),
            returnDate = LocalDate.now().plusDays(1),
            numberOfPassengers = 0
        )

        assertThrows<IllegalArgumentException> {
            runBlocking {
                service.searchFlights(request)
            }
        }
    }

    @Test
    fun `should throw exception when number of passengers is more than 4`() {
        val request = FlightSearchCriteria(
            origin = AirportCode.create("LHR"),
            destination = AirportCode.create("AMS"),
            departureDate = LocalDate.now(),
            returnDate = LocalDate.now().plusDays(1),
            numberOfPassengers = 5
        )

        assertThrows<IllegalArgumentException> {
            runBlocking {
                service.searchFlights(request)
            }
        }
    }

    @Test
    fun `should aggregate and sort flights from all suppliers`() {

        runBlocking {
            // Given
            val request = FlightSearchCriteria(
                origin = lhr,
                destination = ams,
                departureDate = LocalDate.now(),
                returnDate = LocalDate.now().plusDays(1),
                numberOfPassengers = 2
            )

            val flight1 = Flight(
                airline = "British Airways",
                supplier = "Supplier1",
                fare = BigDecimal("100.00"),
                departureAirportCode = lhr,
                destinationAirportCode = ams,
                departureDate = LocalDateTime.now(),
                arrivalDate = LocalDateTime.now().plusHours(2)
            )

            val flight2 = Flight(
                airline = "KLM",
                supplier = "Supplier2",
                fare = BigDecimal("80.00"),
                departureAirportCode = lhr,
                destinationAirportCode = ams,
                departureDate = LocalDateTime.now(),
                arrivalDate = LocalDateTime.now().plusHours(2)
            )

            coEvery { supplier1.supplierName } returns "Supplier1"
            coEvery { supplier2.supplierName } returns "Supplier2"
            coEvery { supplier1.searchFlights(request) } returns listOf(flight1)
            coEvery { supplier2.searchFlights(request) } returns listOf(flight2)

            // When
            val result = service.searchFlights(request)

            // Then
            assertEquals(2, result.size)
            assertEquals(BigDecimal("80.00"), result[0].fare)
            assertEquals(BigDecimal("100.00"), result[1].fare)
        }
    }

    @Test
    fun `should handle empty results from suppliers`() = runBlocking {
        // Given
        val request = FlightSearchCriteria(
            origin = lhr,
            destination = ams,
            departureDate = LocalDate.now(),
            returnDate = LocalDate.now().plusDays(1),
            numberOfPassengers = 2
        )

        coEvery { supplier1.supplierName } returns "Supplier1"
        coEvery { supplier2.supplierName } returns "Supplier2"
        coEvery { supplier1.searchFlights(request) } returns emptyList()
        coEvery { supplier2.searchFlights(request) } returns emptyList()

        // When
        val result = service.searchFlights(request)

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun `should handle exceptions from suppliers`() = runBlocking {
        // Given
        val request = FlightSearchCriteria(
            origin = lhr,
            destination = ams,
            departureDate = LocalDate.now(),
            returnDate = LocalDate.now().plusDays(1),
            numberOfPassengers = 2
        )

        val flight = Flight(
            airline = "British Airways",
            supplier = "Supplier2",
            fare = BigDecimal("100.00"),
            departureAirportCode = lhr,
            destinationAirportCode = ams,
            departureDate = LocalDateTime.now(),
            arrivalDate = LocalDateTime.now().plusHours(2)
        )

        coEvery { supplier1.supplierName } returns "Supplier1"
        coEvery { supplier2.supplierName } returns "Supplier2"
        coEvery { supplier1.searchFlights(request) } throws RuntimeException("API error")
        coEvery { supplier2.searchFlights(request) } returns listOf(flight)

        // When
        val result = service.searchFlights(request)

        // Then
        assertEquals(1, result.size)
        assertEquals("British Airways", result[0].airline)
    }

    @Test
    fun `should throw exception when origin is blank`() {
        assertThrows<IllegalArgumentException> {
            FlightSearchCriteria(
                origin = AirportCode.create(""),
                destination = ams,
                departureDate = LocalDate.now(),
                returnDate = LocalDate.now().plusDays(1),
                numberOfPassengers = 2
            )
        }
    }

    @Test
    fun `should throw exception when destination is blank`() {
        assertThrows<IllegalArgumentException> {
            FlightSearchCriteria(
                origin = lhr,
                destination = AirportCode.create(""),
                departureDate = LocalDate.now(),
                returnDate = LocalDate.now().plusDays(1),
                numberOfPassengers = 2
            )
        }
    }

    @Test
    fun `should throw exception when origin and destination are the same`() {
        assertThrows<IllegalArgumentException> {
            FlightSearchCriteria(
                origin = lhr,
                destination = lhr,
                departureDate = LocalDate.now(),
                returnDate = LocalDate.now().plusDays(1),
                numberOfPassengers = 2
            )
        }
    }
}
