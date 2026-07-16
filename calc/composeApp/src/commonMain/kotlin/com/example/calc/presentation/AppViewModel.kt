package com.example.calc.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calc.data.HistoryRepository
import com.example.calc.domain.MathEngine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Root ViewModel hosting all modes. It routes intents to per-mode pure reducers
 * and keeps a single [AppState] / [StateFlow] the UI collects.
 */
class AppViewModel(
    private val repository: HistoryRepository,
    engine: MathEngine = MathEngine(),
) : ViewModel() {

    private val standardReducer = CalculatorReducer(engine)

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()
    private val _effects = MutableSharedFlow<CalculatorEffect>(extraBufferCapacity = 16)
    val effects: SharedFlow<CalculatorEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.observeHistory().collect { items ->
                _state.update { it.copy(standard = it.standard.copy(history = items)) }
            }
        }
    }

    fun selectMode(mode: CalcMode) = _state.update { it.copy(mode = mode) }

    fun onStandard(intent: CalculatorIntent) {
        val reduction = standardReducer.reduce(_state.value.standard, intent)
        _state.update { it.copy(standard = reduction.state) }
        for (effect in reduction.effects) {
            when (effect) {
                is CalculatorEffect.PersistHistory ->
                    viewModelScope.launch { repository.add(effect.expression, effect.result) }
                else -> _effects.tryEmit(effect)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch { repository.clear() }
    }
}
