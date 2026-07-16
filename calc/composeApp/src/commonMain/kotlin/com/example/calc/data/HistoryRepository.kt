package com.example.calc.data

import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun observeHistory(): Flow<List<HistoryRecord>>
    suspend fun add(expression: String, result: String)
    suspend fun clear()
}
