I now have a complete picture of the codebase conventions. Here is the detailed plan.

---

# Unit Converter Mode — TDD Implementation Plan

**Date:** 2026-07-16
**Scope:** Second vertical slice — a full **Unit Converter** mode (Length, Weight, Temperature, Area, Volume, Speed, Currency) that slots into the existing KMP calculator without touching the tested Standard-mode reducer/engine.

This plan matches the established conventions verified in the repo:
- Clean package layering `domain / data / presentation / ui` under `com.example.calc`.
- Pure logic in `domain`, pure MVI `Reducer` returning `Reduction(state, effects)`, a `androidx.lifecycle.ViewModel` exposing `StateFlow` + `SharedFlow<Effect>`.
- `com.ionspin.kotlin.bignum.decimal.BigDecimal` everywhere — **never `Double`**. API in use: `.multiply/.add/.subtract/.divide(x, DecimalMode(...))`, `BigDecimal.parseString`, `BigDecimal.fromInt`, `.toStringExpanded()`, `.signum()`, `.isZero()`, `.roundSignificand(DecimalMode)`.
- `kotlin.test` (`@Test fun`, `assertEquals/assertTrue/assertNull`) + `kotlinx-coroutines-test` (`runTest`) in `commonTest`.
- Data-driven keypad `KeyPad = List<List<Key>>`, `Key(label, style, intent, span)`.
- Repository pattern like `HistoryRepository` (interface in `data`, `Flow`, fakes in `commonTest/support`).
- `expect/actual` only where a platform API is required (mirrors `DatabaseDriverFactory`).

## 0. Architecture decision (approved judgment call)

The Converter is implemented as a **parallel MVI slice**, not folded into `CalculatorReducer`/`CalculatorState`. Rationale:
- The Standard reducer is pure and fully tested; currency needs **async rate loading** (coroutines + effects), which does not belong in the string-building calculator reducer.
- A separate `ConverterState / ConverterIntent / ConverterReducer / ConverterViewModel` keeps each slice small, independently testable, and matches the "modes slot in as their own cycles" statement in the Standard design doc (§1).

`CalcMode` gains a `UnitConverter` case, and a thin **host** at the app root swaps between `CalculatorScreen` and `ConverterScreen` via a shared mode toggle. No changes to `MathEngine`, `CalculatorReducer`, or their tests.

### New file layout

```
commonMain/kotlin/com/example/calc/
  domain/convert/
    UnitConversion.kt        // Ratio + Affine sealed model
    UnitDef.kt               // one unit (id, symbol, name, conversion)
    UnitCategory.kt          // enum: units + base + defaults
    ConversionEngine.kt      // pure convert(value, from, to)
    ConversionFormatter.kt   // round-to-significant + reuse NumberFormatter
    currency/
      ExchangeRates.kt       // rate table value type + Source
      ExchangeRateProvider.kt   // network port (injected)
      ExchangeRateRepository.kt // interface
      RatesCache.kt          // last-successful-fetch cache port
      BundledRates.kt        // compiled-in offline fallback
      DefaultExchangeRateRepository.kt
      CurrencyConversion.kt  // pure convert over a rate table
  presentation/convert/
    ConverterState.kt
    ConverterIntent.kt
    ConverterEffect.kt
    ConverterReducer.kt      // PURE dual-input sync
    ConverterViewModel.kt    // StateFlow + async rate load
  ui/convert/
    ConverterScreen.kt
    CategorySelector.kt
    UnitPickerRow.kt
    ConverterKeypad.kt       // reuses CalculatorKeypad renderer
  ui/CalculatorHost.kt       // mode toggle + screen switch

commonTest/kotlin/com/example/calc/
  domain/convert/
    ConversionEngineLengthTest.kt
    ConversionEngineWeightTest.kt
    ConversionEngineAreaTest.kt
    ConversionEngineVolumeTest.kt
    ConversionEngineSpeedTest.kt
    ConversionEngineTemperatureTest.kt
    currency/
      CurrencyConversionTest.kt
      DefaultExchangeRateRepositoryTest.kt
  presentation/convert/
    ConverterReducerTest.kt
    ConverterViewModelTest.kt
  support/
    FakeExchangeRateProvider.kt
    FakeRatesCache.kt
```

No new Gradle dependencies are required — everything uses `bignum`, `coroutines`, `kotlin.test` already locked in `libs.versions.toml`. (A live network client is **out of scope**: the network layer is an injected port, per the task.)

---

## Task 1 — Unit conversion model (`domain/convert/UnitConversion.kt`) [TDD]

**Design.** Two conversion kinds. Linear categories use a base-unit **ratio**; temperature uses an **affine** map. To keep factors and affine constants *exact* (no repeating decimals in the definitions), base units and the affine representation are chosen deliberately (see Task 2).

Affine is represented as an **integer/decimal rational** `base = (value·mulNum + addNum) / den`, which makes Celsius/Fahrenheit/Kelvin exact.

