package com.example.calc

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calc.data.HistoryRepository
import com.example.calc.presentation.CalculatorViewModel
import com.example.calc.ui.CalculatorScreen
import com.example.calc.ui.theme.AppTheme

@Composable
fun App(repository: HistoryRepository) {
    AppTheme {
        val vm = viewModel { CalculatorViewModel(repository) }
        CalculatorScreen(vm)
    }
}
