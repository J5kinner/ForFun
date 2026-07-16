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
import kotlinx.coroutines.launch

class CalculatorViewModel(
    private val repository: HistoryRepository,
    engine: MathEngine = MathEngine(),
) : ViewModel() {

    private val reducer = CalculatorReducer(engine)
    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()
    private val _effects = MutableSharedFlow<CalculatorEffect>(extraBufferCapacity = 16)
    val effects: SharedFlow<CalculatorEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.observeHistory().collect { items ->
                _state.value = reducer.reduce(_state.value, CalculatorIntent.HistoryLoaded(items)).state
            }
        }
    }

    fun dispatch(intent: CalculatorIntent) {
        val reduction = reducer.reduce(_state.value, intent)
        _state.value = reduction.state
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
