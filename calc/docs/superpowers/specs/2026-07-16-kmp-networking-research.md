I now have everything needed. Here is the research report.

---

# Adding HTTP + JSON to KMP for Live Currency Rates

**Bottom line on version pinning:** Your Kotlin is locked at **2.2.20**. Kotlin libraries built against a *newer* Kotlin metadata version (2.3.x / 2.4.x) can trigger "compiled with a newer Kotlin, expected 2.2" errors/warnings when consumed by the 2.2.20 compiler. So pick the last releases built on Kotlin 2.2.x:

- **Ktor 3.3.3** — the last 3.3.x patch, built with **Kotlin 2.2.20** (Ktor 3.4.0+ moved to Kotlin 2.3). This is the safest match for your locked stack. ([Ktor releases](https://github.com/ktorio/ktor/releases))
- **kotlinx-serialization 1.9.0** — built on **Kotlin 2.2.0**, the 1.9.x line that tracks Kotlin 2.2 (1.10.0/1.11.0 moved to Kotlin 2.3). ([serialization releases](https://github.com/Kotlin/kotlinx.serialization/releases/tag/v1.9.0))

Avoid Ktor 3.4.x/3.5.x and serialization 1.10/1.11 unless you unlock Kotlin.

---

## 1. Ktor client — artifacts & versions

Version: **`3.3.3`** (all Ktor artifacts share one version).

| Purpose | Source set | Maven coordinate |
|---|---|---|
| Core client | commonMain | `io.ktor:ktor-client-core:3.3.3` |
| Content negotiation plugin | commonMain | `io.ktor:ktor-client-content-negotiation:3.3.3` |
| kotlinx JSON serializer | commonMain | `io.ktor:ktor-serialization-kotlinx-json:3.3.3` |
| Android engine | androidMain | `io.ktor:ktor-client-okhttp:3.3.3` |
| iOS engine | iosMain | `io.ktor:ktor-client-darwin:3.3.3` |

Notes:
- **Android engine choice:** prefer **`ktor-client-okhttp`** over `ktor-client-android`. OkHttp supports HTTP/2, connection pooling, modern TLS, and gets independent updates — the better default for a real app. `ktor-client-android` (bare `HttpURLConnection`) is only worth it to shave the OkHttp dependency. Either works on minSdk 24.
- **iOS engine** `ktor-client-darwin` wraps `NSURLSession` — no extra pods needed.

`build.gradle.kts` (KMP) source-set wiring:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.ktor:ktor-client-core:3.3.3")
            implementation("io.ktor:ktor-client-content-negotiation:3.3.3")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.3")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
        }
        androidMain.dependencies {
            implementation("io.ktor:ktor-client-okhttp:3.3.3")
        }
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:3.3.3")
        }
    }
}
```

---

## 2. kotlinx-serialization — plugin + runtime

- **Gradle plugin id:** `org.jetbrains.kotlin.plugin.serialization`
- **Plugin version:** must equal the Kotlin version → **`2.2.20`** (the serialization compiler plugin ships in lockstep with the Kotlin compiler).
- **JSON runtime artifact:** `org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0` (versioned independently of the plugin; see coordinate above in commonMain).

Apply in `build.gradle.kts`:

```kotlin
plugins {
    kotlin("multiplatform") version "2.2.20"
    // ...
    kotlin("plugin.serialization") version "2.2.20"
    // equivalently: id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
}
```

If you use a version catalog, the plugin is often declared as `kotlin("plugin.serialization")` with the version inherited from the Kotlin BOM/plugin alias. Since your other modules already pin Kotlin 2.2.20, keep the serialization plugin at the identical string.

---

## 3. Free, no-key, HTTPS exchange-rate endpoint

**Recommended: ExchangeRate-API Open Access** — no API key, HTTPS, no auth. ([docs](https://www.exchangerate-api.com/docs/free))

- **URL:** `https://open.er-api.com/v6/latest/USD` (swap `USD` for any base code).

**Example JSON response shape:**

```json
{
  "result": "success",
  "provider": "https://www.exchangerate-api.com",
  "documentation": "https://www.exchangerate-api.com/docs/free",
  "terms_of_use": "https://www.exchangerate-api.com/terms",
  "time_last_update_unix": 1752624001,
  "time_last_update_utc": "Wed, 16 Jul 2026 00:00:01 +0000",
  "time_next_update_unix": 1752710401,
  "time_next_update_utc": "Thu, 17 Jul 2026 00:00:01 +0000",
  "time_eol_unix": 0,
  "base_code": "USD",
  "rates": {
    "USD": 1,
    "EUR": 0.9243,
    "GBP": 0.7891,
    "JPY": 157.23,
    "AUD": 1.5012
  }
}
```

