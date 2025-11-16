package org.deblock.exercise.adapter.supplier.toughjet

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate


data class ToughJetRequest(
    val from: String,
    val to: String,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val outboundDate: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val inboundDate: LocalDate?,
    val numberOfAdults: Int
)