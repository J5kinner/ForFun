package com.example.calc.presentation

import com.example.calc.domain.Radix
import com.example.calc.domain.RadixFormatter
import com.example.calc.domain.WordSize

enum class ProgError { DivByZero }

data class ProgrammerState(
    val wordSize: WordSize = WordSize.QWORD,
    val radix: Radix = Radix.DEC,
    val entry: Long = 0L, // masked current entry (the "displayed" value)
    val acc: Long = 0L, // masked accumulator (lhs of a pending binary op)
    val pending: ProgBinOp? = null,
    val freshEntry: Boolean = true, // next digit replaces entry rather than appending
    val error: ProgError? = null,
)

fun ProgrammerState.displayValue(): String =
    if (error == ProgError.DivByZero) "Can't divide by 0"
    else RadixFormatter.format(entry, radix, wordSize)
