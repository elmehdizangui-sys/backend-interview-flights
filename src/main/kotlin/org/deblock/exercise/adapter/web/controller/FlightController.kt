package org.deblock.exercise.adapter.web.controller

import jakarta.validation.Valid
import org.deblock.exercise.adapter.web.dto.FlightSearchRequestDto
import org.deblock.exercise.adapter.web.dto.FlightSearchResponseDto
import org.deblock.exercise.adapter.web.mapper.FlightMapper
import org.deblock.exercise.application.port.inbound.FlightSearchUseCase
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/flights")
class FlightController(
    private val flightSearchUseCase: FlightSearchUseCase,
    private val flightMapper: FlightMapper
) {
    private val logger = LoggerFactory.getLogger(FlightController::class.java)

    @PostMapping("/search")
    suspend fun searchFlights(@Valid @RequestBody requestDto: FlightSearchRequestDto): ResponseEntity<List<FlightSearchResponseDto>> {
        logger.debug("Searching flights from ${requestDto.origin} to ${requestDto.destination}")

        val request = flightMapper.toFlightSearchRequest(requestDto)
        val flights = flightSearchUseCase.searchFlights(request)

        val flightDtos = flights.map { flightMapper.toFlightDto(it) }

        return ResponseEntity.ok(flightDtos)
    }
}
