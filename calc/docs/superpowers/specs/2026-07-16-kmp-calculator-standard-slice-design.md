# KMP Calculator — Standard Mode Vertical Slice (Design)

**Date:** 2026-07-16
**Status:** Approved (pending spec review)
**Scope:** First vertical slice of a Kotlin Multiplatform + Compose Multiplatform calculator app.

---

## 1. Goal & Strategy

Build a real, runnable, verified calculator app — not an unverifiable code dump. This first
slice ships the **full production architecture** (high-precision math engine, MVI state
management, reusable adaptive keypad, persistent history, dark/light theming) but delivers only
**Standard mode** end-to-end, polished and actually running on Android.

The architecture is deliberately built so additional modes slot in as their own later
spec → plan → build cycles.

### In scope (this slice)
- **Standard mode** arithmetic: `+ − × ÷ %`, decimal, sign toggle (`±`), parentheses, clear,
  backspace, equals.
- **Live instant result preview** shown faintly below the input before `=`.
- **Interactive calculation history** (bottom sheet): tap an expression to inject it; tap a
  result to inject its value.
- **Memory register**: `MC MR M+ M− MS`.
- **Gestures**: swipe down on the display to open history; swipe left on the input to backspace.
- **Adaptive layout** (phone → foldable/tablet) and **system dark/light** theming with Material 3
  dynamic color on Android 12+.
- **High-precision arithmetic** (no `Double` for core math).
- **Persistent history** across launches.

### Explicitly out of scope (future cycles, each its own spec)
- **Programmer mode** (HEX/DEC/OCT/BIN, bitwise, bit-shift, word sizes).
- **Unit Converter mode** (length/weight/temp/area/volume/speed/currency + live rates).

> **Scientific mode is dropped entirely** — not part of this slice and not on the roadmap.

---

## 2. Toolchain & Project Shape

- **Kotlin 2.x**, Gradle via **wrapper** (no system Gradle required), Compose Multiplatform +
  Compose compiler plugin, Material 3.
- **Single Compose Multiplatform app module** `composeApp` with source sets
  `commonMain / androidMain / iosMain / commonTest`, plus a generated `iosApp` Xcode project.
- **Targets:** `androidTarget`, `iosArm64`, `iosSimulatorArm64`, `iosX64`.
- **Clean architecture via package layering** (not separate Gradle modules):
  - `domain` — math engine, models, pure logic (no framework deps).
  - `data` — history persistence.
  - `presentation` — MVI store, state, intents, effects.
  - `ui` — Compose components, theme, keypad, screens.

**Judgment call (approved):** single module with package layering rather than multi-Gradle-module.
Rationale: multi-module KMP adds heavy build ceremony that slows the verify loop; packages provide
the same clean boundaries now, and a `:shared` module can be extracted later without rework.

Local toolchain confirmed present: JDK 17, Android SDK (`~/Library/Android/sdk`), Xcode 26.2.

---

## 3. Tech-Stack Decisions

| Concern | Choice | Rationale / alternative |
|---|---|---|
| High-precision math | `com.ionspin.kotlin:bignum` (pure-Kotlin multiplatform BigDecimal/BigInteger) | Eliminates float error (`0.1 + 0.2`); works on Android + iOS with no `expect/actual`. Alt: platform BigDecimal via `expect/actual` — more code, no benefit. |
| State management | StateFlow-based **MVI**; common `CalculatorStore` + `CalculatorEffect` via `SharedFlow` | Uni-directional, fully testable in `commonTest`. |
| Navigation | **No navigation library** in the slice | Modes are a segmented control that morphs the keypad within one screen; history is a bottom sheet. Adding Decompose/Voyager now is YAGNI; structured to add later. |
| Persistence | **SQLDelight** (Android driver for verification; native driver dependency for iOS) | Production-grade structured history with reactive queries. Alt: KStore (simpler JSON) — lighter but weaker for querying/history growth. |
| DI | Manual constructor injection + small factory | No DI framework for a one-screen slice. |
| Haptics | `expect/actual` `Haptics` abstraction | Android `HapticFeedback`; iOS real/stub. |
| Coroutines | `kotlinx.coroutines` | Async + `StateFlow`/`SharedFlow`. |
| Testing | `kotlin.test` + `kotlinx-coroutines-test` | Engine + reducer tests run on JVM via `commonTest`. |

---

## 4. Domain — Math Engine

**Pipeline:** `Tokenizer → Shunting-yard (to RPN) → RPN evaluator (BigDecimal)`.

Designed to extend to future functions/operators; the slice wires up `+ − × ÷ %`, decimal,
unary `±`, and parentheses.