**Write the test first** — `ConversionEngineTemperatureTest` (Task 7) exercises this indirectly; add a focused `UnitConversionTest` for the primitives:

```kotlin
// commonTest .../domain/convert/UnitConversionTest.kt
class UnitConversionTest {
    private val mode = DecimalMode(30, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
    @Test fun ratioRoundTrips() {
        val km = Ratio(BigDecimal.parseString("1000"))
        val base = km.toBase(BigDecimal.fromInt(2))          // 2 km -> 2000 m
        assertEquals("2000", base.toStringExpanded())
        assertEquals("2", km.fromBase(base, mode).toStringExpanded())
    }
    @Test fun affineFahrenheitExact() {                       // C = (F*5 - 160)/9
        val f = Affine(mulNum = bd(5), addNum = bd(-160), den = bd(9))
        assertEquals("0", f.toBase(bd(32), mode).toStringExpanded())   // 32F -> 0C
        assertEquals("32", f.fromBase(bd(0), mode).toStringExpanded()) // 0C -> 32F
    }
}
```

**Implementation:**

```kotlin
package com.example.calc.domain.convert

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode

/** Maps a unit's value to/from the category's canonical base unit. */
sealed interface UnitConversion {
    fun toBase(value: BigDecimal): BigDecimal
    fun fromBase(base: BigDecimal, mode: DecimalMode): BigDecimal
}

/** Linear: base = value * factor. `factor` is exact (see UnitCategory). */
data class Ratio(val factor: BigDecimal) : UnitConversion {
    override fun toBase(value: BigDecimal) = value.multiply(factor)
    override fun fromBase(base: BigDecimal, mode: DecimalMode) = base.divide(factor, mode)
}

/** Affine (temperature): base = (value*mulNum + addNum) / den, all constants exact. */
data class Affine(val mulNum: BigDecimal, val addNum: BigDecimal, val den: BigDecimal) : UnitConversion {
    override fun toBase(value: BigDecimal) =
        value.multiply(mulNum).add(addNum).divide(den, EXACTISH)
    override fun fromBase(base: BigDecimal, mode: DecimalMode) =
        base.multiply(den).subtract(addNum).divide(mulNum, mode)
    companion object { private val EXACTISH = DecimalMode(50, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO) }
}
```

Note the asymmetry: `toBase` for `Affine` divides by `den` — for the canonical test values (0/32/100/212/273.15) this is exact; general values round at the *final* `fromBase` step under the caller's display mode.

---

## Task 2 — Unit catalogue & categories (`UnitDef.kt`, `UnitCategory.kt`) [TDD]

**Base-unit choices (picked so every factor terminates exactly):**

| Category | Base | Sample units → factor to base (exact) |
|---|---|---|
| Length | metre | km 1000, m 1, cm 0.01, mm 0.001, mi 1609.344, yd 0.9144, ft 0.3048, in 0.0254, nmi 1852 |
| Weight | kilogram | kg 1, g 0.001, mg 0.000001, t 1000, lb 0.45359237, oz 0.028349523125, st 6.35029318 |
| Area | square metre | km² 1000000, m² 1, cm² 0.0001, ha 10000, acre 4046.8564224, ft² 0.09290304, in² 0.00064516, mi² 2589988.110336 |
| Volume | litre | L 1, mL 0.001, m³ 1000, gal(US) 3.785411784, qt(US) 0.946352946, pt(US) 0.473176473, cup(US) 0.2365882365, floz(US) 0.0295735295625 |
| Speed | **km/h** | km/h 1, m/s 3.6, mph 1.609344, ft/s 1.09728, knot 1.852 |
| Temperature | **Celsius** | affine (see Task 1) |
| Currency | (rate base, dynamic) | handled by rate table, not static factors |

Choosing **km/h** as the speed base and **metre/kg/m²/L** as the others makes every listed factor a terminating decimal (no `1/3.6` in the definitions).

**Test first:**

```kotlin
// commonTest .../domain/convert/UnitCategoryTest.kt
class UnitCategoryTest {
    @Test fun everyLinearCategoryHasExactFactors() {
        UnitCategory.linearCategories.flatMap { it.units }.forEach { u ->
            val c = u.conversion
            assertTrue(c is Ratio || c is Affine)
        }
    }
    @Test fun defaultsAreDistinct() =
        UnitCategory.entries.filter { it.units.size >= 2 }.forEach {
            assertTrue(it.defaultFrom != it.defaultTo)
        }
    @Test fun lengthContainsMetreAsBase() {
        val m = UnitCategory.Length.units.first { it.id == "m" }
        assertEquals(BigDecimal.parseString("1"), (m.conversion as Ratio).factor)
    }
}
```

**Implementation (excerpt):**

