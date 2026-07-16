package com.example.calc

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calc.data.HistoryRepository
import com.example.calc.presentation.AppViewModel
import com.example.calc.ui.CalculatorApp
import com.example.calc.ui.theme.AppTheme

@Composable
fun App(repository: HistoryRepository) {
    AppTheme {
        val vm = viewModel { AppViewModel(repository) }
        CalculatorApp(vm)
    }
}
