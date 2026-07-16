package com.example.calc.platform

actual fun epochSeconds(): Long = System.currentTimeMillis() / 1000