```kotlin
data class UnitDef(val id: String, val symbol: String, val name: String, val conversion: UnitConversion)

enum class UnitCategory(val displayName: String, val units: List<UnitDef>) {
    Length("Length", listOf(
        UnitDef("km", "km", "Kilometre", Ratio(bd("1000"))),
        UnitDef("m",  "m",  "Metre",     Ratio(bd("1"))),
        UnitDef("cm", "cm", "Centimetre",Ratio(bd("0.01"))),
        UnitDef("mi", "mi", "Mile",      Ratio(bd("1609.344"))),
        UnitDef("ft", "ft", "Foot",      Ratio(bd("0.3048"))),
        UnitDef("in", "in", "Inch",      Ratio(bd("0.0254"))),
        /* … */)),
    Weight("Weight", /* kg base … */ listOf(/* … */)),
    Area("Area", /* … */ listOf(/* … */)),
    Volume("Volume", /* … */ listOf(/* … */)),
    Speed("Speed", listOf(
        UnitDef("kmh","km/h","Kilometres/hour", Ratio(bd("1"))),
        UnitDef("ms", "m/s", "Metres/second",   Ratio(bd("3.6"))),
        UnitDef("mph","mph", "Miles/hour",      Ratio(bd("1.609344"))),
        UnitDef("kn", "kn",  "Knot",            Ratio(bd("1.852"))),
        UnitDef("fts","ft/s","Feet/second",     Ratio(bd("1.09728"))))),
    Temperature("Temperature", listOf(
        UnitDef("C","°C","Celsius",    Affine(bd("1"),   bd("0"),      bd("1"))),
        UnitDef("F","°F","Fahrenheit", Affine(bd("5"),   bd("-160"),   bd("9"))),   // C=(F*5-160)/9
        UnitDef("K","K", "Kelvin",     Affine(bd("100"), bd("-27315"), bd("100"))))), // C=(K*100-27315)/100
    Currency("Currency", emptyList());   // populated at runtime from rates

    val defaultFrom: UnitDef get() = units.first()
    val defaultTo: UnitDef get() = units.getOrElse(1) { units.first() }

    companion object {
        val linearCategories = listOf(Length, Weight, Area, Volume, Speed, Temperature)
        private fun bd(s: String) = BigDecimal.parseString(s)
    }
}
```

---

## Task 3 — Conversion engine + formatter (`ConversionEngine.kt`, `ConversionFormatter.kt`) [TDD]

**Design.** Pure: `convert(value, from, to)` = `to.fromBase(from.toBase(value), DISPLAY_MODE)`. Same-category assumed (UI guarantees it). Result rounded to display significance, then formatted via the existing `NumberFormatter` for grouping/trimming/scientific fallback.

```kotlin
object ConversionEngine {
    private val DISPLAY_MODE = DecimalMode(12, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
    fun convert(value: BigDecimal, from: UnitDef, to: UnitDef): BigDecimal =
        to.conversion.fromBase(from.conversion.toBase(value), DISPLAY_MODE)
            .roundSignificand(DISPLAY_MODE)
}

object ConversionFormatter {
    /** Reuse the Standard-mode formatter for grouping/trim/scientific. */
    fun format(value: BigDecimal): String = NumberFormatter.format(value)
}
```

Rounding to 12 significant digits before formatting is what turns `37.777…78` into a clean display and keeps exact cases (`500`, `12`, `4`) exact after `NumberFormatter` trims trailing zeros.

---

## Task 4 — Length / Weight / Area / Volume / Speed factor tests [TDD]

One test file per category. **Exact input → expected formatted string.** Sample (`ConversionEngineLengthTest`):

```kotlin
class ConversionEngineLengthTest {
    private fun u(id: String) = UnitCategory.Length.units.first { it.id == id }
    private fun conv(v: String, from: String, to: String) =
        ConversionFormatter.format(ConversionEngine.convert(BigDecimal.parseString(v), u(from), u(to)))

    @Test fun kmToM()   = assertEquals("1,000", conv("1", "km", "m"))
    @Test fun miToM()   = assertEquals("1,609.344", conv("1", "mi", "m"))
    @Test fun mToKm()   = assertEquals("5", conv("5000", "m", "km"))
    @Test fun inToCm()  = assertEquals("2.54", conv("1", "in", "cm"))
    @Test fun ftToIn()  = assertEquals("12", conv("1", "ft", "in"))
    @Test fun cmToM()   = assertEquals("1", conv("100", "cm", "m"))
}
```

**Weight** (`ConversionEngineWeightTest`):
`1 kg→g = 1,000`; `1 lb→g = 453.59237`; `1 oz→g = 28.349523125`; `1 st→lb = 14`; `1 kg→lb = 2.20462262185`.

**Area** (`ConversionEngineAreaTest`):
`1 m²→cm² = 10,000`; `1 ha→m² = 10,000`; `1 acre→m² = 4,046.8564224`; `1 km²→m² = 1,000,000`; `1 ft²→in² = 144`.

**Volume** (`ConversionEngineVolumeTest`):
`1 L→mL = 1,000`; `1 m³→L = 1,000`; `1 gal→L = 3.785411784`; `1 gal→qt = 4`.

**Speed** (`ConversionEngineSpeedTest`):
`1 m/s→km/h = 3.6`; `36 km/h→m/s = 10`; `1 mph→km/h = 1.609344`; `1 kn→km/h = 1.852`; `60 mph→km/h = 96.56064`.

