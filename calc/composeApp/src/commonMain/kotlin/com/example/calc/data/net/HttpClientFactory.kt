package com.example.calc.data.net

import io.ktor.client.engine.HttpClientEngine

/** Platform HTTP engine: OkHttp on Android, Darwin (NSURLSession) on iOS. */
expect fun httpClientEngine(): HttpClientEngine
