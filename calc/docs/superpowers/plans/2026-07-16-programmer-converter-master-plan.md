I now have an exact picture of the codebase. Here is the reconciled master plan.

---

# Master Implementation Plan — Programmer + Unit Converter + Mode Switcher
**KMP + Compose Multiplatform Calculator · single ordered TDD plan**

All paths below are under
`/Users/jonahskinner/Github/ForFun/calc/composeApp/src/commonMain/kotlin/com/example/calc/` (prod) and
`/Users/jonahskinner/Github/ForFun/calc/composeApp/src/commonTest/kotlin/com/example/calc/` (tests), unless a platform source set is named.

---

## Part A — Reconciliation decisions (conflicts resolved before any code)

The four docs disagree on four load-bearing points. I picked ONE approach for each; the plan below is internally consistent with these choices. Deviations from each source doc are called out so the executor is not confused when a doc says otherwise.

1. **ViewModel structure → ONE host `AppViewModel` (Host doc wins).**
   - The Programmer doc folds programmer logic into `CalculatorState`/`CalculatorReducer` via a `CalculatorIntent.Programmer` wrapper and leaves the ViewModel unchanged. **Rejected.**
   - The Converter doc introduces a separate `ConverterViewModel`. **Rejected.**
   - **Chosen:** rename `CalculatorViewModel` → `AppViewModel`, holding `AppState` (mode + three independent sub-states) and routing to three *pure* reducers via typed entry points `onStandard / onProgrammer / onConverter / selectMode`. This is the Host doc's design and it explicitly reconciles the other two.

2. **Mode ownership → moved out of `CalculatorState` (Host doc wins).**
   - Today `CalcMode` and the `mode` field live inside `CalculatorState`, plus a dead `CalculatorIntent.SwitchMode` + reducer branch. Move `CalcMode` to `presentation/CalcMode.kt` (3 entries), delete the `mode` field, delete `SwitchMode` and its reducer branch, put `mode` on `AppState`. The Programmer doc's "add `CalcMode.Programmer` + `programmer` field to `CalculatorState`" is **superseded**.

3. **Generic keypad → `Key<I>` / `KeyPad<I>` (Host doc wins).**
   - Programmer/Converter docs keep `Key` bound to `CalculatorIntent` and add `enabled`. **Chosen instead:** make `Key<I>` generic + add `enabled`, so each mode dispatches its own intent type with compile-time safety. `standardKeyPad: KeyPad<CalculatorIntent>`, `programmerKeyPad(...): KeyPad<ProgrammerIntent>`, `converterKeyPad: KeyPad<ConverterIntent>`.

4. **Domain models → take the *richer* of each doc.**
   - **Programmer domain:** use the Programmer doc's `Bits`/`RadixFormatter`/`Radix`/`WordSize` (masked-`Long`, two's-complement, immediate-execution accumulator) verbatim — it is far more complete and TDD-specified than the Host doc's `ProgrammerEngine`. But wire it through the Host architecture (a pure `ProgrammerReducer` routed by `AppViewModel.onProgrammer`, **not** a `CalculatorIntent` wrapper). Rename the Programmer doc's `ProgIntent` → top-level `ProgrammerIntent`; keep its `ProgBinOp`/`ProgUnaryOp` op sets.
   - **Converter domain:** use the Converter doc's sealed `UnitConversion` (`Ratio`/`Affine`), `UnitDef`, 7-category `UnitCategory`, `ConversionEngine`, and full currency layer (repository/provider/cache/bundled) — the Host doc's lambda-based `ConvUnit` and 3-category, no-currency model is **superseded**. Use the Converter doc's dual-input `ConverterState` (edit A→B, edit B→A). Route through `AppViewModel.onConverter` (not a separate VM); the VM services the `RequestRates` effect.

5. **Effects channel:** keep the single existing `SharedFlow<CalculatorEffect>` (`ErrorBlip` is the only surfaced effect that matters — haptics already fire inside `KeyButton`). `onProgrammer` emits `ErrorBlip` on an error *transition*. Converter's `RequestRates` is handled internally by the VM and never surfaced.

6. **Bug fixes carried in from the docs (applied in this plan):**
   - Converter `recompute` in the source references `from` outside its scope for the `RequestRates` effect — fixed to a fixed base `"USD"`.
   - `BundledRates.rebase(...)` is undefined — dropped; the app always requests base `USD`, so the bundled table is returned as-is.
   - Repository TTL needs a clock; added an `expect fun epochSeconds(): Long` (mirrors the `DatabaseDriverFactory` expect/actual pattern) rather than the doc's hand-waved `now`.