(All expected strings include grouping commas because they route through `NumberFormatter.format`.)

---

## Task 5 — Temperature affine tests [TDD]

`ConversionEngineTemperatureTest` — the affine correctness cases the task calls out explicitly:

```kotlin
class ConversionEngineTemperatureTest {
    private fun u(id: String) = UnitCategory.Temperature.units.first { it.id == id }
    private fun c(v: String, from: String, to: String) =
        ConversionFormatter.format(ConversionEngine.convert(BigDecimal.parseString(v), u(from), u(to)))

    @Test fun freezingCtoF()  = assertEquals("32", c("0", "C", "F"))
    @Test fun boilingCtoF()   = assertEquals("212", c("100", "C", "F"))
    @Test fun freezingCtoK()  = assertEquals("273.15", c("0", "C", "K"))
    @Test fun fToCzero()      = assertEquals("0", c("32", "F", "C"))
    @Test fun fToChundred()   = assertEquals("100", c("212", "F", "C"))
    @Test fun kToCzero()      = assertEquals("0", c("273.15", "K", "C"))
    @Test fun minus40Equal()  = assertEquals("-40", c("-40", "C", "F"))   // the crossover point
    @Test fun bodyTemp()      = assertEquals("98.6", c("37", "C", "F"))
    @Test fun kToF()          = assertEquals("32", c("273.15", "K", "F"))
}
```

The `-40°C = -40°F` and `273.15 K = 0°C = 32°F` cases verify both the affine offset and the round-trip through the Celsius base.

---

## Task 6 — Currency model, provider port, repository & fallback [TDD]

**Value types & ports** (`currency/`):

```kotlin
enum class RatesSource { LIVE, CACHED, BUNDLED }

data class ExchangeRates(
    val base: String,                       // e.g. "USD"
    val rates: Map<String, BigDecimal>,     // code -> units of code per 1 base (base itself = 1)
    val timestamp: Long,                    // epoch seconds of the data
    val source: RatesSource,
)

/** Network port — the actual HTTP client is injected/provided elsewhere (out of scope here). */
interface ExchangeRateProvider {
    suspend fun fetchLatest(base: String): ExchangeRates      // throws on any network/parse failure
}

/** Persisted last-successful fetch. Prod impl can be SQLDelight/file; tests use a fake. */
interface RatesCache {
    suspend fun load(base: String): ExchangeRates?
    suspend fun save(rates: ExchangeRates)
}

interface ExchangeRateRepository {
    /** Never fails: live → stale-cache → bundled, in that order. */
    suspend fun getRates(base: String): ExchangeRates
}
```

**Bundled offline table** (compiled in, `source = BUNDLED`):

```kotlin
object BundledRates {
    fun rates(base: String): ExchangeRates = ExchangeRates(
        base = "USD",
        rates = mapOf("USD" to bd("1"), "EUR" to bd("0.92"), "GBP" to bd("0.79"),
                      "JPY" to bd("157"), "CAD" to bd("1.37"), "AUD" to bd("1.52")),
        timestamp = 0L, source = RatesSource.BUNDLED,
    ).let { if (base == "USD") it else rebase(it, base) }   // rebase helper divides through
}
```

**Repository with robust fallback** (`DefaultExchangeRateRepository.kt`):

```kotlin
class DefaultExchangeRateRepository(
    private val provider: ExchangeRateProvider,
    private val cache: RatesCache,
    private val bundled: BundledRates = BundledRates,
    private val ttlSeconds: Long = 3600,
    private val now: () -> Long = { /* injected clock */ 0L },
) : ExchangeRateRepository {
    override suspend fun getRates(base: String): ExchangeRates {
        cache.load(base)?.let { if (now() - it.timestamp < ttlSeconds) return it }   // fresh cache
        return try {
            provider.fetchLatest(base).also { cache.save(it) }                        // live
        } catch (e: Exception) {
            cache.load(base) ?: bundled.rates(base)                                   // stale cache, else bundled
        }
    }
}
```

**Fakes** (`commonTest/support/`): `FakeExchangeRateProvider(var result: ExchangeRates?, var throwing: Boolean)` and `FakeRatesCache` (in-memory map).

**Repository tests** (`DefaultExchangeRateRepositoryTest`, `runTest`) — offline path fully testable without network:

