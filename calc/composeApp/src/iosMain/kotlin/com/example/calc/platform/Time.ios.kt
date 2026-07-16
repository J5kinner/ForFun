package com.example.calc.platform

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun epochSeconds(): Long = NSDate().timeIntervalSince1970.toLong()
