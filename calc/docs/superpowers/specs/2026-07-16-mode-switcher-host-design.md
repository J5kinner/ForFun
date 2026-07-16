I've read the full codebase and existing spec. Here is the design.

---

# Multi-Mode Host Design — Standard / Programmer / Unit Converter

**Date:** 2026-07-16
**Status:** Proposed
**Scope:** Add a mode switcher and two new modes (Programmer, Unit Converter) to the existing single-mode calculator, without breaking Standard.
**Builds on:** `docs/superpowers/specs/2026-07-16-kmp-calculator-standard-slice-design.md`

---

## 1. What exists today (verified in code)

- `CalcMode` enum lives inside `CalculatorState.kt` and has **only** `Standard`. `CalculatorState` carries a `mode` field that nothing reads.
- `CalculatorIntent.SwitchMode(mode)` exists; the reducer handles it (`state.copy(mode = ...)`) but no UI ever dispatches it. It is dead scaffolding.
- One `CalculatorViewModel(repository, engine)` owns a single `MutableStateFlow<CalculatorState>`, a `SharedFlow<CalculatorEffect>`, and the SQLDelight history collect loop.
- Reducer is pure: `CalculatorReducer(engine).reduce(state, intent): Reduction`. Tested in `commonTest` with no framework deps.
- Keypad is data-driven: `Key(label, style, intent: CalculatorIntent, span)`, `KeyPad = List<List<Key>>`, rendered by one `CalculatorKeypad(pad, onKey)`. Haptics fire inside `KeyButton`.
- `CalculatorScreen(vm)` draws the whole screen: `Surface > Box(BottomCenter) > Column` = top-right history `IconButton` row → `CalculatorDisplay` (weight 1f) → `CalculatorKeypad(standardKeyPad)`; plus a conditional `HistorySheet`.
- `App(repository)` = `AppTheme { val vm = viewModel { CalculatorViewModel(repository) }; CalculatorScreen(vm) }`.
- Domain math is `Long`-free and BigDecimal-based (ionspin bignum 0.3.10, which also ships `BigInteger`). Tests use `kotlin.test`; VM tests use `FakeHistoryRepository` + `StandardTestDispatcher`.
- No nav library (documented YAGNI decision). Material3 from CMP 1.11.1.

---

## 2. Key architectural decisions

### 2.1 One host ViewModel with per-mode sub-state (recommended)

**Recommendation: a single root ViewModel that owns the active `mode` plus three independent, pure per-mode reducers/states — NOT three separate ViewModels behind a root.**

```
AppState
 ├─ mode: CalcMode
 ├─ standard:   CalculatorState   (existing, unchanged behaviour)
 ├─ programmer: ProgrammerState   (new)
 └─ converter:  ConverterState    (new)
```

Why this over separate ViewModels per mode:

- **Matches the established pattern.** The project already has exactly one `ViewModel` wrapping pure reducers. Three ViewModels + a root would introduce a lifecycle-hoisting problem (where does `mode` live? who survives config change?) that the current design deliberately avoids.
- **Testability is unaffected.** Each mode keeps its own *pure reducer* (`CalculatorReducer`, `ProgrammerReducer`, `UnitConverterReducer`), each unit-tested in `commonTest` with zero framework deps — the real logic never touches the ViewModel. The ViewModel becomes a thin router.
- **State preservation is free.** Switching modes just changes `mode`; each sub-state persists in `AppState`, so returning to a mode restores exactly what you left. With separate ViewModels you either keep them all alive (defeating the point) or lose state.
- **Single history/effects plumbing.** History (Standard-only) and the effects `SharedFlow` stay wired in one place.
- **YAGNI-consistent.** No DI, no nav, no multi-VM ceremony for a three-screen app.

Cost: `AppState` is a container of three data classes, and the ViewModel exposes three typed `dispatch` entry points. That is the whole tax.

