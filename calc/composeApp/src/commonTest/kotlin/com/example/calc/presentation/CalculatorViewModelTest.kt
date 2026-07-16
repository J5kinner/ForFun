package com.example.calc.presentation

import com.example.calc.domain.Operator
import com.example.calc.support.FakeHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalculatorViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @BeforeTest fun setup() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun dispatchUpdatesState() = runTest(dispatcher) {
        val vm = CalculatorViewModel(FakeHistoryRepository())
        vm.dispatch(CalculatorIntent.Digit('2'))
        vm.dispatch(CalculatorIntent.Op(Operator.Plus))
        vm.dispatch(CalculatorIntent.Digit('3'))
        assertEquals("2+3", vm.state.value.input)
        assertEquals("5", vm.state.value.preview)
    }

    @Test fun equalsPersistsHistory() = runTest(dispatcher) {
        val repo = FakeHistoryRepository()
        val vm = CalculatorViewModel(repo)
        vm.dispatch(CalculatorIntent.Digit('2'))
        vm.dispatch(CalculatorIntent.Op(Operator.Plus))
        vm.dispatch(CalculatorIntent.Digit('3'))
        vm.dispatch(CalculatorIntent.Equals)
        testScheduler.advanceUntilIdle()
        assertTrue(repo.added.contains("2+3" to "5"))
        assertEquals(1, vm.state.value.history.size)
    }
}
