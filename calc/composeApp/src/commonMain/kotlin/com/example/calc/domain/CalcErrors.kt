package com.example.calc.domain

enum class CalcError { DivByZero, Overflow, Malformed }

class MalformedExpressionException : Exception("Malformed expression")
class DivideByZeroException : Exception("Division by zero")