### 2.2 Type-safe per-mode intents (no giant union)

Each mode keeps its own sealed intent hierarchy and its own `dispatch` method on the host. Screens can only send intents their mode understands:

```kotlin
fun onStandard(intent: CalculatorIntent)
fun onProgrammer(intent: ProgrammerIntent)
fun onConverter(intent: ConverterIntent)
fun selectMode(mode: CalcMode)
```

This is cleaner than folding everything into one `CalculatorIntent` and avoids "intent valid for the wrong mode" bugs at compile time.

### 2.3 Mode ownership moves out of `CalculatorState`

`mode` and `SwitchMode` were unused scaffolding embedded in Standard's state. Mode is a **host** concern. We remove `mode` from `CalculatorState` and remove `CalculatorIntent.SwitchMode` + its reducer branch, and put `mode` on `AppState`, switched via `selectMode()`. This is behaviour-preserving for Standard (nothing read `mode`) and gives the Standard reducer a single responsibility again.

### 2.4 Generic data-driven keypad

To reuse the excellent `CalculatorKeypad` for all three modes, make `Key`/keypad generic over the intent type and add an `enabled` flag (Programmer needs A–F disabled outside HEX):

```kotlin
data class Key<I>(
    val label: String,
    val style: KeyStyle,
    val intent: I,
    val span: Int = 1,
    val enabled: Boolean = true,
)
typealias KeyPad<I> = List<List<Key<I>>>
```

`standardKeyPad` becomes `KeyPad<CalculatorIntent>` — a one-line signature change, no behavioural change. This is the smallest change that lets all three modes share the renderer.

### 2.5 History: Standard only (recommended, keep it simple)

Programmer and Unit Converter do **not** contribute to history for now. The SQLDelight schema (`expression, result` strings) is arithmetic-shaped; forcing radix/word-size or from/to-unit context into it would need schema changes for marginal value. The history button therefore only appears in Standard mode. If Programmer/Converter history is wanted later, add a `mode` column and a discriminated record — out of scope here.

---

## 3. Package / file plan

```
domain/
  programmer/  Radix.kt  WordSize.kt  ProgOp.kt  ProgrammerEngine.kt
  convert/     UnitCategory.kt  ConvUnit.kt  UnitConverter.kt
presentation/
  CalcMode.kt                         (moved out, expanded to 3)
  AppState.kt                         (new host state)
  CalculatorState.kt                  (mode field removed)
  CalculatorIntent.kt                 (SwitchMode removed)
  CalculatorReducer.kt                (SwitchMode branch removed)
  CalculatorViewModel.kt  -> AppViewModel.kt (host)
  programmer/  ProgrammerState.kt  ProgrammerIntent.kt  ProgrammerReducer.kt
  convert/     ConverterState.kt   ConverterIntent.kt   UnitConverterReducer.kt
ui/
  CalculatorApp.kt          (new host scaffold: top bar + when(mode) body)
  ModeSwitcher.kt           (new segmented control)
  StandardBody.kt           (extracted from CalculatorScreen inner content)
  programmer/ ProgrammerBody.kt  ProgrammerKeyPad.kt
  convert/    ConverterBody.kt
  keypad/ Key.kt CalculatorKeypad.kt  (generified)
App.kt                       (mounts CalculatorApp with AppViewModel)
```

Everything stays in `commonMain`; no new `expect/actual` (Programmer uses `Long`, Converter uses BigDecimal — both multiplatform).

---

## 4. Task-by-task plan (TDD throughout)

Each domain/presentation task is **red → green**: write `kotlin.test` cases first, then implement.

### Task A — Lift mode ownership (refactor, non-breaking)

Move and expand the enum:

```kotlin
// presentation/CalcMode.kt
package com.example.calc.presentation

enum class CalcMode(val label: String) {
    Standard("Std"),
    Programmer("Prog"),
    Converter("Conv"),
}
```

Trim Standard's state and intents:

```kotlin
// CalculatorState.kt — remove `mode` field and the CalcMode enum declaration.
data class CalculatorState(
    val input: String = "",
    val preview: String = "",
    val result: BigDecimal? = null,
    val error: CalcError? = null,
    val justEvaluated: Boolean = false,
    val history: List<HistoryRecord> = emptyList(),
    val historyVisible: Boolean = false,
)
```

```kotlin
// CalculatorIntent.kt — delete `data class SwitchMode(...)`.
// CalculatorReducer.kt — delete the `is CalculatorIntent.SwitchMode -> ...` branch.
```

New host state:

```kotlin
// presentation/AppState.kt
package com.example.calc.presentation

data class AppState(
    val mode: CalcMode = CalcMode.Standard,
    val standard: CalculatorState = CalculatorState(),
    val programmer: ProgrammerState = ProgrammerState(),
    val converter: ConverterState = ConverterState(),
)
```

Test updates: existing `CalculatorReducerTest` needs no change (never referenced `mode`). Build stays green.

### Task B — Generify the keypad (refactor, non-breaking)

```kotlin
// ui/keypad/Key.kt
enum class KeyStyle { Number, Operator, Function, Accent, Equals }

data class Key<I>(
    val label: String,
    val style: KeyStyle,
    val intent: I,
    val span: Int = 1,
    val enabled: Boolean = true,
)
typealias KeyPad<I> = List<List<Key<I>>>
```

```kotlin
// ui/keypad/CalculatorKeypad.kt
@Composable
fun <I> CalculatorKeypad(
    pad: KeyPad<I>,
    onKey: (I) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in pad) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (key in row) {
                    KeyButton(
                        key = key,
                        modifier = Modifier.weight(key.span.toFloat()),
                        onClick = {
                            if (!key.enabled) return@KeyButton
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onKey(key.intent)
                        },
                    )
                }
            }
        }
    }
}
```

`KeyButton` gains: when `!key.enabled`, dim (`bg.copy(alpha = .35f)`, `fg.copy(alpha = .35f)`) and skip `.clickable`. `standardKeyPad` type changes to `KeyPad<CalculatorIntent>` (`digit()` helper unchanged). No behavioural change to Standard.

### Task C — Host ViewModel (rename `CalculatorViewModel` → `AppViewModel`)

```kotlin
// presentation/AppViewModel.kt
class AppViewModel(
    private val repository: HistoryRepository,
    engine: MathEngine = MathEngine(),
) : ViewModel() {

    private val standardReducer = CalculatorReducer(engine)
    private val programmerReducer = ProgrammerReducer()
    private val converterReducer = UnitConverterReducer()

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()
    private val _effects = MutableSharedFlow<CalculatorEffect>(extraBufferCapacity = 16)
    val effects: SharedFlow<CalculatorEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.observeHistory().collect { items ->
                _state.update { it.copy(standard = it.standard.copy(history = items)) }
            }
        }
    }

    fun selectMode(mode: CalcMode) = _state.update { it.copy(mode = mode) }

    fun onStandard(intent: CalculatorIntent) {
        val r = standardReducer.reduce(_state.value.standard, intent)
        _state.update { it.copy(standard = r.state) }
        for (e in r.effects) when (e) {
            is CalculatorEffect.PersistHistory ->
                viewModelScope.launch { repository.add(e.expression, e.result) }
            else -> _effects.tryEmit(e)
        }
    }

    fun onProgrammer(intent: ProgrammerIntent) =
        _state.update { it.copy(programmer = programmerReducer.reduce(it.programmer, intent)) }

    fun onConverter(intent: ConverterIntent) =
        _state.update { it.copy(converter = converterReducer.reduce(it.converter, intent)) }

    fun clearHistory() = viewModelScope.launch { repository.clear() }
}
```

VM tests: port the two existing tests to `onStandard(...)` / `_state.value.standard`, add `selectMode` preserves sub-state test, add a Programmer and Converter routing test.

