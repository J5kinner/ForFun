package com.example.calc.data

import app.cash.sqldelight.db.SqlDriver
import com.example.calc.db.AppDatabase

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(factory: DatabaseDriverFactory): AppDatabase =
    AppDatabase(factory.createDriver())