7. **History stays Standard-only** (both the Host and both feature docs agree). The history button renders only in Standard mode.

---

## Part B — Consolidated dependency / Gradle / manifest changes (do once, in Phase 5 — NOT before)

Programmer mode and the entire Converter domain/reducer/VM are testable with **zero** new dependencies. Only the *live currency provider* needs Ktor. Add these only when you reach Phase 5 so earlier phases stay dependency-clean.

**`gradle/libs.versions.toml`** — add to `[versions]`:
```toml
ktor = "3.3.3"                 # last Ktor built on Kotlin 2.2.20 (3.4.0+ = Kotlin 2.3 → metadata mismatch)
serialization = "1.9.0"        # last serialization on Kotlin 2.2 (1.10/1.11 = Kotlin 2.3)
```
add to `[libraries]`:
```toml
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
```
add to `[plugins]`:
```toml
kotlinSerialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }  # 2.2.20, lockstep with compiler
```

**`composeApp/build.gradle.kts`** — add plugin alias and source-set deps:
```kotlin
plugins {
    // ...existing...
    alias(libs.plugins.kotlinSerialization)
}
// in kotlin { sourceSets { ... } }:
commonMain.dependencies {
    // ...existing...
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
}
androidMain.dependencies {
    // ...existing...
    implementation(libs.ktor.client.okhttp)   // OkHttp: HTTP/2, pooling, modern TLS; fine on minSdk 24
}
iosMain.dependencies {
    // ...existing...
    implementation(libs.ktor.client.darwin)    // wraps NSURLSession, no pods
}
```

**`composeApp/src/androidMain/AndroidManifest.xml`** — add as a direct child of `<manifest>` (endpoint is HTTPS, so **no** `usesCleartextTraffic`, no network-security-config needed on API 28+):
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

**Endpoint (no key, HTTPS):** `https://open.er-api.com/v6/latest/USD`. Daily refresh; cache and fetch ≤ hourly (429 over-limit). Attribution required: add a discreet "Rates by ExchangeRate-API" link/text in the Converter currency UI.

---

## Part C — Ordered task plan

Every task is **(a)** write failing tests → **(b)** implement → **(c)** run the verification command green → **(d)** commit. Domain/presentation logic is `commonTest` on JVM.

Standard test command: `./gradlew :composeApp:testDebugUnitTest`
Android build gate: `./gradlew :composeApp:assembleDebug`
iOS link gate: `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`

---

### PHASE 0 — Shared host + mode switcher (non-breaking; Standard must stay identical)

#### Task 0.1 — Lift & expand `CalcMode`; strip mode scaffolding from Standard
**Create** `presentation/CalcMode.kt`:
```kotlin
package com.example.calc.presentation
enum class CalcMode(val label: String) { Standard("Std"), Programmer("Prog"), Converter("Conv") }
```
**Modify** `presentation/CalculatorState.kt`: delete `enum class CalcMode { Standard }` and the `val mode` field.
**Modify** `presentation/CalculatorIntent.kt`: delete `data class SwitchMode(...)`.
**Modify** `presentation/CalculatorReducer.kt`: delete the `is CalculatorIntent.SwitchMode -> ...` branch.
**Tests:** existing `CalculatorReducerTest`/`CalculatorViewModelTest` never referenced `mode`, so they still pass — that IS the regression check.
**Verify:** `./gradlew :composeApp:testDebugUnitTest` (must stay green after deletions).