### Task D — Mode switcher + host scaffold; extract `StandardBody`

Custom segmented control (hand-rolled, dependency-free — consistent with the inline `HistoryIcon` precedent; avoids the experimental `SegmentedButton` API surface). It is data-driven over `CalcMode.entries`:

```kotlin
// ui/ModeSwitcher.kt
@Composable
fun ModeSwitcher(
    selected: CalcMode,
    onSelect: (CalcMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        color = scheme.surfaceVariant,
        shape = RoundedCornerShape(percent = 50),
        modifier = modifier.height(40.dp),
    ) {
        Row(Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (mode in CalcMode.entries) {
                val active = mode == selected
                Surface(
                    color = if (active) scheme.primary else Color.Transparent,
                    contentColor = if (active) scheme.onPrimary else scheme.onSurfaceVariant,
                    shape = RoundedCornerShape(percent = 50),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSelect(mode) },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(mode.label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
```

Host scaffold owns the outer `Surface/Box/Column` and the top bar; the mode `when` selects a body. Only Standard shows the history button:

```kotlin
// ui/CalculatorApp.kt
@Composable
fun CalculatorApp(vm: AppViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Box(Alignment.BottomCenter, Modifier.fillMaxSize().safeDrawingPadding()) {
            Column(
                Modifier.fillMaxSize().widthIn(max = 560.dp).padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ModeSwitcher(state.mode, vm::selectMode, Modifier.weight(1f))
                    if (state.mode == CalcMode.Standard) {
                        IconButton(onClick = { vm.onStandard(CalculatorIntent.ShowHistory) }) {
                            Icon(HistoryIcon, "History", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
                when (state.mode) {
                    CalcMode.Standard   -> StandardBody(state.standard, vm::onStandard, Modifier.weight(1f))
                    CalcMode.Programmer -> ProgrammerBody(state.programmer, vm::onProgrammer, Modifier.weight(1f))
                    CalcMode.Converter  -> ConverterBody(state.converter, vm::onConverter, Modifier.weight(1f))
                }
            }
        }
    }
    if (state.mode == CalcMode.Standard && state.standard.historyVisible) {
        HistorySheet(
            history = state.standard.history,
            onInjectExpression = { vm.onStandard(CalculatorIntent.InjectExpression(it)) },
            onInjectResult = { vm.onStandard(CalculatorIntent.InjectResult(it)) },
            onClear = { vm.clearHistory() },
            onDismiss = { vm.onStandard(CalculatorIntent.HideHistory) },
        )
    }
}
```

`StandardBody` is the *inner* content lifted verbatim out of today's `CalculatorScreen` (display + keypad), now taking sub-state + a dispatch lambda:

```kotlin
// ui/StandardBody.kt
@Composable
fun StandardBody(
    state: CalculatorState,
    dispatch: (CalculatorIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.Bottom) {
        CalculatorDisplay(
            state = state,
            onSwipeDown = { dispatch(CalculatorIntent.ShowHistory) },
            modifier = Modifier.weight(1f),
        )
        CalculatorKeypad(pad = standardKeyPad, onKey = dispatch)
    }
}
```

`App.kt`:

```kotlin
@Composable
fun App(repository: HistoryRepository) {
    AppTheme {
        val vm = viewModel { AppViewModel(repository) }
        CalculatorApp(vm)
    }
}
```

Verify Standard is visually/behaviourally identical before moving on.

### Task E — Programmer domain (pure, TDD)

Use `Long` (64-bit, the natural programmer-calc width) with word-size masking; two's-complement semantics for signed decimal display. No BigInteger needed — QWORD is the ceiling.

