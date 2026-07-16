package com.example.calc.domain

enum class WordSize(val label: String, val bits: Int) {
    BYTE("BYTE", 8),
    WORD("WORD", 16),
    DWORD("DWORD", 32),
    QWORD("QWORD", 64),
}