```kotlin
@Test fun usesLiveWhenAvailableAndCachesIt() = runTest {
    val provider = FakeExchangeRateProvider(result = live("EUR" to "0.9"))
    val cache = FakeRatesCache()
    val repo = DefaultExchangeRateRepository(provider, cache, now = { 10_000 })
    val r = repo.getRates("USD")
    assertEquals(RatesSource.LIVE, r.source)
    assertEquals(live("EUR" to "0.9").rates, cache.load("USD")!!.rates)   // cached
}
@Test fun fallsBackToBundledWhenProviderThrowsAndNoCache() = runTest {
    val repo = DefaultExchangeRateRepository(FakeExchangeRateProvider(throwing = true), FakeRatesCache())
    assertEquals(RatesSource.BUNDLED, repo.getRates("USD").source)
}
@Test fun prefersStaleCacheOverBundledOnFailure() = runTest {
    val cache = FakeRatesCache().apply { save(cached("EUR" to "0.85", ts = 0)) }
    val repo = DefaultExchangeRateRepository(
        FakeExchangeRateProvider(throwing = true), cache, ttlSeconds = 60, now = { 10_000 })
    val r = repo.getRates("USD")
    assertEquals(RatesSource.CACHED, r.source)          // stale but real beats bundled
    assertEquals(bd("0.85"), r.rates["EUR"])
}
@Test fun freshCacheSkipsProvider() = runTest {
    val cache = FakeRatesCache().apply { save(cached("EUR" to "0.8", ts = 9_950)) }
    val provider = FakeExchangeRateProvider(throwing = true)   // must NOT be called
    val repo = DefaultExchangeRateRepository(provider, cache, ttlSeconds = 100, now = { 10_000 })
    assertEquals("0.8", repo.getRates("USD").rates["EUR"]!!.toStringExpanded())
}
```

**Currency conversion** (`CurrencyConversion.kt`, pure): with `rate(code)` = units per 1 base, `amountTo = amountFrom * rate(to) / rate(from)`.

```kotlin
object CurrencyConversion {
    private val MODE = DecimalMode(12, RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)
    fun convert(amount: BigDecimal, from: String, to: String, rates: ExchangeRates): BigDecimal {
        val rf = rates.rates[from] ?: throw IllegalArgumentException(from)
        val rt = rates.rates[to] ?: throw IllegalArgumentException(to)
        return amount.multiply(rt).divide(rf, MODE).roundSignificand(MODE)
    }
}
```

**Currency conversion tests** (`CurrencyConversionTest`) using a fixed fake table `USD=1, EUR=0.9, GBP=0.8, JPY=150`:

```kotlin
private val T = ExchangeRates("USD",
    mapOf("USD" to bd("1"), "EUR" to bd("0.9"), "GBP" to bd("0.8"), "JPY" to bd("150")),
    0L, RatesSource.BUNDLED)
private fun c(v: String, f: String, t: String) =
    ConversionFormatter.format(CurrencyConversion.convert(bd(v), f, t, T))

@Test fun usdToEur() = assertEquals("90", c("100", "USD", "EUR"))
@Test fun usdToJpy() = assertEquals("15,000", c("100", "USD", "JPY"))
@Test fun eurToUsd() = assertEquals("100", c("90", "EUR", "USD"))
@Test fun eurToGbp() = assertEquals("88.8888888889", c("100", "EUR", "GBP"))   // 100*0.8/0.9
@Test fun usdToUsd() = assertEquals("100", c("100", "USD", "USD"))
```

---

## Task 7 — Converter MVI: state, intents, effects, PURE reducer (dual-input sync) [TDD]

**State** (`presentation/convert/ConverterState.kt`):

```kotlin
enum class ConverterField { A, B }

data class ConverterState(
    val category: UnitCategory = UnitCategory.Length,
    val unitA: UnitDef = UnitCategory.Length.defaultFrom,
    val unitB: UnitDef = UnitCategory.Length.defaultTo,
    val editing: ConverterField = ConverterField.A,
    val textA: String = "",          // the edited field holds raw user text
    val textB: String = "",          // the passive field is always derived
    val rates: ExchangeRates? = null,      // only for Currency
    val ratesSource: RatesSource? = null,  // shown as "live/offline" badge
)
fun ConverterState.activeText() = if (editing == ConverterField.A) textA else textB
```

**Intents** (`ConverterIntent.kt`):

```kotlin
sealed interface ConverterIntent {
    data class Digit(val d: Char) : ConverterIntent
    data object Decimal : ConverterIntent
    data object Delete : ConverterIntent
    data object Clear : ConverterIntent
    data object ToggleSign : ConverterIntent          // temperature allows negatives
    data class SelectField(val field: ConverterField) : ConverterIntent
    data class SelectCategory(val category: UnitCategory) : ConverterIntent
    data class SelectUnit(val field: ConverterField, val unit: UnitDef) : ConverterIntent
    data object SwapUnits : ConverterIntent
    data class RatesLoaded(val rates: ExchangeRates) : ConverterIntent   // from ViewModel async
}
```

**Effects** (`ConverterEffect.kt`): `Haptic`, `ErrorBlip`, `RequestRates(base: String)` — plus the shared `Reduction`-style pair:

```kotlin
sealed interface ConverterEffect {
    data object Haptic : ConverterEffect
    data class RequestRates(val base: String) : ConverterEffect
}
data class ConverterReduction(val state: ConverterState, val effects: List<ConverterEffect> = emptyList())
```

**Pure reducer** (`ConverterReducer.kt`) — the dual-input sync is the core. Editing a field updates that field's raw text and *derives* the other; changing units/category/swap re-derives the passive field from the active one.

