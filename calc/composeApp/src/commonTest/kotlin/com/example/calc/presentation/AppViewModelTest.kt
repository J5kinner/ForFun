package com.example.calc.presentation

import com.example.calc.domain.Operator
import com.example.calc.support.FakeHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    @Test fun programmerIntentsUpdateOnlyProgrammer() = runTest(dispatcher) {
        val vm = AppViewModel(FakeHistoryRepository())
        vm.onStandard(CalculatorIntent.Digit('9'))
        vm.onProgrammer(ProgrammerIntent.Digit(7))
        assertEquals(7L, vm.state.value.programmer.entry)
        assertEquals("9", vm.state.value.standard.input) // untouched
    }

    @Test fun programmerDivByZeroEmitsErrorBlip() = runTest(dispatcher) {
        val vm = AppViewModel(FakeHistoryRepository())
        val effects = mutableListOf<CalculatorEffect>()
        val job = launch { vm.effects.collect { effects.add(it) } }
        testScheduler.advanceUntilIdle()
        vm.onProgrammer(ProgrammerIntent.Digit(5))
        vm.onProgrammer(ProgrammerIntent.Binary(ProgBinOp.Div))
        vm.onProgrammer(ProgrammerIntent.Digit(0))
        vm.onProgrammer(ProgrammerIntent.Equals)
        testScheduler.advanceUntilIdle()
        assertTrue(effects.any { it is CalculatorEffect.ErrorBlip })
        job.cancel()
    }
}