#### Task 0.2 — Generify the keypad (mechanical, non-breaking)
**Modify** `ui/keypad/Key.kt`:
```kotlin
package com.example.calc.ui.keypad
enum class KeyStyle { Number, Operator, Function, Accent, Equals, Toggle, ToggleActive }
data class Key<I>(val label: String, val style: KeyStyle, val intent: I, val span: Int = 1, val enabled: Boolean = true)
typealias KeyPad<I> = List<List<Key<I>>>
```
(`Toggle`/`ToggleActive` added now for Programmer radix/word-size tabs.)
**Modify** `ui/keypad/CalculatorKeypad.kt`: make it generic and honor `enabled`:
```kotlin
@Composable fun <I> CalculatorKeypad(pad: KeyPad<I>, onKey: (I) -> Unit, modifier: Modifier = Modifier) {
    val haptics = LocalHapticFeedback.current
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in pad) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (key in row) KeyButton(key, Modifier.weight(key.span.toFloat())) {
                if (!key.enabled) return@KeyButton
                haptics.performHapticFeedback(HapticFeedbackType.LongPress); onKey(key.intent)
            }
        }
    }
}
```
In `KeyButton`: add `bg`/`fg` cases for `Toggle` (→ `surfaceVariant`/`onSurfaceVariant`) and `ToggleActive` (→ `primary`/`onPrimary`); when `!key.enabled` render with `bg.copy(alpha=.35f)`, `fg.copy(alpha=.35f)` and drop the `.clickable`. Make `aspectRatio` configurable (add a `keyAspect: Float = 1f` param to `CalculatorKeypad`, default preserves Standard) so Programmer's 11 dense rows fit.
**Modify** `ui/keypad/StandardKeyPad.kt`: change type to `val standardKeyPad: KeyPad<CalculatorIntent>` — the `digit()` helper and layout are otherwise untouched.
**Verify:** `./gradlew :composeApp:assembleDebug` (compile), then existing tests green.

#### Task 0.3 — `AppState` + `AppViewModel` (rename from `CalculatorViewModel`)
**Create** `presentation/AppState.kt`:
```kotlin
package com.example.calc.presentation
data class AppState(
    val mode: CalcMode = CalcMode.Standard,
    val standard: CalculatorState = CalculatorState(),
    val programmer: ProgrammerState = ProgrammerState(),   // added in Phase 1
    val converter: ConverterState = ConverterState(),      // added in Phase 3
)
```
> Ordering note: `ProgrammerState`/`ConverterState` don't exist yet. To keep Phase 0 compiling standalone, first introduce `AppState` with only `mode` + `standard`, and add the other two fields in Tasks 2.x / 4.x. (Or stub empty `ProgrammerState()`/`ConverterState()` now.) The plan assumes you add fields as their types land.

**Rename** `presentation/CalculatorViewModel.kt` → `presentation/AppViewModel.kt`:
```kotlin
class AppViewModel(
    private val repository: HistoryRepository,
    private val rateRepo: ExchangeRateRepository? = null,   // null until Phase 5 wired; converter falls to bundled
    engine: MathEngine = MathEngine(),
) : ViewModel() {
    private val standardReducer = CalculatorReducer(engine)
    private val programmerReducer = ProgrammerReducer()     // Phase 1
    private val converterReducer = ConverterReducer()        // Phase 3

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()
    private val _effects = MutableSharedFlow<CalculatorEffect>(extraBufferCapacity = 16)
    val effects: SharedFlow<CalculatorEffect> = _effects.asSharedFlow()

    init { viewModelScope.launch {
        repository.observeHistory().collect { items ->
            _state.update { it.copy(standard = it.standard.copy(history = items)) }
        }
    } }

    fun selectMode(mode: CalcMode) = _state.update { it.copy(mode = mode) }

    fun onStandard(intent: CalculatorIntent) {
        val r = standardReducer.reduce(_state.value.standard, intent)
        _state.update { it.copy(standard = r.state) }
        for (e in r.effects) when (e) {
            is CalculatorEffect.PersistHistory -> viewModelScope.launch { repository.add(e.expression, e.result) }
            else -> _effects.tryEmit(e)
        }
    }
    fun clearHistory() = viewModelScope.launch { repository.clear() }
    // onProgrammer added in Task 2.x; onConverter added in Task 4.x
}
```
**Modify tests:** port `CalculatorViewModelTest` → `AppViewModelTest`, replacing `dispatch(...)`→`onStandard(...)` and `vm.state.value.input`→`vm.state.value.standard.input`; both existing assertions (`"2+3"`/`"5"` preview; history persistence) must still pass. Add:
- `selectMode preserves sub-state`: type in Standard, `selectMode(Programmer)`, assert `standard.input` unchanged.
**Verify:** `./gradlew :composeApp:testDebugUnitTest`.

