package com.example.calc.domain

sealed interface Token {
    data class Num(val text: String) : Token
    data class Op(val operator: Operator) : Token
    data object Percent : Token
    data object LParen : Token
    data object RParen : Token
    data object UnaryMinus : Token
}