```kotlin
// domain/programmer/Radix.kt
enum class Radix(val base: Int, val label: String, val digits: String) {
    HEX(16, "HEX", "0123456789ABCDEF"),
    DEC(10, "DEC", "0123456789"),
    OCT(8,  "OCT", "01234567"),
    BIN(2,  "BIN", "01"),
}

// domain/programmer/WordSize.kt
enum class WordSize(val bits: Int, val label: String) {
    BYTE(8, "BYTE"), WORD(16, "WORD"), DWORD(32, "DWORD"), QWORD(64, "QWORD");
    val mask: Long get() = if (bits == 64) -1L else (1L shl bits) - 1L
}

// domain/programmer/ProgOp.kt
enum class ProgOp { Add, Sub, Mul, Div, And, Or, Xor, Lsh, Rsh }
```

```kotlin
// domain/programmer/ProgrammerEngine.kt
object ProgrammerEngine {
    fun mask(v: Long, w: WordSize): Long = v and w.mask

    fun apply(a: Long, op: ProgOp, b: Long, w: WordSize): CalcResult /* reuse Success/Error */ {
        if ((op == ProgOp.Div) && mask(b, w) == 0L) return CalcResult.Error(CalcError.DivByZero)
        val r = when (op) {
            ProgOp.Add -> a + b;  ProgOp.Sub -> a - b
            ProgOp.Mul -> a * b;  ProgOp.Div -> a / b
            ProgOp.And -> a and b; ProgOp.Or -> a or b; ProgOp.Xor -> a xor b
            ProgOp.Lsh -> a shl (b.toInt() and 63); ProgOp.Rsh -> a shr (b.toInt() and 63)
        }
        return CalcResult.Success(/* wrap masked r */ ...)   // or a ProgResult(Long) type
    }

    fun not(a: Long, w: WordSize): Long = mask(a.inv(), w)

    fun parse(text: String, radix: Radix): Long?      // per-radix, ignore sign, null if invalid
    fun format(v: Long, radix: Radix, w: WordSize): String  // DEC signed; HEX/OCT/BIN unsigned masked
}
```

> Note: reuse `CalcResult`/`CalcError` for div-by-zero, or introduce a small `ProgResult` sealed type if you prefer to keep `Long` out of `BigDecimal`-typed `Success`. TDD tests: `mask` per word size, `not(0x00, BYTE) == 0xFF`, `AND/OR/XOR`, `Lsh/Rsh`, wrap-around overflow (`0xFF + 1` in BYTE → `0x00`), div-by-zero, `format` signed decimal for the high bit set (`0xFF` BYTE → `-1`), radix round-trips.

### Task F — Programmer presentation (pure reducer, TDD)

```kotlin
// presentation/programmer/ProgrammerState.kt
data class ProgrammerState(
    val value: Long = 0L,
    val entry: String = "0",          // current-radix text being typed
    val pending: PendingProg? = null, // committed lhs + op
    val radix: Radix = Radix.DEC,
    val wordSize: WordSize = WordSize.QWORD,
    val error: CalcError? = null,
    val justEvaluated: Boolean = false,
)
data class PendingProg(val lhs: Long, val op: ProgOp)

// presentation/programmer/ProgrammerIntent.kt
sealed interface ProgrammerIntent {
    data class Digit(val c: Char) : ProgrammerIntent      // '0'..'9','A'..'F'
    data class Op(val op: ProgOp) : ProgrammerIntent
    data object Not : ProgrammerIntent
    data object Equals : ProgrammerIntent
    data object Delete : ProgrammerIntent
    data object Clear : ProgrammerIntent
    data class SetRadix(val radix: Radix) : ProgrammerIntent
    data class SetWordSize(val size: WordSize) : ProgrammerIntent
}
```

Immediate-execution reducer (classic programmer-calc semantics — avoids bit-op precedence ambiguity): `Digit` appends to `entry` (validated against `radix.digits`) and reparses `value`; `Op` computes any `pending` then stores `PendingProg(value, op)` and arms a fresh entry; `Equals` applies `pending`; `Not`/`SetWordSize` act on `value` immediately (re-masking); `SetRadix` reformats `entry` from `value` in the new base. Every result is masked by `wordSize`. Tests mirror `CalculatorReducerTest` style.