**Caveats / terms:**
- **Update frequency:** rates refresh once every ~24 h (this is a free daily-rate feed, not tick-by-tick "live"). Fine for a hobby calculator.
- **Rate limits:** requesting once per hour (or once per 24 h) never hits limits. Over-limit returns **HTTP 429**; the block lifts after ~20 minutes. Cache the response locally and refresh at most hourly.
- **Attribution required:** include a link `Rates By Exchange Rate API` → `https://www.exchangerate-api.com` somewhere in the app (can be styled/discreet).
- **Terms:** caching for personal/commercial conversion is allowed; **re-distributing the raw rate data is prohibited.**

**Alternatives:** `exchangerate.host` and `frankfurter.app` (ECB data, fully free/no-key: `https://api.frankfurter.dev/v1/latest?base=USD`) are good fallbacks; Frankfurter has no attribution requirement but only covers ~30 major currencies and is also daily.

---

## 4. Android manifest — INTERNET permission & network config

Add to `androidApp/src/main/AndroidManifest.xml` (or the shared module manifest), as a direct child of `<manifest>`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Network/cleartext notes:
- The endpoint is **HTTPS**, so you do **not** need `android:usesCleartextTraffic="true"` or a custom `network_security_config.xml`. On compileSdk 36 / API 28+, cleartext is disabled by default — keep it that way.
- No `ACCESS_NETWORK_STATE` is required for basic fetching (only if you want to check connectivity first).
- minSdk 24 is fully fine for OkHttp + modern TLS.

---

## 5. Minimal commonMain Ktor setup + platform engine injection

**expect/actual engine factory** — keeps the shared client platform-agnostic.

`commonMain/.../HttpClientFactory.kt`:
```kotlin
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine

// Each platform supplies its engine.
expect fun httpClientEngine(): HttpClientEngine
```

`commonMain/.../ApiClient.kt`:
```kotlin
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ExchangeRateResponse(
    val result: String,
    val base_code: String,
    val time_last_update_unix: Long,
    val rates: Map<String, Double>
)

fun createHttpClient(): HttpClient =
    HttpClient(httpClientEngine()) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true   // tolerate extra provider/time fields
                isLenient = true
            })
        }
    }

class RatesApi(private val client: HttpClient = createHttpClient()) {
    suspend fun latest(base: String = "USD"): ExchangeRateResponse =
        client.get("https://open.er-api.com/v6/latest/$base").body()
}
```

`androidMain/.../HttpClientFactory.android.kt`:
```kotlin
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual fun httpClientEngine(): HttpClientEngine = OkHttp.create()
```

`iosMain/.../HttpClientFactory.ios.kt`:
```kotlin
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

actual fun httpClientEngine(): HttpClientEngine = Darwin.create()
```

Notes:
- `ignoreUnknownKeys = true` is important — the response has many `time_*`/`provider` fields you don't need to model; without it, deserialization throws on the first unknown key.
- Rates are returned as JSON numbers. Modeling them as `Double` is simplest; if you want to keep them exact for your **ionspin bignum 0.3.10** math pipeline, deserialize `rates` as `Map<String, String>` via a custom serializer or post-convert the doubles into `BigDecimal`/`BigInteger` — but note the API only sends ~4-decimal precision, so `Double` → bignum on read is acceptable for a hobby app.
- Prefer a single long-lived `HttpClient` instance; call `client.close()` on teardown.

---

## Sources
- [Ktor releases (GitHub)](https://github.com/ktorio/ktor/releases) — 3.3.3 = last Kotlin 2.2.20 build; 3.4.0+ = Kotlin 2.3
- [Ktor releases doc](https://ktor.io/docs/releases.html) — 3.3.1 "updates Kotlin to 2.2.20"
- [Ktor client dependencies](https://ktor.io/docs/client-dependencies.html)
- [kotlinx.serialization 1.9.0 release](https://github.com/Kotlin/kotlinx.serialization/releases/tag/v1.9.0) — based on Kotlin 2.2.0
- [serialization Gradle plugin](https://plugins.gradle.org/plugin/org.jetbrains.kotlin.plugin.serialization)
- [ExchangeRate-API free/open-access docs](https://www.exchangerate-api.com/docs/free)
- [Frankfurter API (fallback)](https://frankfurter.dev/)