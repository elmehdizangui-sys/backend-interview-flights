package org.deblock.exercise.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AirportCodeTest {

    @Test
    fun `should create valid airport code and normalize to uppercase`() {
        val code = "cdg"
        
        val airportCode = AirportCode.create(code)
        
        assertEquals("CDG", airportCode.code)
    }
    
    @Test
    fun `should create valid airport code and trim whitespace`() {
        val code = " LHR "
        
        val airportCode = AirportCode.create(code)
        
        // Then code should be trimmed and normalized
        assertEquals("LHR", airportCode.code)
    }
    
    @Test
    fun `should throw exception for blank code`() {
        val code = ""
        
        val exception = assertThrows<IllegalArgumentException> {
            AirportCode.create(code)
        }
        
        assertEquals("Airport code cannot be blank", exception.message)
    }
    
    @Test
    fun `should throw exception for code with less than 3 characters`() {
        // Given code with less than 3 characters
        val code = "AB"
        
        val exception = assertThrows<IllegalArgumentException> {
            AirportCode.create(code)
        }
        
        // Then exception message should be clear
        assertEquals("Airport code must be exactly 3 characters, got: AB", exception.message)
    }
    
    @Test
    fun `should throw exception for code with more than 3 characters`() {
        // Given code with more than 3 characters
        val code = "ABCD"
        
        val exception = assertThrows<IllegalArgumentException> {
            AirportCode.create(code)
        }
        
        // Then exception message should be clear
        assertEquals("Airport code must be exactly 3 characters, got: ABCD", exception.message)
    }
    
    @Test
    fun `should throw exception for code with digits`() {
        // Given code with digits
        val code = "A1C"
        
        val exception = assertThrows<IllegalArgumentException> {
            AirportCode.create(code)
        }
        
        // Then exception message should be clear
        assertEquals("Airport code must contain only letters, got: A1C", exception.message)
    }
    
    @Test
    fun `should throw exception for code with symbols`() {
        // Given code with symbols
        val code = "@@@"
        
        val exception = assertThrows<IllegalArgumentException> {
            AirportCode.create(code)
        }
        
        // Then exception message should be clear
        assertEquals("Airport code must contain only letters, got: @@@", exception.message)
    }
}