```kotlin
class ConverterReducer {
    fun reduce(s: ConverterState, i: ConverterIntent): ConverterReduction = when (i) {
        is ConverterIntent.Digit    -> editActive(s, appendDigit(s.activeText(), i.d))
        ConverterIntent.Decimal     -> editActive(s, appendDecimal(s.activeText()))
        ConverterIntent.Delete      -> editActive(s, s.activeText().dropLast(1))
        ConverterIntent.Clear       -> ConverterReduction(s.copy(textA = "", textB = ""))
        ConverterIntent.ToggleSign  -> editActive(s, toggleSign(s.activeText()))
        is ConverterIntent.SelectField    -> ConverterReduction(s.copy(editing = i.field))
        is ConverterIntent.SelectCategory -> selectCategory(s, i.category)
        is ConverterIntent.SelectUnit     -> recompute(setUnit(s, i.field, i.unit))
        ConverterIntent.SwapUnits         -> recompute(swap(s))
        is ConverterIntent.RatesLoaded    -> recompute(s.copy(rates = i.rates, ratesSource = i.rates.source))
    }

    /** Write raw text into the edited field, derive the other field, keep 'editing' as-is. */
    private fun editActive(s: ConverterState, newText: String): ConverterReduction {
        val ns = if (s.editing == ConverterField.A) s.copy(textA = newText) else s.copy(textB = newText)
        return recompute(ns)
    }

    /** Single source of truth: derive the passive field from the active field's parsed value. */
    private fun recompute(s: ConverterState): ConverterReduction {
        val srcText = s.activeText()
        val parsed = srcText.toBigDecimalOrNull()          // handles "", "-", "1." etc. -> null
        val out = if (parsed == null) "" else {
            val (from, to) = if (s.editing == ConverterField.A) s.unitA to s.unitB else s.unitB to s.unitA
            val result = if (s.category == UnitCategory.Currency)
                s.rates?.let { CurrencyConversion.convert(parsed, from.id, to.id, it) }
            else ConversionEngine.convert(parsed, from, to)
            result?.let { ConversionFormatter.format(it) } ?: ""
        }
        val ns = if (s.editing == ConverterField.A) s.copy(textB = out) else s.copy(textA = out)
        val effects = if (s.category == UnitCategory.Currency && s.rates == null)
            listOf(ConverterEffect.RequestRates(from = "USD")) else emptyList()
        return ConverterReduction(ns, effects)
    }

    private fun selectCategory(s: ConverterState, cat: UnitCategory): ConverterReduction {
        val base = s.copy(category = cat, unitA = cat.defaultFrom, unitB = cat.defaultTo,
                          editing = ConverterField.A, textB = "")
        return if (cat == UnitCategory.Currency)
            ConverterReduction(base.copy(rates = null), listOf(ConverterEffect.RequestRates("USD")))
        else recompute(base)
    }
    // setUnit / swap / appendDigit / appendDecimal / toggleSign: small pure helpers,
    // append* mirror the guards already used in CalculatorReducer (one '.', leading "0.", etc.)
}
```

Key sync guarantee: **the edited field is never overwritten by conversion** (only the passive field is), so typing in A never clobbers the user's in-progress A text, and switching `editing` to B and typing recomputes A. This is the "edit A updates B, edit B updates A" requirement.

**Reducer tests** (`ConverterReducerTest`) — mirror the style of `CalculatorReducerTest.run(...)`:

```kotlin
class ConverterReducerTest {
    private val r = ConverterReducer()
    private fun run(s: ConverterState, vararg i: ConverterIntent) =
        i.fold(s) { acc, it -> r.reduce(acc, it).state }
    private fun length(a: String, b: String) = ConverterState(
        category = UnitCategory.Length,
        unitA = UnitCategory.Length.units.first { it.id == a },
        unitB = UnitCategory.Length.units.first { it.id == b })

    @Test fun editingAUpdatesB() {
        val s = run(length("km","m"), ConverterIntent.Digit('1'))
        assertEquals("1", s.textA); assertEquals("1,000", s.textB)
    }
    @Test fun editingBUpdatesA() {
        val s = run(length("km","m"), ConverterIntent.SelectField(ConverterField.B),
                    ConverterIntent.Digit('5'), ConverterIntent.Digit('0'), ConverterIntent.Digit('0'))
        assertEquals("500", s.textB); assertEquals("0.5", s.textA)   // 500 m = 0.5 km
    }
    @Test fun changingPassiveUnitReDerives() {
        val s = run(length("km","m"), ConverterIntent.Digit('1'),
                    ConverterIntent.SelectUnit(ConverterField.B,
                        UnitCategory.Length.units.first { it.id == "cm" }))
        assertEquals("100,000", s.textB)     // 1 km = 100,000 cm
    }
    @Test fun swapUnitsRecomputes() {
        val s = run(length("km","m"), ConverterIntent.Digit('2'), ConverterIntent.SwapUnits)
        assertEquals("km", s.unitB.id); assertEquals("m", s.unitA.id)
        assertEquals("2", s.textA); assertEquals("0.002", s.textB)   // now A=2 m -> 0.002 km
    }
    @Test fun emptyOrPartialInputClearsPassive() {
        val s = run(length("km","m"), ConverterIntent.Digit('1'), ConverterIntent.Delete)
        assertEquals("", s.textA); assertEquals("", s.textB)
    }
    @Test fun tempNegativeConverts() {
        val s = run(ConverterState(category = UnitCategory.Temperature,
            unitA = tempU("C"), unitB = tempU("F")),
            ConverterIntent.Digit('4'), ConverterIntent.Digit('0'), ConverterIntent.ToggleSign)
        assertEquals("-40", s.textA); assertEquals("-40", s.textB)
    }
    @Test fun selectingCurrencyRequestsRates() {
        val red = ConverterReducer().reduce(ConverterState(),
            ConverterIntent.SelectCategory(UnitCategory.Currency))
        assertTrue(red.effects.any { it is ConverterEffect.RequestRates })
    }
    @Test fun ratesLoadedFillsConversion() {
        val start = ConverterState(category = UnitCategory.Currency,
            unitA = usd(), unitB = eur())
        val typed = run(start, ConverterIntent.Digit('1'), ConverterIntent.Digit('0'),
                        ConverterIntent.Digit('0'))
        assertEquals("", typed.textB)                          // no rates yet
        val loaded = r.reduce(typed, ConverterIntent.RatesLoaded(fakeUsdTable())).state
        assertEquals("90", loaded.textB)                       // 100 USD -> 90 EUR
    }
}
```

---

## Task 8 — ConverterViewModel (async rate loading) [TDD]

Mirrors `CalculatorViewModel`: pure reducer inside, `StateFlow` out, `SharedFlow<ConverterEffect>`, and it services `RequestRates` effects by calling the injected repository off the main thread, dispatching `RatesLoaded` back into the reducer.

```kotlin
class ConverterViewModel(
    private val rateRepo: ExchangeRateRepository,
    private val reducer: ConverterReducer = ConverterReducer(),
) : ViewModel() {
    private val _state = MutableStateFlow(ConverterState())
    val state: StateFlow<ConverterState> = _state.asStateFlow()
    private val _effects = MutableSharedFlow<ConverterEffect>(extraBufferCapacity = 16)
    val effects: SharedFlow<ConverterEffect> = _effects.asSharedFlow()

    fun dispatch(intent: ConverterIntent) {
        val red = reducer.reduce(_state.value, intent)
        _state.value = red.state
        for (e in red.effects) when (e) {
            is ConverterEffect.RequestRates -> viewModelScope.launch {
                val rates = rateRepo.getRates(e.base)                     // never throws
                dispatch(ConverterIntent.RatesLoaded(rates))
            }
            else -> _effects.tryEmit(e)
        }
    }
}
```

**Test** (`ConverterViewModelTest`, `runTest` + injected fakes):

```kotlin
@Test fun selectingCurrencyLoadsRatesAndConverts() = runTest {
    val repo = DefaultExchangeRateRepository(
        FakeExchangeRateProvider(result = fakeUsdTable()), FakeRatesCache())
    val vm = ConverterViewModel(repo)
    vm.dispatch(ConverterIntent.SelectCategory(UnitCategory.Currency))
    vm.dispatch(ConverterIntent.SelectUnit(ConverterField.B, eur()))
    vm.dispatch(ConverterIntent.Digit('1')); vm.dispatch(ConverterIntent.Digit('0'))
    vm.dispatch(ConverterIntent.Digit('0'))
    advanceUntilIdle()
    assertEquals("90", vm.state.value.textB)
}
@Test fun offlineStillConvertsViaBundled() = runTest {
    val repo = DefaultExchangeRateRepository(
        FakeExchangeRateProvider(throwing = true), FakeRatesCache())
    val vm = ConverterViewModel(repo)
    vm.dispatch(ConverterIntent.SelectCategory(UnitCategory.Currency))
    advanceUntilIdle()
    assertEquals(RatesSource.BUNDLED, vm.state.value.ratesSource)
}
```

---

## Task 9 — Converter UI (`ui/convert/`) + mode host [TDD-light, Compose]

- **`CategorySelector`** — a horizontally scrollable `FilterChip` row over `UnitCategory.entries`; `onSelect { vm.dispatch(SelectCategory(it)) }`.
- **`UnitPickerRow`** — two rows, each an `ExposedDropdownMenuBox` (Material3) listing `category.units`; a center swap `IconButton` dispatches `SwapUnits`.
- **Two synced input fields** — each a tappable `Surface` showing `symbol` + value text; tapping sets `editing` via `SelectField`. The active field gets a highlighted border; the passive field shows the derived value. For Currency, a small badge shows `ratesSource` ("Live"/"Offline"). Fields are driven by an on-screen **numeric keypad** (Task 10), so they are read-only surfaces, not `TextField`s — this keeps input identical to Standard mode and avoids soft-keyboard divergence.
- **`ConverterScreen(vm)`** — collects `vm.state` with `collectAsStateWithLifecycle()`, lays out selector → picker row → two fields → keypad, reusing the existing `Surface`/`Column` structure from `CalculatorScreen`.
- **`CalculatorHost`** — replaces the direct `CalculatorScreen` call in `App.kt`. Holds the shared `CalcMode` toggle (a two-chip segmented control at the top). Switches between `CalculatorScreen(calcVm)` and `ConverterScreen(convVm)`. `App(repository, rateRepo)` constructs both view models via `viewModel { … }`.