#### Task 0.4 — `ModeSwitcher`, `CalculatorApp` host scaffold, `StandardBody`; rewire `App`
**Create** `ui/ModeSwitcher.kt` — hand-rolled segmented control over `CalcMode.entries` (dependency-free; avoids experimental Material3 `SegmentedButton`), exactly as the Host doc specifies (active chip = `primary`/`onPrimary`, others transparent/`onSurfaceVariant`).
**Create** `ui/StandardBody.kt` — the inner display+keypad lifted verbatim from today's `CalculatorScreen`:
```kotlin
@Composable fun StandardBody(state: CalculatorState, dispatch: (CalculatorIntent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.Bottom) {
        CalculatorDisplay(state, onSwipeDown = { dispatch(CalculatorIntent.ShowHistory) }, Modifier.weight(1f))
        CalculatorKeypad(pad = standardKeyPad, onKey = dispatch)
    }
}
```
**Create** `ui/CalculatorApp.kt` — owns the outer `Surface/Box/Column`, the top row (`ModeSwitcher` + history `IconButton` shown only when `mode == Standard`), the `when(mode)` body switch, and the `HistorySheet` (guarded by `mode == Standard && standard.historyVisible`). Bodies for Programmer/Converter are placeholders until Phases 2/6 (`Text("Programmer")` etc. so it compiles).
**Delete** `ui/CalculatorScreen.kt` (its content is now split across `CalculatorApp` + `StandardBody`).
**Modify** `App.kt`:
```kotlin
@Composable fun App(repository: HistoryRepository, rateRepo: ExchangeRateRepository? = null) {
    AppTheme { val vm = viewModel { AppViewModel(repository, rateRepo) }; CalculatorApp(vm) }
}
```
`MainActivity`/`MainViewController` still call `App(repository)` (rateRepo defaults null → bundled currency until Phase 5).
**Verify:** `./gradlew :composeApp:assembleDebug`; launch on Android, confirm Standard mode looks/behaves identically (display, keypad, history sheet, swipe-down), and the mode switcher renders. **On-device check required** (visual parity).

---

### PHASE 1 — Programmer domain (pure, TDD, no deps)

All in `domain/` (Programmer doc §2, adopted verbatim). Reuse `CalcError.DivByZero` — but `Bits` throws a dedicated `ProgArithmeticException` (also new) and the reducer maps it to a `ProgError`.

#### Task 1.1 — `WordSize` + `Radix` (+ `allowsDigit`)
**Create** `domain/WordSize.kt`, `domain/Radix.kt` (Programmer doc §2.1–2.2).
**Test** `domain/RadixTest.kt`: `BIN.allowsDigit(2)==false`, `OCT.allowsDigit(8)==false`, `DEC.allowsDigit(10)==false`, `HEX.allowsDigit(15)==true`/`allowsDigit(16)==false`, `WordSize.DWORD.bits==32`.

#### Task 1.2–1.6 — `Bits.kt` (masked two's-complement primitives) — build incrementally
**Create** `domain/Bits.kt` and `class ProgArithmeticException` (Programmer doc §2.3), one test file per group:
- `BitsMaskTest` — `mask`/`signed`: `mask(0x1FF,BYTE)==0xFF`, `mask(-1,QWORD)==-1`, `signed(0xFF,BYTE)==-1`, `signed(0x80,BYTE)==-128`, `signed(0x8000_0000,DWORD)==-2147483648`.
- `BitsBitwiseTest` — and/or/xor/not/nand/nor/xnor/negate incl. `negate(0x80,BYTE)==0x80` (min-value overflow), `not(0x00,QWORD)==-1`.
- `BitsShiftTest` — shl/shrLogical/shrArithmetic incl. edge counts `n==bits→0`, `shrLogical(-1,1,QWORD)==Long.MAX_VALUE`, `shrArithmetic(0xF0,4,BYTE)==0xFF` vs `shrLogical(0xF0,4,BYTE)==0x0F`.
- `BitsArithTest` — add/sub/mul/div/rem with wrap; `add(0x7F,1,BYTE)==0x80`; signed truncating div `div(0xF8,3,BYTE)==0xFE`; `assertFailsWith<ProgArithmeticException>{ div(5,0,BYTE) }`.
- `BitsEntryTest` — appendDigit/dropDigit; fold 16 F's in HEX/QWORD → `-1L` (no overflow crash); `dropDigit(-1,HEX,QWORD)==0x0FFFFFFFFFFFFFFF` (ULong divide).

(Full input→expected vectors are enumerated in the Programmer doc §6 P2–P6 — implement each exactly.)

#### Task 1.7 — `RadixFormatter.kt`
**Create** `domain/RadixFormatter.kt` (Programmer doc §2.4). **Test** `RadixFormatterTest`: `format(0xFF,HEX,BYTE)=="FF"`, `format(0xFF,DEC,BYTE)=="-1"`, `format(0xFF,OCT,BYTE)=="377"`, `format(0xFF,BIN,BYTE)=="1111 1111"`, `format(0x7B,BIN,BYTE)=="111 1011"` (nibble group, no pad), `format(-1,HEX,QWORD)=="FFFFFFFFFFFFFFFF"`, `all(0x0A,DWORD)=={HEX="A",DEC="10",OCT="12",BIN="1010"}`.
**Verify (whole phase):** `./gradlew :composeApp:testDebugUnitTest`.