### Task G — Programmer UI

`programmerKeyPad(radix): KeyPad<ProgrammerIntent>` — a *function of radix* so A–F carry `enabled = radix == Radix.HEX` (rendered dimmed otherwise). Layout adds a bitwise column/row (AND OR XOR NOT LSH RSH) and reuses number keys 0–9 + A–F:

```kotlin
fun programmerKeyPad(radix: Radix): KeyPad<ProgrammerIntent> {
    fun d(c: Char, enabled: Boolean = true) =
        Key(c.toString(), KeyStyle.Number, ProgrammerIntent.Digit(c), enabled = enabled)
    val hex = radix == Radix.HEX
    return listOf(
        listOf(Key("AND", KeyStyle.Function, ProgrammerIntent.Op(ProgOp.And)),
               Key("OR",  KeyStyle.Function, ProgrammerIntent.Op(ProgOp.Or)),
               Key("XOR", KeyStyle.Function, ProgrammerIntent.Op(ProgOp.Xor)),
               Key("NOT", KeyStyle.Function, ProgrammerIntent.Not)),
        listOf(d('A', hex), d('B', hex), Key("«", KeyStyle.Operator, ProgrammerIntent.Op(ProgOp.Lsh)),
               Key("÷", KeyStyle.Operator, ProgrammerIntent.Op(ProgOp.Div))),
        listOf(d('C', hex), d('D', hex), Key("»", KeyStyle.Operator, ProgrammerIntent.Op(ProgOp.Rsh)),
               Key("×", KeyStyle.Operator, ProgrammerIntent.Op(ProgOp.Mul))),
        listOf(d('E', hex), d('F', hex), Key("⌫", KeyStyle.Function, ProgrammerIntent.Delete),
               Key("−", KeyStyle.Operator, ProgrammerIntent.Op(ProgOp.Sub))),
        listOf(d('7'), d('8'), d('9'), Key("+", KeyStyle.Operator, ProgrammerIntent.Op(ProgOp.Plus_ /*Add*/))),
        // ... 4 5 6 / 1 2 3 / C 0 =  rows
    )
}
```

