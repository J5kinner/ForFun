package com.example.calc

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.example.calc.data.DatabaseDriverFactory
import com.example.calc.data.SqlDelightHistoryRepository
import com.example.calc.data.createDatabase

fun MainViewController() = ComposeUIViewController {
    val repository = remember {
        SqlDelightHistoryRepository(createDatabase(DatabaseDriverFactory()))
    }
    App(repository)
}
