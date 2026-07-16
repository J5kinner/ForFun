package com.example.calc.support

import com.example.calc.data.HistoryRecord
import com.example.calc.data.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeHistoryRepository : HistoryRepository {
    val flow = MutableStateFlow<List<HistoryRecord>>(emptyList())
    val added = mutableListOf<Pair<String, String>>()
    override fun observeHistory(): Flow<List<HistoryRecord>> = flow
    override suspend fun add(expression: String, result: String) {
        added += expression to result
        flow.value = flow.value + HistoryRecord(flow.value.size + 1L, expression, result, 0L)
    }
    override suspend fun clear() { flow.value = emptyList() }
}