---

### PHASE 2 — Programmer presentation + UI

#### Task 2.1 — State + intents + pure reducer (TDD)
**Create** `presentation/ProgrammerState.kt` (Programmer doc §3.1: `wordSize`, `radix`, `entry`, `acc`, `pending`, `freshEntry`, `error`; `enum ProgError { DivByZero }`).
**Create** `presentation/ProgrammerIntent.kt` — the Programmer doc's `ProgIntent` **renamed to top-level `ProgrammerIntent`** (no `CalculatorIntent.Programmer` wrapper), plus `enum ProgBinOp { And,Or,Xor,Nand,Nor,Xnor,Shl,ShrLogical,ShrArith,Add,Sub,Mul,Div,Mod }` and `enum ProgUnaryOp { Not, Negate }`.
**Create** `presentation/ProgrammerReducer.kt` — pure `reduce(state, intent): ProgrammerState` (Programmer doc §3.3), unchanged logic (digits, radix guard, binary fold/chain, equals, unary, delete, clear, SetRadix, SetWordSize re-mask). Div-by-zero sets `error = ProgError.DivByZero`.
**Add** `fun ProgrammerState.displayValue(): String` (Programmer doc §3.4) using `RadixFormatter`.
**Tests** (three files, Programmer doc §6 P9–P11):
- `ProgrammerReducerEntryTest` — DEC 5,3→"53"; BIN digit 2→no-op; fresh entry after op; delete FF→0x0F; clear preserves radix/wordSize.
- `ProgrammerReducerOpsTest` — `0xF0 AND 0x0F =`→0; `5 + 3 =`→8; chaining `0xF0 AND 0x0F OR 0xFF =`→0xFF; NOT/NEG immediate; `0x01 << 4 =`→0x10; `0xFF >> 1`(logical)→0x7F vs `>>>`→0xFF; repeated `2+3=` then `+10=`→15.
- `ProgrammerReducerModeTest` — WORD 0x1FF→BYTE→0xFF; BYTE 0xFF→WORD keeps 255 & DEC flips "-1"→"255"; SetRadix keeps bits; `5 ÷ 0 =`→`error==DivByZero`; digit after error clears it.

#### Task 2.2 — Route Programmer through `AppViewModel` (+ ErrorBlip)
**Modify** `AppViewModel`: add
```kotlin
fun onProgrammer(intent: ProgrammerIntent) {
    val prev = _state.value.programmer
    val next = programmerReducer.reduce(prev, intent)
    _state.update { it.copy(programmer = next) }
    if (next.error != null && prev.error == null) _effects.tryEmit(CalculatorEffect.ErrorBlip)
}
```
Add `programmer: ProgrammerState` field to `AppState` (if not already).
**Test** `AppViewModelTest` additions: `onProgrammer(Digit(7))` sets `programmer.entry==7` and leaves `standard.input` untouched; `5 ÷ 0 =` emits `ErrorBlip`.

#### Task 2.3 — Programmer keypad (radix-aware enablement, TDD)
**Create** `ui/keypad/ProgrammerKeyPad.kt` — `fun programmerKeyPad(state: ProgrammerState): KeyPad<ProgrammerIntent>` (Programmer doc §4.1, but keys carry `ProgrammerIntent` directly, no wrapper): radix tabs + word-size tabs (`ToggleActive` for the selected), bitwise/shift/arith ops, digits 0–F with `enabled = state.radix.allowsDigit(v)`.
**Test** `ProgrammerKeyPadTest`: in BIN, "A".."F" and "2" disabled, "0"/"1" enabled; in OCT "8"/"9" disabled; in HEX A–F enabled; selected radix/word-size tab has `KeyStyle.ToggleActive`.

