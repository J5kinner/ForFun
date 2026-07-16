package com.example.calc.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.calc.db.AppDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightHistoryRepository(
    db: AppDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : HistoryRepository {
    private val queries = db.historyQueries

    override fun observeHistory(): Flow<List<HistoryRecord>> =
        queries.selectAll().asFlow().mapToList(dispatcher).map { rows ->
            rows.map { HistoryRecord(it.id, it.expression, it.result, it.timestamp) }
        }

    override suspend fun add(expression: String, result: String) {
        withContext(dispatcher) { queries.insertEntry(expression, result) }
    }

    override suspend fun clear() {
        withContext(dispatcher) { queries.deleteAll() }
    }
}
