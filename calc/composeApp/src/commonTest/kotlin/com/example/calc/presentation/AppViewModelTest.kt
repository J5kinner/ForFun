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

class AppViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @BeforeTest fun setup() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun standardIntentsUpdateSubState() = runTest(dispatcher) {
        val vm = AppViewModel(FakeHistoryRepository())
        vm.onStandard(CalculatorIntent.Digit('2'))
        vm.onStandard(CalculatorIntent.Op(Operator.Plus))
        vm.onStandard(CalculatorIntent.Digit('3'))
        assertEquals("2+3", vm.state.value.standard.input)
        assertEquals("5", vm.state.value.standard.preview)
    }

    @Test fun equalsPersistsHistory() = runTest(dispatcher) {
        val repo = FakeHistoryRepository()
        val vm = AppViewModel(repo)
        vm.onStandard(CalculatorIntent.Digit('2'))
        vm.onStandard(CalculatorIntent.Op(Operator.Plus))
        vm.onStandard(CalculatorIntent.Digit('3'))
        vm.onStandard(CalculatorIntent.Equals)
        testScheduler.advanceUntilIdle()
        assertTrue(repo.added.contains("2+3" to "5"))
        assertEquals(1, vm.state.value.standard.history.size)
    }

    @Test fun selectModePreservesStandardSubState() = runTest(dispatcher) {
        val vm = AppViewModel(FakeHistoryRepository())
        vm.onStandard(CalculatorIntent.Digit('7'))
        vm.selectMode(CalcMode.Programmer)
        assertEquals(CalcMode.Programmer, vm.state.value.mode)
        assertEquals("7", vm.state.value.standard.input)
    }
}
