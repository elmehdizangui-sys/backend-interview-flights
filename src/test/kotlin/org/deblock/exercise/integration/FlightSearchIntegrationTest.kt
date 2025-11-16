package org.deblock.exercise.integration

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.reset
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import org.deblock.exercise.adapter.web.dto.FlightSearchRequestDto
import org.deblock.exercise.adapter.web.dto.FlightSearchResponseDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.time.LocalDate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest(httpPort = 8090)
@TestPropertySource(properties = [
    "supplier.crazyair.url=http://localhost:8090/crazyair",
    "supplier.toughjet.url=http://localhost:8090/toughjet"
])
@ActiveProfiles("test")
class FlightSearchIntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @BeforeEach
    fun setup() {
        // Reset WireMock stubs before each test
        reset()

        // Setup CrazyAir mock with successful response
        stubFor(post(urlEqualTo("/crazyair/flights"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    [
                        {
                            "airline": "British Airways",
                            "price": 100.00,
                            "cabinclass": "E",
                            "departureAirportCode": "LHR",
                            "destinationAirportCode": "AMS",
                            "departureDate": "2023-01-01T10:00:00",
                            "arrivalDate": "2023-01-01T12:00:00"
                        }
                    ]
                """.trimIndent())
            )
        )

        // Setup ToughJet mock with successful response
        stubFor(post(urlEqualTo("/toughjet/flights"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    [
                        {
                            "carrier": "KLM",
                            "basePrice": 90.00,
                            "tax": 10.00,
                            "discount": 5.00,
                            "departureAirportName": "LHR",
                            "arrivalAirportName": "AMS",
                            "outboundDateTime": "2023-01-01T11:00:00Z",
                            "inboundDateTime": "2023-01-01T13:00:00Z"
                        }
                    ]
                """.trimIndent())
            )
        )
    }

    @Test
    fun `should return aggregated flights from all suppliers`() {
        // Given
        val requestDto = FlightSearchRequestDto(
            origin = "LHR",
            destination = "AMS",
            departureDate = LocalDate.of(2023, 1, 1),
            returnDate = LocalDate.of(2023, 1, 2),
            numberOfPassengers = 2
        )

        val url = "http://localhost:$port/flights/search"
        val request = HttpEntity(requestDto)

        // When
        val response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            object : ParameterizedTypeReference<List<FlightSearchResponseDto>>() {}
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val flights = response.body
        assertEquals(2, flights?.size)

        // Verify flights are ordered by fare
        val fares = flights?.map { it.fare }
        assertTrue(fares?.get(0)!! <= fares[1])

        // Verify both suppliers are represented
        val suppliers = flights.map { it.supplier }.toSet()
        assertEquals(2, suppliers.size)
        assertTrue(suppliers.contains("CrazyAir"))
        assertTrue(suppliers.contains("ToughJet"))

        // Verify WireMock was called
        verify(postRequestedFor(urlEqualTo("/crazyair/flights")))
        verify(postRequestedFor(urlEqualTo("/toughjet/flights")))
    }

    @Test
    fun `should return flights from ToughJet when CrazyAir throws exception`() {
        // Reset and setup mocks
        reset()

        // Setup CrazyAir mock to return an error
        stubFor(post(urlEqualTo("/crazyair/flights"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("""{"error": "Internal Server Error"}""")
            )
        )

        // Setup ToughJet mock with successful response
        stubFor(post(urlEqualTo("/toughjet/flights"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    [
                        {
                            "carrier": "KLM",
                            "basePrice": 90.00,
                            "tax": 10.00,
                            "discount": 5.00,
                            "departureAirportName": "LHR",
                            "arrivalAirportName": "AMS",
                            "outboundDateTime": "2023-01-01T11:00:00Z",
                            "inboundDateTime": "2023-01-01T13:00:00Z"
                        }
                    ]
                """.trimIndent())
            )
        )

        // Given
        val requestDto = FlightSearchRequestDto(
            origin = "LHR",
            destination = "AMS",
            departureDate = LocalDate.of(2023, 1, 1),
            returnDate = LocalDate.of(2023, 1, 2),
            numberOfPassengers = 2
        )

        val url = "http://localhost:$port/flights/search"
        val request = HttpEntity(requestDto)

        // When
        val response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            object : ParameterizedTypeReference<List<FlightSearchResponseDto>>() {}
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val flights = response.body

        // Should only have flights from ToughJet
        assertEquals(1, flights?.size)
        assertEquals("ToughJet", flights?.get(0)?.supplier)

        // Verify both suppliers were called
        verify(postRequestedFor(urlEqualTo("/crazyair/flights")))
        verify(postRequestedFor(urlEqualTo("/toughjet/flights")))
    }

    @Test
    fun `should return flights from CrazyAir when ToughJet throws exception`() {
        // Reset and setup mocks
        reset()

        // Setup CrazyAir mock with successful response
        stubFor(post(urlEqualTo("/crazyair/flights"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    [
                        {
                            "airline": "British Airways",
                            "price": 100.00,
                            "cabinclass": "E",
                            "departureAirportCode": "LHR",
                            "destinationAirportCode": "AMS",
                            "departureDate": "2023-01-01T10:00:00",
                            "arrivalDate": "2023-01-01T12:00:00"
                        }
                    ]
                """.trimIndent())
            )
        )

        // Setup ToughJet mock to return an error
        stubFor(post(urlEqualTo("/toughjet/flights"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("""{"error": "Internal Server Error"}""")
            )
        )

        // Given
        val requestDto = FlightSearchRequestDto(
            origin = "LHR",
            destination = "AMS",
            departureDate = LocalDate.of(2023, 1, 1),
            returnDate = LocalDate.of(2023, 1, 2),
            numberOfPassengers = 2
        )

        val url = "http://localhost:$port/flights/search"
        val request = HttpEntity(requestDto)

        // When
        val response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            object : ParameterizedTypeReference<List<FlightSearchResponseDto>>() {}
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val flights = response.body

        // Should only have flights from CrazyAir
        assertEquals(1, flights?.size)
        assertEquals("CrazyAir", flights?.get(0)?.supplier)

        // Verify both suppliers were called
        verify(postRequestedFor(urlEqualTo("/crazyair/flights")))
        verify(postRequestedFor(urlEqualTo("/toughjet/flights")))
    }

    @Test
    fun `should return empty list when both suppliers throw exceptions`() {
        // Reset and setup mocks
        reset()

        // Setup CrazyAir mock to return an error
        stubFor(post(urlEqualTo("/crazyair/flights"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("""{"error": "Internal Server Error"}""")
            )
        )

        // Setup ToughJet mock to return an error
        stubFor(post(urlEqualTo("/toughjet/flights"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("""{"error": "Internal Server Error"}""")
            )
        )

        // Given
        val requestDto = FlightSearchRequestDto(
            origin = "LHR",
            destination = "AMS",
            departureDate = LocalDate.of(2023, 1, 1),
            returnDate = LocalDate.of(2023, 1, 2),
            numberOfPassengers = 2
        )

        val url = "http://localhost:$port/flights/search"
        val request = HttpEntity(requestDto)

        // When
        val response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            object : ParameterizedTypeReference<List<FlightSearchResponseDto>>() {}
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val flights = response.body

        // Should have no flights
        assertTrue(flights?.isEmpty() ?: false)

        // Verify both suppliers were called
        verify(postRequestedFor(urlEqualTo("/crazyair/flights")))
        verify(postRequestedFor(urlEqualTo("/toughjet/flights")))
    }

    @Test
    fun `should return flights from CrazyAir when ToughJet returns malformed response`() {
        // Reset and setup mocks
        reset()

        // Setup CrazyAir mock with successful response
        stubFor(post(urlEqualTo("/crazyair/flights"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    [
                        {
                            "airline": "British Airways",
                            "price": 100.00,
                            "cabinclass": "E",
                            "departureAirportCode": "LHR",
                            "destinationAirportCode": "AMS",
                            "departureDate": "2023-01-01T10:00:00",
                            "arrivalDate": "2023-01-01T12:00:00"
                        }
                    ]
                """.trimIndent())
            )
        )

        // Setup ToughJet mock to return a malformed JSON response
        stubFor(post(urlEqualTo("/toughjet/flights"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    [
                        {
                            "carrier": "KLM",
                            "basePrice": "not-a-number", // This will cause a parsing error
                            "tax": 10.00,
                            "discount": 5.00,
                            "departureAirportName": "LHR",
                            "arrivalAirportName": "AMS",
                            "outboundDateTime": "2023-01-01T11:00:00Z",
                            "inboundDateTime": "2023-01-01T13:00:00Z"
                        }
                    ]
                """.trimIndent())
            )
        )

        // Given
        val requestDto = FlightSearchRequestDto(
            origin = "LHR",
            destination = "AMS",
            departureDate = LocalDate.of(2023, 1, 1),
            returnDate = LocalDate.of(2023, 1, 2),
            numberOfPassengers = 2
        )

        val url = "http://localhost:$port/flights/search"
        val request = HttpEntity(requestDto)

        // When
        val response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            request,
            object : ParameterizedTypeReference<List<FlightSearchResponseDto>>() {}
        )

        // Then
        assertEquals(HttpStatus.OK, response.statusCode)
        val flights = response.body

        // Should only have flights from CrazyAir
        assertEquals(1, flights?.size)
        assertEquals("CrazyAir", flights?.get(0)?.supplier)

        // Verify both suppliers were called
        verify(postRequestedFor(urlEqualTo("/crazyair/flights")))
        verify(postRequestedFor(urlEqualTo("/toughjet/flights")))
    }
}