### Percentage semantics (calculator-intuitive, context-aware)
- `100 + 10%` → `110`  (percent of the pending left operand, added)
- `100 − 10%` → `90`
- `200 × 10%` → `20`   (percent as a factor)
- `200 ÷ 10%` → `2000`
- bare `50%` → `0.5`

### Error handling
- Result type: `sealed CalcResult { Success(value, formatted) | Error(kind) | Empty }`.
- `kind ∈ { DivByZero, Overflow, Malformed }`.
- **No exceptions leak to the UI** — division by zero, overflow, and malformed input become typed
  `Error` results surfaced as a display state.

### Live preview
- The evaluator tolerates a single trailing operator (trims it) so a faint preview result can be
  computed for an in-progress expression before `=` is pressed.

### Formatting
- Grouping separators for the integer part.
- Trailing-zero trimming.
- Display precision cap with scientific-notation fallback for very large / very small magnitudes.

---

## 5. Presentation — MVI Contracts

- **`CalculatorState`** (immutable data class): `expressionTokens`, `displayText`,
  `preview: CalcResult`, `error`, `memory: BigDecimal?`, `historyVisible`, `mode` (enum; only
  Standard active this slice), plus minimal scaffolding fields for future modes.
- **`CalculatorIntent`** (sealed): `Digit`, `Decimal`, `Operator`, `Percent`, `Paren`,
  `ToggleSign`, `Delete`, `Clear`, `AllClear`, `Equals`, `Memory(MC/MR/M+/M−/MS)`,
  `History(Show/Hide/InjectExpression/InjectResult)`, `SwitchMode`.
- **`CalculatorStore`**: exposes `StateFlow<CalculatorState>` and `dispatch(intent)`; emits
  `CalculatorEffect` (haptic tick, error blip) via `SharedFlow`. Lives in `commonMain`; wrapped in
  an Android `ViewModel` on the platform side.

---

## 6. UI — Reusable Adaptive Keypad

- **Data-driven keypad:** `KeyPad = List<List<Key>>`; `Key(label, intent, style, span)`. One
  composable `CalculatorKeypad(pad, onKey)` renders any mode's grid.
- **Performance:** hoisted immutable key lists, stable keys, press-state via `pointerInput`,
  no per-frame allocations — targeting 120Hz smoothness.
- **Adaptive:** column count / sizing react to `WindowSizeClass` (phone → foldable/tablet).
  `Modifier.animateContentSize()` ready for future keypad morphing between modes.
- **Standard screen layout:** display (expression + faint live preview) → memory row
  (`MC MR M+ M− MS`) → keypad:
  ```
  C   ⌫   %   ÷
  7   8   9   ×
  4   5   6   −
  1   2   3   +
  ±   0   .   =
  ```
- **Gestures:** swipe **down** on display → open history sheet; swipe **left** on input → backspace.
- **Theming:** system dark/light; Material 3 **dynamic color** on Android 12+; tasteful fallback
  scheme elsewhere. Haptics on key press.
- **Interactive history (bottom sheet):** tap expression → inject expression; tap result → inject
  value.

---

## 7. Data — Persistence

- **SQLDelight** table `HistoryEntry(id, expression, result, timestamp)`.
- `HistoryRepository` (common interface) + platform drivers (Android for verification; iOS native
  driver dependency).
- Reactive `Flow<List<HistoryEntry>>` fed into `CalculatorState`.

---

## 8. Test & Verification Plan

**Unit tests (`commonTest`, run on JVM):**
- Math engine: `0.1 + 0.2 == 0.3`, operator precedence, parentheses, all percentage cases,
  div-by-zero → `Error`, overflow, formatting (grouping/trimming/scientific), preview trailing-op
  trimming.
- Store reducer: representative intents (digit entry, operator chaining, equals, backspace, clear,
  memory ops, history injection).

**Builds (green required):**
- Android: `./gradlew :composeApp:assembleDebug`.
- iOS: `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64` (compile/link only — no
  on-screen iOS run this cycle).

**On-screen (Android only):**
- Boot an Android emulator, install the debug APK, launch, and drive the UI: type an expression,
  observe the live preview, press `=`, open history via swipe, tap-to-inject, backspace gesture,
  memory keys, and toggle dark/light.
- Capture **screenshots** (light + dark) as proof of a working, polished app.

---

## 9. Deliverables (mapped to the original request)

1. **Domain Math Engine** — tokenizer + shunting-yard + high-precision RPN evaluator with
   real-time validation (§4).
2. **UI State & Intent Contracts** — MVI `CalculatorState` / `CalculatorIntent` / effects (§5).
3. **Keyboard Components** — data-driven, performance-optimized adaptive `CalculatorKeypad` (§6).
4. **Local Storage** — SQLDelight `HistoryEntry` schema + repository (§7).