`App.kt` / `MainActivity` change: thread an `ExchangeRateRepository` alongside the existing `HistoryRepository`. For the Android verification build, wire `DefaultExchangeRateRepository(<injected provider>, <cache>)`; since the live provider is out of scope, a `BundledOnlyProvider` (always throws → bundled path) or the future network client can be dropped in without touching the ViewModel.

---

## Task 10 — Data-driven converter keypad (`ConverterKeypad.kt`)

Reuse the existing `CalculatorKeypad(pad, onKey)` renderer verbatim — it already takes a generic `KeyPad`. Define a converter pad whose `Key.intent` values are a thin bridge to `ConverterIntent` (either add a small `onKey: (ConverterIntent) -> Unit` wrapper, or, to reuse `CalculatorKeypad` unchanged, map keys through a local `when`). Layout (no operators, adds a sign key for temperature):

```
7 8 9 ⌫
4 5 6 C
1 2 3 ±
  0 .
```

`ToggleSign` is only meaningful for Temperature; disable/hide it for other categories (pass `enabled` per key or filter the pad by `state.category`).

---

## Task 11 — Verification (matches Standard slice §8)

1. **Unit tests green** (`commonTest`, JVM): all Task 4–8 suites. Command: `./gradlew :composeApp:testDebugUnitTest` (or the KMP JVM test task used in the repo).
2. **Android build green:** `./gradlew :composeApp:assembleDebug`.
3. **iOS compile/link green:** `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`.
4. **On-screen (Android):** launch, toggle to Converter, pick Length, type in A → B updates live; tap B, type → A updates; swap units; switch to Temperature and verify `100°C → 212°F`; switch to Currency and confirm it converts using bundled rates when offline (airplane mode) and shows the "Offline" badge. Capture light+dark screenshots.

---

## Notes, risks, and precision decisions

- **No `Double` anywhere** — all factors, affine constants, and rate arithmetic are `BigDecimal`; divisions use an explicit `DecimalMode` (12 significant digits for display, 30–50 for intermediate affine steps), then `roundSignificand` + `NumberFormatter` for output. This satisfies the "high precision" requirement.
- **Exact factor definitions** — base units chosen (metre/kg/m²/L/**km-per-hour**/Celsius) so every catalogued factor terminates; the only rounding happens on the final display divide, which is correct behaviour for repeating results (e.g. `100 km/h = 27.7778 m/s`).
- **Affine correctness** — Fahrenheit/Kelvin use integer-rational affine forms (`C=(F·5−160)/9`, `C=(K·100−27315)/100`) so the canonical checkpoints (`0°C=32°F=273.15K`, `−40` crossover) are bit-exact, not tolerance-based.
- **Currency network is injected** — `ExchangeRateProvider` is a pure port; the repository's live→stale-cache→bundled fallback is fully unit-tested with fakes and never throws to the UI. When the separate research agent supplies a networking approach, it implements `ExchangeRateProvider` only; nothing else changes.
- **Standard mode untouched** — `MathEngine`, `CalculatorReducer`, `CalculatorState`, and their existing tests are not modified; the only edit to shared code is adding `UnitConverter` to `CalcMode` and introducing `CalculatorHost` in place of the direct `CalculatorScreen` call in `App.kt`.

Relevant existing files this plan integrates with (all absolute):
`/Users/jonahskinner/Github/ForFun/calc/composeApp/src/commonMain/kotlin/com/example/calc/domain/NumberFormatter.kt`,
`/Users/jonahskinner/Github/ForFun/calc/composeApp/src/commonMain/kotlin/com/example/calc/presentation/CalculatorState.kt` (add `UnitConverter` to `CalcMode`),
`/Users/jonahskinner/Github/ForFun/calc/composeApp/src/commonMain/kotlin/com/example/calc/ui/keypad/CalculatorKeypad.kt` (reused renderer),
`/Users/jonahskinner/Github/ForFun/calc/composeApp/src/commonMain/kotlin/com/example/calc/App.kt` and `/Users/jonahskinner/Github/ForFun/calc/composeApp/src/androidMain/kotlin/com/example/calc/MainActivity.kt` (thread in `ExchangeRateRepository`),
`/Users/jonahskinner/Github/ForFun/calc/composeApp/src/commonTest/kotlin/com/example/calc/support/FakeHistoryRepository.kt` (fake pattern to mirror).