#### Task 2.4 — Programmer display + shared `ScrollingLine` + body (visual)
**Refactor:** extract the private `ScrollingLine` from `ui/CalculatorDisplay.kt` into `ui/ScrollingLine.kt` (public), update `CalculatorDisplay` to use it (no behaviour change).
**Create** `ui/ProgrammerDisplay.kt` — four-row HEX/DEC/OCT/BIN panel (active radix highlighted, `wordSize · two's complement` caption, error line), each row using shared `ScrollingLine` (Programmer doc §4.2).
**Create** `ui/programmer/ProgrammerBody.kt` — `ProgrammerDisplay(state.programmer, Modifier.weight(1f))` above `CalculatorKeypad(programmerKeyPad(state.programmer), onKey = vm::onProgrammer, keyAspect = <denser>)`.
**Modify** `ui/CalculatorApp.kt` — replace the Programmer placeholder with `ProgrammerBody(...)`.
**Verify:** `assembleDebug`; **on-device**: HEX↔BIN↔DEC digit enable/disable, AND/XOR, `<<` shift, WORD→BYTE truncation of `0x1FF`; light+dark screenshots.

---

### PHASE 3 — Unit Converter domain (pure, TDD, no deps)

All under `domain/convert/` (Converter doc Tasks 1–5), with the currency layer built against injected ports and a bundled fallback so it needs no network.

#### Task 3.1 — `UnitConversion` (Ratio/Affine) [TDD]
**Create** `domain/convert/UnitConversion.kt` — sealed `UnitConversion { toBase(value); fromBase(base, mode) }`, `Ratio(factor)`, `Affine(mulNum, addNum, den)` (Converter doc Task 1).
**Test** `UnitConversionTest`: `Ratio(1000).toBase(2)=="2000"`, round-trip `fromBase=="2"`; `Affine(5,-160,9).toBase(32)=="0"`, `fromBase(0)=="32"`.

#### Task 3.2 — `UnitDef` + `UnitCategory` [TDD]
**Create** `domain/convert/UnitDef.kt`, `domain/convert/UnitCategory.kt` — 7 categories with exact-terminating factors (bases: metre/kg/m²/L/**km-h**/Celsius; Currency empty, runtime-filled). `defaultFrom`/`defaultTo`, `linearCategories` (Converter doc Task 2).
**Test** `UnitCategoryTest`: every linear unit's conversion is `Ratio`/`Affine`; `defaultFrom != defaultTo` for multi-unit categories; metre factor == "1".

#### Task 3.3 — `ConversionEngine` + `ConversionFormatter` [TDD]
**Create** `domain/convert/ConversionEngine.kt` (`convert = to.fromBase(from.toBase(value), DISPLAY_MODE).roundSignificand(DISPLAY_MODE)`, `DecimalMode(12, ROUND_HALF_AWAY_FROM_ZERO)`) and `domain/convert/ConversionFormatter.kt` (`format = NumberFormatter.format(value)` — reuses existing grouping/trim/scientific).

#### Task 3.4 — Linear category tests [TDD]
**Create** `ConversionEngineLengthTest`, `WeightTest`, `AreaTest`, `VolumeTest`, `SpeedTest` (Converter doc Task 4). Representative: `1 mi→m = "1,609.344"`, `1 lb→g = "453.59237"`, `1 acre→m² = "4,046.8564224"`, `1 gal→L = "3.785411784"`, `60 mph→km/h = "96.56064"`. All expected strings carry grouping commas (route through `NumberFormatter`).

#### Task 3.5 — Temperature affine tests [TDD]
**Create** `ConversionEngineTemperatureTest` (Converter doc Task 5): `0°C→F="32"`, `100→212`, `0°C→K="273.15"`, `32°F→C="0"`, `212→100`, `273.15K→C="0"`, `-40°C→F="-40"`, `37°C→F="98.6"`.

#### Task 3.6 — Currency model, ports, repository, bundled fallback [TDD]
**Create** under `domain/convert/currency/`: `ExchangeRates` (+`RatesSource{LIVE,CACHED,BUNDLED}`), `ExchangeRateProvider` (suspend `fetchLatest(base)`), `RatesCache`, `ExchangeRateRepository`, `BundledRates` (USD table only — **no `rebase`**, app always requests USD), `DefaultExchangeRateRepository` (fresh-cache → live(+save) → stale-cache → bundled; uses injected `now`), `CurrencyConversion` (`amount*rate(to)/rate(from)`, `DecimalMode(12,…)`).
**Create fakes** `commonTest/support/FakeExchangeRateProvider.kt`, `FakeRatesCache.kt`.
**Tests:**
- `CurrencyConversionTest` (fixed table USD=1,EUR=0.9,GBP=0.8,JPY=150): `100 USD→EUR="90"`, `→JPY="15,000"`, `90 EUR→USD="100"`, `100 EUR→GBP="88.8888888889"`.
- `DefaultExchangeRateRepositoryTest` (`runTest`): live+cache; provider-throws+no-cache→BUNDLED; stale-cache beats bundled→CACHED; fresh cache skips provider. (Converter doc Task 6 assertions verbatim.)
**Verify (phase):** `./gradlew :composeApp:testDebugUnitTest`.

