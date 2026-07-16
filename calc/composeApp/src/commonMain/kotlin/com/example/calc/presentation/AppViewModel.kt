package com.example.calc.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calc.data.HistoryRepository
import com.example.calc.domain.MathEngine
import com.example.calc.domain.convert.currency.BundledOnlyProvider
import com.example.calc.domain.convert.currency.DefaultExchangeRateRepository
import com.example.calc.domain.convert.currency.ExchangeRateRepository
import com.example.calc.domain.convert.currency.InMemoryRatesCache
import com.example.calc.presentation.convert.ConverterEffect
import com.example.calc.presentation.convert.ConverterIntent
import com.example.calc.presentation.convert.ConverterReducer
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
    rateRepo: ExchangeRateRepository? = null,
    engine: MathEngine = MathEngine(),
) : ViewModel() {

    private val standardReducer = CalculatorReducer(engine)
    private val programmerReducer = ProgrammerReducer()
    private val converterReducer = ConverterReducer()
    private val ratesRepository: ExchangeRateRepository =
        rateRepo ?: DefaultExchangeRateRepository(BundledOnlyProvider, InMemoryRatesCache())

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

    fun onProgrammer(intent: ProgrammerIntent) {
        val prev = _state.value.programmer
        val next = programmerReducer.reduce(prev, intent)
        _state.update { it.copy(programmer = next) }
        if (next.error != null && prev.error == null) _effects.tryEmit(CalculatorEffect.ErrorBlip)
    }

    fun onConverter(intent: ConverterIntent) {
        val reduction = converterReducer.reduce(_state.value.converter, intent)
        _state.update { it.copy(converter = reduction.state) }
        for (effect in reduction.effects) {
            when (effect) {
                is ConverterEffect.RequestRates -> viewModelScope.launch {
                    val rates = ratesRepository.getRates(effect.base) // never throws (falls to bundled)
                    onConverter(ConverterIntent.RatesLoaded(rates))
                }
                ConverterEffect.Haptic -> {} // handled by the keypad
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch { repository.clear() }
    }
}
