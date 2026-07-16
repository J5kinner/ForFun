package com.example.calc.data

data class HistoryRecord(
    val id: Long,
    val expression: String,
    val result: String,
    val timestamp: Long,
)