---

### PHASE 4 — Converter presentation + VM routing (pure reducer + async effect)

#### Task 4.1 — State, intents, effects, dual-input reducer [TDD]
**Create** `presentation/convert/ConverterState.kt` (dual `textA`/`textB`, `editing`, `unitA`/`unitB`, `category`, `rates`, `ratesSource`; `activeText()`), `ConverterIntent.kt`, `ConverterEffect.kt` (`Haptic`, `RequestRates(base)`; `data class ConverterReduction(state, effects)`), `ConverterReducer.kt` (Converter doc Task 7).
**Apply fixes:** in `recompute`, the `RequestRates` effect uses a fixed `base = "USD"` (not the out-of-scope `from`); add a small `String.toBigDecimalOrNull()` guard helper that rejects `""`, `"-"`, `"1."` etc.
Key invariant: **only the passive field is overwritten by conversion; the edited field keeps raw user text.**
**Test** `ConverterReducerTest` (Converter doc Task 7): edit A→B (`1 km`→`"1,000"` m); edit B→A (`500 m`→`"0.5"` km); changing passive unit re-derives (`1 km→cm = "100,000"`); swap recomputes; partial input clears passive; temp `-40` via `ToggleSign`; selecting Currency emits `RequestRates`; `RatesLoaded` fills conversion (`100 USD→EUR="90"`).

#### Task 4.2 — Route Converter through `AppViewModel` (service RequestRates)
**Modify** `AppViewModel`: add `converter` to `AppState`, and:
```kotlin
fun onConverter(intent: ConverterIntent) {
    val r = converterReducer.reduce(_state.value.converter, intent)
    _state.update { it.copy(converter = r.state) }
    for (e in r.effects) when (e) {
        is ConverterEffect.RequestRates -> viewModelScope.launch {
            val rates = (rateRepo ?: DefaultExchangeRateRepository(BundledOnlyProvider, InMemoryRatesCache()))
                .getRates(e.base)                       // never throws
            onConverter(ConverterIntent.RatesLoaded(rates))
        }
        else -> {} // Haptic handled by keypad
    }
}
```
Provide `BundledOnlyProvider` (a provider that always throws → forces bundled) and `InMemoryRatesCache` in `commonMain` so the app converts currency offline before Phase 5 wires Ktor.
**Test** `AppViewModelTest` additions (`runTest` + `FakeExchangeRateProvider`): selecting Currency + typing loads rates and converts (`100 USD→EUR="90"` after `advanceUntilIdle()`); offline path yields `ratesSource==BUNDLED`.
**Verify:** `./gradlew :composeApp:testDebugUnitTest`.

---

### PHASE 5 — Currency networking (add deps here; see Part B)

#### Task 5.1 — Apply the Part B Gradle/manifest changes
Add Ktor + serialization deps, the serialization plugin, and the INTERNET permission. **Verify:** `./gradlew :composeApp:assembleDebug` still green (deps resolve, plugin applies).

#### Task 5.2 — expect/actual HTTP engine + clock (mirror `DatabaseDriverFactory`)
**Create** `data/net/HttpClientFactory.kt` (commonMain) `expect fun httpClientEngine(): HttpClientEngine`; actuals in `androidMain` (`OkHttp.create()`) and `iosMain` (`Darwin.create()`).
**Create** `platform/Time.kt` (commonMain) `expect fun epochSeconds(): Long`; android (`System.currentTimeMillis()/1000`), ios (`NSDate().timeIntervalSince1970.toLong()`). Wire `DefaultExchangeRateRepository(now = ::epochSeconds)` in prod.

#### Task 5.3 — `KtorExchangeRateProvider` implements `ExchangeRateProvider`
**Create** `data/net/RatesApi.kt` — `@Serializable ExchangeRateResponse(result, base_code, time_last_update_unix, rates: Map<String,Double>)`; `HttpClient(httpClientEngine()){ install(ContentNegotiation){ json(Json{ ignoreUnknownKeys=true; isLenient=true }) } }`; GET `https://open.er-api.com/v6/latest/$base`.
**Create** `data/net/KtorExchangeRateProvider.kt` — maps the response into `ExchangeRates` (convert each `Double` rate to `BigDecimal` via `BigDecimal.parseString(it.toString())` to stay off `Double` in the math pipeline; API precision ~4dp so this is lossless enough), `timestamp = time_last_update_unix`, `source = LIVE`.
> No `commonTest` for the live call (network). The repository fallback logic is already fully covered with fakes in Phase 3. The provider is thin glue.
**Verify:** `./gradlew :composeApp:assembleDebug` and `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`.