`ProgrammerBody` renders: a **radix row** (four `SetRadix` chips showing each base's current formatting of `value`, active one highlighted), a **word-size row** (`SetWordSize` chips), the display (value formatted in current radix; use a Programmer-specific `displayText()` extension), then `CalculatorKeypad(programmerKeyPad(state.radix), onKey = dispatch)`. Keypad stays bottom-anchored for thumb reach; radix/word-size chips sit just above the display.

### Task H — Converter domain (pure, TDD)

BigDecimal throughout (matches the precision ethos). Units carry `toBase`/`fromBase` lambdas so temperature's affine conversion is handled uniformly with linear ones.

```kotlin
// domain/convert/ConvUnit.kt
data class ConvUnit(
    val symbol: String,
    val name: String,
    val toBase: (BigDecimal) -> BigDecimal,
    val fromBase: (BigDecimal) -> BigDecimal,
)
fun linear(factor: String): Pair<(BigDecimal)->BigDecimal,(BigDecimal)->BigDecimal> {
    val f = BigDecimal.parseString(factor)
    return { v -> v * f } to { v -> v / f }
}

// domain/convert/UnitCategory.kt  (slice: Length, Mass, Temperature; extensible)
enum class UnitCategory(val label: String, val units: List<ConvUnit>) {
    Length("Length", listOf(/* m(base), km, cm, mi, ft, in ... via linear(...) */)),
    Mass("Mass", listOf(/* kg(base), g, lb, oz ... */)),
    Temperature("Temp", listOf(/* C(base), F: toBase{(it-32)*5/9}, K: toBase{it-273.15} */)),
}

// domain/convert/UnitConverter.kt
object UnitConverter {
    fun convert(value: BigDecimal, from: ConvUnit, to: ConvUnit): BigDecimal =
        to.fromBase(from.toBase(value))
}
```

TDD tests: km↔mi, kg↔lb, C↔F↔K (0 °C = 32 °F = 273.15 K), identity conversion, precision (no float drift). Reuse `NumberFormatter.format` for display.

### Task I — Converter presentation (pure reducer, TDD)

```kotlin
data class ConverterState(
    val category: UnitCategory = UnitCategory.Length,
    val fromIndex: Int = 0,
    val toIndex: Int = 1,
    val input: String = "",
    val result: String = "",
)
sealed interface ConverterIntent {
    data class Digit(val c: Char) : ConverterIntent
    data object Decimal : ConverterIntent
    data object Delete : ConverterIntent
    data object Clear : ConverterIntent
    data class SetCategory(val c: UnitCategory) : ConverterIntent
    data class SetFrom(val i: Int) : ConverterIntent
    data class SetTo(val i: Int) : ConverterIntent
    data object Swap : ConverterIntent
}
```

Reducer recomputes `result` after every mutation via `UnitConverter` + `NumberFormatter`; `SetCategory` resets `fromIndex=0, toIndex=1`; `Swap` exchanges indices and recomputes. Pure, no effects.

### Task J — Converter UI

`ConverterBody`: category chip row (`SetCategory`), a from/to pair (two unit pickers with a swap button between — use simple clickable `Surface` dropdowns or a horizontally scrollable chip row per side to stay dependency-free), the value/result display, and a **numeric-only** `converterKeyPad: KeyPad<ConverterIntent>` (0–9, `.`, `⌫`, `C`) reusing `CalculatorKeypad`. No `=`; conversion is live.

### Task K — Wire-up, verification, screenshots

- Confirm the `when(mode)` in `CalculatorApp` mounts all three bodies and preserves sub-state on switch.
- Green builds: `./gradlew :composeApp:assembleDebug` and `:composeApp:linkDebugFrameworkIosSimulatorArm64`.
- On-screen Android: switch across all three modes, drive each (Standard unchanged incl. history; Programmer radix/word-size/bitwise; Converter live temperature + length). Capture light/dark screenshots as proof.

---

## 5. Non-breaking guarantees

- Standard's reducer, engine, display, history sheet, and `standardKeyPad` layout are behaviourally untouched; only `Key`'s type parameter and the `mode`/`SwitchMode` scaffolding removal change existing files, both mechanical.
- All new logic is pure and lives in `commonMain` with `commonTest` coverage; no new `expect/actual`, no new libraries (Programmer = `Long`; Converter = existing bignum; switcher = hand-rolled).
- History remains Standard-only; the button is hidden in other modes.

---

## 6. Files the implementer will touch

Modify: `CalculatorState.kt`, `CalculatorIntent.kt`, `CalculatorReducer.kt`, `CalculatorViewModel.kt`→`AppViewModel.kt`, `ui/keypad/Key.kt`, `ui/keypad/CalculatorKeypad.kt`, `ui/keypad/StandardKeyPad.kt`, `ui/CalculatorScreen.kt`→split into `CalculatorApp.kt`+`StandardBody.kt`, `App.kt`, and the VM/reducer tests.
Create: `presentation/CalcMode.kt`, `presentation/AppState.kt`, the `domain/programmer/*`, `domain/convert/*`, `presentation/programmer/*`, `presentation/convert/*`, `ui/ModeSwitcher.kt`, `ui/programmer/*`, `ui/convert/*`, plus their `commonTest` suites.

All referenced existing paths are under `/Users/jonahskinner/Github/ForFun/calc/composeApp/src/commonMain/kotlin/com/example/calc/` and tests under `.../commonTest/kotlin/com/example/calc/`.