package org.deblock.exercise.domain.model

/**
 * Represents an IATA airport code.
 * This is a value class that ensures the code is valid according to IATA standards.
 */
@JvmInline
value class AirportCode private constructor(val code: String) {
    companion object {
        /**
         * Creates a new AirportCode instance after validating and normalizing the input.
         *
         * @param code The airport code to validate and normalize
         * @return A valid AirportCode instance
         * @throws IllegalArgumentException if the code is invalid
         */
        fun create(code: String): AirportCode {
            val trimmedCode = code.trim()
            
            if (trimmedCode.isBlank()) {
                throw IllegalArgumentException("Airport code cannot be blank")
            }
            
            if (trimmedCode.length != 3) {
                throw IllegalArgumentException("Airport code must be exactly 3 characters, got: $trimmedCode")
            }
            
            if (!trimmedCode.all { it.isLetter() }) {
                throw IllegalArgumentException("Airport code must contain only letters, got: $trimmedCode")
            }
            
            return AirportCode(trimmedCode.uppercase())
        }
    }
    
    override fun toString(): String = code
}