#### Task 5.4 — Wire real repository into the app entry points
**Modify** `MainActivity` and `MainViewController`: build `DefaultExchangeRateRepository(KtorExchangeRateProvider(RatesApi()), InMemoryRatesCache(), now = ::epochSeconds)` and pass to `App(repository, rateRepo)`.
**Verify:** `assembleDebug`.

---

### PHASE 6 — Converter UI

#### Task 6.1 — Converter widgets + body
**Create** `ui/convert/CategorySelector.kt` (scrollable chips over `UnitCategory.entries` → `onConverter(SelectCategory)`), `ui/convert/UnitPickerRow.kt` (two unit pickers + center swap `IconButton` → `SwapUnits`), `ui/convert/ConverterKeypad.kt` (`val converterKeyPad: KeyPad<ConverterIntent>` — 7 8 9 ⌫ / 4 5 6 C / 1 2 3 ± / 0 . ; `±` `enabled` only for Temperature), and `ui/convert/ConverterBody.kt` composing selector → picker row → two synced read-only value surfaces (active field highlighted; tap → `SelectField`; currency shows a "Rates by ExchangeRate-API" attribution + Live/Offline badge from `ratesSource`) → `CalculatorKeypad(converterKeyPad, onKey = vm::onConverter)`.
**Modify** `ui/CalculatorApp.kt`: replace the Converter placeholder with `ConverterBody(...)`.
**Verify:** `assembleDebug`; **on-device**: Length A→B & B→A live; swap; Temperature `100°C→212°F`; Currency converts (airplane mode → bundled + "Offline" badge; online → "Live"); light+dark screenshots.

---

### PHASE 7 — Final verification gates
1. `./gradlew :composeApp:testDebugUnitTest` — all Standard (unchanged) + Programmer + Converter suites green.
2. `./gradlew :composeApp:assembleDebug` — Android green.
3. `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64` — iOS compile/link green.
4. On-device Android sweep across all three modes; capture light+dark screenshots as proof. Confirm sub-state survives mode switches (type in each mode, switch away and back).

---

## Part D — Risks & things that MUST be verified on-device / at build time

- **Kotlin metadata pinning (highest risk).** Ktor **must** be 3.3.3 and serialization **1.9.0**; 3.4.0+/1.10+ compile against Kotlin 2.3 and will throw "compiled with a newer Kotlin" against the locked 2.2.20 compiler. Verify `assembleDebug` after Phase 5.1 immediately.
- **iOS Ktor/Darwin link.** All prior phases are pure `commonMain` and link trivially. The first real iOS risk is Phase 5.2–5.3 (Darwin engine, `NSDate`). Run the iOS link gate right after Task 5.3.
- **Generic keypad regression.** Task 0.2 touches the shared `KeyButton`. Verify Standard is pixel-identical on-device (Task 0.4) before building any new mode.
- **`InMemoryRatesCache` does not persist across process death** — every cold start re-hits the network (or falls to bundled offline). Acceptable for this slice; note if persistent caching is wanted later (add a SQLDelight-backed `RatesCache`).
- **Currency `Double→BigDecimal` at the network boundary.** The API sends ~4-dp `Double`; converting via `.toString()` avoids binary-float artifacts. All downstream math stays `BigDecimal`. Verify a live fetch renders sane values on-device.
- **Attribution requirement.** ExchangeRate-API's free tier requires a visible "Rates by ExchangeRate-API" link; it's part of Task 6.1 — do not drop it.
- **Programmer `ErrorBlip` timing.** It's emitted only on a `null→DivByZero` transition in `onProgrammer`; verify the blip animation actually fires on-device (the effects `SharedFlow` must be collected in `CalculatorApp`, same as today's Standard collector — confirm that collector wasn't lost in the `CalculatorScreen`→`CalculatorApp` split).
- **Denser Programmer keypad layout (11 rows).** The `keyAspect` reduction is the only styling knob; verify the grid fits without clipping on a small screen (on-device, both orientations).
- **`AppState` bootstrap ordering.** `AppState` references `ProgrammerState`/`ConverterState` before those types exist in Phase 0 — add those fields as their types land (Tasks 2.2 / 4.2) or stub the data classes early, or Phase 0 won't compile.