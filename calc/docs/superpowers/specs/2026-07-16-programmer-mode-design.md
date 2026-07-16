I have everything needed. Here is the complete design.

---

# Programmer Mode — KMP Calculator (TDD Implementation Plan)

**Date:** 2026-07-16 · **Status:** Design (for review) · **Scope:** Second vertical slice — Programmer mode (HEX/DEC/OCT/BIN, bitwise, shifts, word sizes), slotting into the existing Standard-mode architecture with zero rework of the proven Standard pipeline.

Locked toolchain (unchanged): Kotlin 2.2.20, Compose MP 1.11.1, AGP 8.13.0, Gradle 8.14.3, SQLDelight 2.3.2, ionspin bignum 0.3.10, coroutines 1.10.2, lifecycle 2.9.6, compileSdk/targetSdk 36, minSdk 24, JDK 17. No new dependencies required.

---

## 1. Key architectural decisions (and justification)

### 1.1 Value representation — canonical masked `Long`, two's-complement

Programmer mode is an **integer** world, not a `BigDecimal` world. All values fit in ≤ 64 bits (max word size QWORD = 64), so a Kotlin `Long` is the perfect canonical store — it *is* 64-bit two's complement, and every bitwise/shift primitive (`and`, `or`, `xor`, `inv`, `shl`, `shr`, `ushr`) maps 1:1 to hardware semantics. **No `bignum` is needed here** (and using it would fight the whole point — bignum has no fixed-width wraparound).

Canonical form: the value is stored **masked to the low `wordSize.bits` bits, high bits zero** (an unsigned bit-pattern). Two interpretations are derived on demand:
- **DEC display** = sign-extend the low `bits` to a signed `Long`, then `toString()`.
- **HEX / OCT / BIN display** = the raw low `bits` printed unsigned.

This is exactly the Windows-calculator convention (e.g. BYTE value `-1` → DEC `-1`, HEX `FF`, BIN `11111111`; QWORD `-1` → DEC `-1`, HEX `FFFFFFFFFFFFFFFF`).

Why masked-unsigned-canonical rather than storing the sign-extended signed Long: it makes HEX/OCT/BIN formatting trivial (no per-radix sign logic), keeps word-size switching a pure re-mask, and entry-building (see §1.3) becomes plain modular arithmetic.

### 1.2 Evaluation model — immediate-execution accumulator, NOT an expression parser

Standard mode uses `Tokenizer → shunting-yard → RPN`. **Programmer mode deliberately does not.** Recommendation: a classic **immediate-execution accumulator** (`acc`, `pending op`, `entry`), the model every hardware programmer calculator uses.

Justification:
- **Bitwise operator precedence is a footgun.** C's precedence (`&` below `^` below `|`, shifts above all, below `+`) is unfamiliar and surprising to users; a precedence parser would compute things users don't expect. Immediate execution ("what you press is what applies") is predictable and is the established UX for this exact feature.
- It is a tiny, **pure** reducer — trivially unit-testable in `commonTest`, matching the existing MVI discipline.
- It avoids reintroducing a whole tokenizer/parser for a different value type. The Standard pipeline stays untouched and its 30-odd green tests stay green.

### 1.3 MVI integration — a dedicated `ProgrammerState` + `ProgrammerReducer`, composed into `CalculatorState`

**Recommended: separate sub-state + separate pure reducer, composed into the existing single `CalculatorState`/single `StateFlow`.** Not an extension of the Standard reducer.

- `CalcMode` gains a `Programmer` entry.
- `CalculatorState` gains one field: `val programmer: ProgrammerState = ProgrammerState()`.
- A new pure `ProgrammerReducer` owns all programmer logic on `ProgrammerState`.
- `CalculatorReducer` gains **one** new branch: `is CalculatorIntent.Programmer -> delegate to ProgrammerReducer, write back state.programmer`. Every existing Standard branch is untouched.

Why this over the two alternatives:
- *vs. cramming programmer logic into `CalculatorReducer`'s string-`input` model:* the Standard reducer's whole world is a glyph string parsed by BigDecimal. Programmer values are masked Longs with radix/word-size. Merging them would sprinkle `when (mode)` through every branch and endanger tested Standard behaviour. Rejected.
- *vs. a fully separate second `StateFlow`/store:* the UI already collects one `CalculatorState`; mode is already a field there; history/effects plumbing is shared. Composing one sub-state keeps a single source of truth and one `collectAsStateWithLifecycle`. Chosen.

The existing `CalculatorEffect` (`Haptic`, `ErrorBlip`) and `Reduction` types are reused as-is. Programmer results are **not** persisted to SQLDelight history in this slice (history schema is expression/decimal-string oriented; wiring radix-aware history is explicitly deferred). `Reduction` for programmer intents carries `Haptic`/`ErrorBlip` only.

---

## 2. Domain layer (new files under `.../domain/`)

### 2.1 `WordSize.kt`

```kotlin
package com.example.calc.domain

enum class WordSize(val label: String, val bits: Int) {
    BYTE("BYTE", 8),
    WORD("WORD", 16),
    DWORD("DWORD", 32),
    QWORD("QWORD", 64),
}
```

### 2.2 `Radix.kt`

```kotlin
package com.example.calc.domain

enum class Radix(val label: String, val base: Int) {
    HEX("HEX", 16),
    DEC("DEC", 10),
    OCT("OCT", 8),
    BIN("BIN", 2);

    /** True if a keypad digit with this numeric value is legal in this radix. */
    fun allowsDigit(value: Int): Boolean = value in 0 until base
}
```

### 2.3 `Bits.kt` — pure fixed-width primitives (the heart; TDD first)

```kotlin
package com.example.calc.domain

/**
 * Fixed-width two's-complement arithmetic over a Long canonical store.
 * The canonical form of every value is "masked": the low `ws.bits` bits hold the
 * value and all higher bits are zero. `signed()` reinterprets it as a signed Long.
 */
object Bits {

    /** Keep only the low `ws.bits` bits (high bits zeroed). QWORD is identity. */
    fun mask(value: Long, ws: WordSize): Long =
        if (ws == WordSize.QWORD) value else value and ((1L shl ws.bits) - 1L)

    /** Sign-extend a masked value to a full signed Long (for DEC display / signed div). */
    fun signed(masked: Long, ws: WordSize): Long {
        if (ws == WordSize.QWORD) return masked
        val shift = 64 - ws.bits
        return (masked shl shift) shr shift   // arithmetic right shift extends the sign
    }

    // --- Bitwise ---
    fun and(a: Long, b: Long, ws: WordSize) = mask(a and b, ws)
    fun or(a: Long, b: Long, ws: WordSize)  = mask(a or b, ws)
    fun xor(a: Long, b: Long, ws: WordSize) = mask(a xor b, ws)
    fun not(a: Long, ws: WordSize)          = mask(a.inv(), ws)
    fun nand(a: Long, b: Long, ws: WordSize) = mask((a and b).inv(), ws)
    fun nor(a: Long, b: Long, ws: WordSize)  = mask((a or b).inv(), ws)
    fun xnor(a: Long, b: Long, ws: WordSize) = mask((a xor b).inv(), ws)

    /** Two's-complement negate: mask(-value). NEG of the min value stays itself (overflow). */
    fun negate(a: Long, ws: WordSize) = mask(-signed(a, ws), ws)

    // --- Shifts. n is clamped to [0, bits]; guards Kotlin's shift-count-mod-64 trap. ---
    fun shl(a: Long, n: Int, ws: WordSize): Long {
        if (n <= 0) return mask(a, ws)
        if (n >= ws.bits) return 0L
        return mask(a shl n, ws)
    }

    /** Logical right shift: zero-fill. Operates on the unsigned masked pattern. */
    fun shrLogical(a: Long, n: Int, ws: WordSize): Long {
        if (n <= 0) return mask(a, ws)
        if (n >= ws.bits) return 0L
        return mask(mask(a, ws) ushr n, ws)   // mask first so QWORD bit-63 is treated unsigned
    }

    /** Arithmetic right shift: sign-fill. */
    fun shrArithmetic(a: Long, n: Int, ws: WordSize): Long {
        if (n <= 0) return mask(a, ws)
        val s = signed(a, ws)
        if (n >= ws.bits) return mask(s shr 63, ws)   // all-sign result
        return mask(s shr n, ws)
    }

    // --- Integer arithmetic (masked; wraps like hardware). Signed division/truncation. ---
    fun add(a: Long, b: Long, ws: WordSize) = mask(a + b, ws)
    fun sub(a: Long, b: Long, ws: WordSize) = mask(a - b, ws)
    fun mul(a: Long, b: Long, ws: WordSize) = mask(a * b, ws)

    /** Signed truncating division. Throws ProgArithmeticException on divide-by-zero. */
    fun div(a: Long, b: Long, ws: WordSize): Long {
        if (mask(b, ws) == 0L) throw ProgArithmeticException()
        return mask(signed(a, ws) / signed(b, ws), ws)
    }
    fun rem(a: Long, b: Long, ws: WordSize): Long {
        if (mask(b, ws) == 0L) throw ProgArithmeticException()
        return mask(signed(a, ws) % signed(b, ws), ws)
    }

    /** Append one radix digit to an entry, masked (used by the reducer). */
    fun appendDigit(entry: Long, digit: Int, radix: Radix, ws: WordSize): Long =
        mask(entry * radix.base + digit, ws)

    /** Remove the last radix digit (backspace). */
    fun dropDigit(entry: Long, radix: Radix, ws: WordSize): Long {
        // Divide the *unsigned* low-bits representation by the base.
        val u = mask(entry, ws)
        return mask(u.toULong().div(radix.base.toULong()).toLong(), ws)
    }
}

class ProgArithmeticException : Exception("Programmer arithmetic error")
```

Notes:
- `appendDigit` needs no explicit overflow check: at QWORD the wraparound of `entry * 16 + d` mod 2⁶⁴ is exactly the intended low-64-bit pattern, so building `FFFFFFFFFFFFFFFF` works despite intermediate signed overflow.
- `dropDigit` uses `ULong` division so backspacing a QWORD value with bit-63 set halves the unsigned pattern correctly.

### 2.4 `RadixFormatter.kt`

```kotlin
package com.example.calc.domain

object RadixFormatter {

    fun format(masked: Long, radix: Radix, ws: WordSize): String = when (radix) {
        Radix.DEC -> Bits.signed(masked, ws).toString()
        Radix.HEX -> unsignedString(masked, ws, 16).uppercase()
        Radix.OCT -> unsignedString(masked, ws, 8)
        Radix.BIN -> groupBinary(unsignedString(masked, ws, 2))
    }

    /** All four radixes at once, for the multi-radix panel. */
    fun all(masked: Long, ws: WordSize): Map<Radix, String> =
        Radix.entries.associateWith { format(masked, it, ws) }

    private fun unsignedString(masked: Long, ws: WordSize, base: Int): String {
        val u = Bits.mask(masked, ws).toULong()
        return u.toString(base)   // ULong.toString(radix) is unsigned, no leading zeros, "0" when zero
    }

    /** Space-group binary into nibbles from the right: 1111011 -> "111 1011". */
    private fun groupBinary(bin: String): String =
        bin.reversed().chunked(4).joinToString(" ").reversed()
}
```

---

## 3. Presentation layer

### 3.1 `ProgrammerState.kt`

```kotlin
package com.example.calc.presentation

import com.example.calc.domain.Radix
import com.example.calc.domain.WordSize

enum class ProgError { DivByZero }

data class ProgrammerState(
    val wordSize: WordSize = WordSize.QWORD,
    val radix: Radix = Radix.DEC,
    val entry: Long = 0L,          // masked current entry (the "displayed" value)
    val acc: Long = 0L,            // masked accumulator (lhs of a pending binary op)
    val pending: ProgBinOp? = null,
    val freshEntry: Boolean = true, // next digit replaces entry rather than appending
    val error: ProgError? = null,
)
```

### 3.2 Intents (extend the existing sealed hierarchy)

Add to `CalculatorIntent.kt` one wrapper plus a nested `ProgIntent` set (keeps the top-level dispatch surface single-typed and lets `ProgrammerReducer` own the enum of ops):

```kotlin
sealed interface ProgBinOp { }   // in domain or presentation; see enum below

enum class ProgBinOp2 // placeholder — real definition:
```

Concretely, in `presentation/ProgrammerIntent.kt`:

```kotlin
package com.example.calc.presentation

import com.example.calc.domain.Radix
import com.example.calc.domain.WordSize

enum class ProgBinOp { And, Or, Xor, Nand, Nor, Xnor, Shl, ShrLogical, ShrArith, Add, Sub, Mul, Div, Mod }
enum class ProgUnaryOp { Not, Negate }

sealed interface ProgIntent {
    data class Digit(val value: Int) : ProgIntent      // 0..15 (A=10 … F=15)
    data class Binary(val op: ProgBinOp) : ProgIntent
    data class Unary(val op: ProgUnaryOp) : ProgIntent
    data object Equals : ProgIntent
    data object Delete : ProgIntent
    data object Clear : ProgIntent
    data class SetRadix(val radix: Radix) : ProgIntent
    data class SetWordSize(val wordSize: WordSize) : ProgIntent
}
```

And one line added to `CalculatorIntent`:

```kotlin
data class Programmer(val intent: ProgIntent) : CalculatorIntent
```

### 3.3 `ProgrammerReducer.kt` (pure)

```kotlin
package com.example.calc.presentation

import com.example.calc.domain.Bits
import com.example.calc.domain.ProgArithmeticException

class ProgrammerReducer {

    fun reduce(state: ProgrammerState, intent: ProgIntent): ProgrammerState = when (intent) {
        is ProgIntent.Digit -> {
            if (!state.radix.allowsDigit(intent.value)) state   // illegal digit: no-op
            else {
                val ws = state.wordSize
                val base = if (state.freshEntry || state.error != null) 0L else state.entry
                ProgrammerState(
                    wordSize = ws, radix = state.radix,
                    entry = Bits.appendDigit(base, intent.value, state.radix, ws),
                    acc = state.acc, pending = state.pending, freshEntry = false, error = null,
                )
            }
        }

        is ProgIntent.Binary -> {
            val folded = if (state.pending != null && !state.freshEntry)
                applyPending(state) else state.copy(acc = state.entry)
            folded.copy(pending = intent.op, freshEntry = true, error = null)
        }

        is ProgIntent.Unary -> {
            val ws = state.wordSize
            val v = when (intent.op) {
                ProgUnaryOp.Not -> Bits.not(state.entry, ws)
                ProgUnaryOp.Negate -> Bits.negate(state.entry, ws)
            }
            state.copy(entry = v, freshEntry = false, error = null)
        }

        ProgIntent.Equals ->
            if (state.pending == null) state
            else applyPending(state).copy(pending = null, freshEntry = true)

        ProgIntent.Delete ->
            if (state.freshEntry) state
            else state.copy(entry = Bits.dropDigit(state.entry, state.radix, state.wordSize))

        ProgIntent.Clear -> ProgrammerState(wordSize = state.wordSize, radix = state.radix)

        is ProgIntent.SetRadix -> state.copy(radix = intent.radix)

        is ProgIntent.SetWordSize -> state.copy(
            wordSize = intent.wordSize,
            entry = Bits.mask(state.entry, intent.wordSize),
            acc = Bits.mask(state.acc, intent.wordSize),
        )
    }

    private fun applyPending(state: ProgrammerState): ProgrammerState {
        val op = state.pending ?: return state
        val ws = state.wordSize
        val a = state.acc; val b = state.entry
        return try {
            val r = when (op) {
                ProgBinOp.And -> Bits.and(a, b, ws)
                ProgBinOp.Or -> Bits.or(a, b, ws)
                ProgBinOp.Xor -> Bits.xor(a, b, ws)
                ProgBinOp.Nand -> Bits.nand(a, b, ws)
                ProgBinOp.Nor -> Bits.nor(a, b, ws)
                ProgBinOp.Xnor -> Bits.xnor(a, b, ws)
                ProgBinOp.Shl -> Bits.shl(a, b.toInt(), ws)
                ProgBinOp.ShrLogical -> Bits.shrLogical(a, b.toInt(), ws)
                ProgBinOp.ShrArith -> Bits.shrArithmetic(a, b.toInt(), ws)
                ProgBinOp.Add -> Bits.add(a, b, ws)
                ProgBinOp.Sub -> Bits.sub(a, b, ws)
                ProgBinOp.Mul -> Bits.mul(a, b, ws)
                ProgBinOp.Div -> Bits.div(a, b, ws)
                ProgBinOp.Mod -> Bits.rem(a, b, ws)
            }
            state.copy(entry = r, acc = r, error = null)
        } catch (e: ProgArithmeticException) {
            state.copy(error = ProgError.DivByZero, freshEntry = true)
        }
    }
}
```

For shift ops the shift amount is the *entry* (`b`); `b.toInt()` is safe because `Bits.shl/shr*` clamp `n >= bits` to a full shift.

### 3.4 Wire into `CalculatorReducer` and state

- `CalcMode` → `enum class CalcMode { Standard, Programmer }`.
- `CalculatorState` → add `val programmer: ProgrammerState = ProgrammerState()`.
- `CalculatorReducer` constructor → add `private val progReducer: ProgrammerReducer = ProgrammerReducer()`.
- Add one branch:

```kotlin
is CalculatorIntent.Programmer -> {
    val next = progReducer.reduce(state.programmer, intent.intent)
    val fx = buildList {
        add(CalculatorEffect.Haptic)
        if (next.error != null && state.programmer.error == null) add(CalculatorEffect.ErrorBlip)
    }
    Reduction(state.copy(programmer = next), fx)
}
```

- `SwitchMode` branch already exists and needs no change.
- Add a display helper in `CalculatorState.kt`:

```kotlin
fun ProgrammerState.displayValue(): String =
    if (error == ProgError.DivByZero) "Can't divide by 0"
    else com.example.calc.domain.RadixFormatter.format(entry, radix, wordSize)
```

The `CalculatorViewModel` needs **no change** — it already dispatches any `CalculatorIntent` through the reducer and drains effects.

---

## 4. UI layer

### 4.1 Keypad reuse — data-driven, radix-aware enablement

`Key`/`KeyPad`/`CalculatorKeypad` are reused. Two small, backward-compatible additions:

1. `Key` gains `val enabled: Boolean = true` — but because enablement is *dynamic* (depends on current radix), the programmer keypad is produced by a function and `CalculatorKeypad` learns to render disabled keys.

`keypad/Key.kt`:
```kotlin
enum class KeyStyle { Number, Operator, Function, Accent, Equals, Toggle, ToggleActive }

data class Key(
    val label: String,
    val style: KeyStyle,
    val intent: CalculatorIntent,
    val span: Int = 1,
    val enabled: Boolean = true,
)
```

`CalculatorKeypad.kt` — `KeyButton` gains an `enabled` param: when false, lower alpha and skip the click/haptic. Add color mappings for `Toggle` (unselected radix/word tab) and `ToggleActive` (selected). Existing Standard keypad passes `enabled = true` by default — no behavioural change.

2. `keypad/ProgrammerKeyPad.kt` builds the grid from the current `ProgrammerState`:

```kotlin
fun programmerKeyPad(state: ProgrammerState): KeyPad {
    fun prog(intent: ProgIntent) = CalculatorIntent.Programmer(intent)
    fun digit(label: String, v: Int) =
        Key(label, KeyStyle.Number, prog(ProgIntent.Digit(v)),
            enabled = state.radix.allowsDigit(v))
    fun radixTab(r: Radix) =
        Key(r.label, if (state.radix == r) KeyStyle.ToggleActive else KeyStyle.Toggle,
            prog(ProgIntent.SetRadix(r)))
    fun wsTab(w: WordSize) =
        Key(w.label, if (state.wordSize == w) KeyStyle.ToggleActive else KeyStyle.Toggle,
            prog(ProgIntent.SetWordSize(w)))

    return listOf(
        listOf(radixTab(Radix.HEX), radixTab(Radix.DEC), radixTab(Radix.OCT), radixTab(Radix.BIN)),
        listOf(wsTab(WordSize.BYTE), wsTab(WordSize.WORD), wsTab(WordSize.DWORD), wsTab(WordSize.QWORD)),
        listOf(
            Key("AND", KeyStyle.Operator, prog(ProgIntent.Binary(ProgBinOp.And))),
            Key("OR",  KeyStyle.Operator, prog(ProgIntent.Binary(ProgBinOp.Or))),
            Key("XOR", KeyStyle.Operator, prog(ProgIntent.Binary(ProgBinOp.Xor))),
            Key("NOT", KeyStyle.Operator, prog(ProgIntent.Unary(ProgUnaryOp.Not))),
        ),
        listOf(
            Key("NAND", KeyStyle.Operator, prog(ProgIntent.Binary(ProgBinOp.Nand))),
            Key("NOR",  KeyStyle.Operator, prog(ProgIntent.Binary(ProgBinOp.Nor))),
            Key("XNOR", KeyStyle.Operator, prog(ProgIntent.Binary(ProgBinOp.Xnor))),
            Key("NEG",  KeyStyle.Operator, prog(ProgIntent.Unary(ProgUnaryOp.Negate))),
        ),
        listOf(
            Key("<<", KeyStyle.Operator, prog(ProgIntent.Binary(ProgBinOp.Shl))),
            Key(">>", KeyStyle.Operator, prog(ProgIntent.Binary(ProgBinOp.ShrLogical))),
            Key(">>>", KeyStyle.Operator, prog(ProgIntent.Binary(ProgBinOp.ShrArith))),
            Key("÷",  KeyStyle.Operator, prog(ProgIntent.Binary(ProgBinOp.Div))),
        ),
        listOf(digit("D", 13), digit("E", 14), digit("F", 15),
            Key("×", KeyStyle.Operator, prog(ProgIntent.Binary(ProgBinOp.Mul)))),
        listOf(digit("A", 10), digit("B", 11), digit("C", 12),
            Key("−", KeyStyle.Operator, prog(ProgIntent.Binary(ProgBinOp.Sub)))),
        listOf(digit("7", 7), digit("8", 8), digit("9", 9),
            Key("+", KeyStyle.Operator, prog(ProgIntent.Binary(ProgBinOp.Add)))),
        listOf(digit("4", 4), digit("5", 5), digit("6", 6),
            Key("C", KeyStyle.Accent, prog(ProgIntent.Clear))),
        listOf(digit("1", 1), digit("2", 2), digit("3", 3),
            Key("⌫", KeyStyle.Function, prog(ProgIntent.Delete))),
        listOf(
            Key("0", KeyStyle.Number, prog(ProgIntent.Digit(0)), span = 2,
                enabled = state.radix.allowsDigit(0)),
            Key("=", KeyStyle.Equals, prog(ProgIntent.Equals), span = 2),
        ),
    )
}
```

In BIN only `0`/`1` render enabled; in OCT `0–7`; in DEC `0–9`; A–F enabled only in HEX — driven entirely by `radix.allowsDigit`.

### 4.2 Multi-radix display — `ui/ProgrammerDisplay.kt`

A four-row panel showing HEX / DEC / OCT / BIN simultaneously, each row `RADIX  value`, active radix highlighted, reusing the existing horizontally-scrolling single-line pattern from `CalculatorDisplay`:

```kotlin
@Composable
fun ProgrammerDisplay(state: ProgrammerState, modifier: Modifier = Modifier) {
    val values = if (state.error != null) null else RadixFormatter.all(state.entry, state.wordSize)
    Column(modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
        Text("${state.wordSize.label} · two's complement",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), fontSize = 12.sp)
        for (r in Radix.entries) {
            RadixRow(
                label = r.label,
                value = if (state.error != null) "—" else values!![r]!!,
                active = r == state.radix,
            )
        }
        if (state.error != null) Text(state.displayValue(),
            color = MaterialTheme.colorScheme.error, fontSize = 20.sp)
    }
}
```

`RadixRow` reuses a right-pinned `horizontalScroll` line (extract the private `ScrollingLine` from `CalculatorDisplay` into a small shared `ui/ScrollingLine.kt` so both displays use it — a light refactor, no behaviour change).

### 4.3 `CalculatorScreen.kt` — mode switch

Add a segmented control (Standard / Programmer) at the top row next to the history button, and branch on `state.mode`:

```kotlin
when (state.mode) {
    CalcMode.Standard -> {
        CalculatorDisplay(state, onSwipeDown = { vm.dispatch(CalculatorIntent.ShowHistory) },
            modifier = Modifier.weight(1f))
        CalculatorKeypad(pad = standardKeyPad, onKey = { vm.dispatch(it) })
    }
    CalcMode.Programmer -> {
        ProgrammerDisplay(state.programmer, modifier = Modifier.weight(1f))
        CalculatorKeypad(pad = programmerKeyPad(state.programmer), onKey = { vm.dispatch(it) })
    }
}
```

The mode toggle dispatches `CalculatorIntent.SwitchMode(...)`. Programmer mode has more rows/denser keys — reduce key `aspectRatio` (or make it configurable per keypad) so 11 rows fit; that is the only styling adjustment to `KeyButton`.

---

## 5. Task-by-task plan (strict TDD — test first, then implement, `commonTest` on JVM)

Each task: **(a)** write failing tests, **(b)** implement, **(c)** `./gradlew :composeApp:testDebugUnitTest` green, **(d)** commit.

| # | Task | New/changed files | Test file |
|---|------|-------------------|-----------|
| P1 | `WordSize`, `Radix` enums + `allowsDigit` | `domain/WordSize.kt`, `domain/Radix.kt` | `domain/RadixTest.kt` |
| P2 | `Bits`: mask + signed (sign-extension) | `domain/Bits.kt` (partial) | `domain/BitsMaskTest.kt` |
| P3 | `Bits`: AND/OR/XOR/NOT/NAND/NOR/XNOR/NEG | `domain/Bits.kt` | `domain/BitsBitwiseTest.kt` |
| P4 | `Bits`: shifts (shl / shrLogical / shrArithmetic) incl. edge counts | `domain/Bits.kt` | `domain/BitsShiftTest.kt` |
| P5 | `Bits`: arithmetic add/sub/mul/div/rem + div-by-zero throw | `domain/Bits.kt` | `domain/BitsArithTest.kt` |
| P6 | `Bits`: appendDigit / dropDigit | `domain/Bits.kt` | `domain/BitsEntryTest.kt` |
| P7 | `RadixFormatter` (all four radixes, binary grouping, signed DEC) | `domain/RadixFormatter.kt` | `domain/RadixFormatterTest.kt` |
| P8 | `ProgrammerState`, `ProgIntent`, `ProgBinOp/ProgUnaryOp` | `presentation/ProgrammerState.kt`, `presentation/ProgrammerIntent.kt` | — |
| P9 | `ProgrammerReducer` (digits, radix guard, backspace, clear) | `presentation/ProgrammerReducer.kt` (partial) | `presentation/ProgrammerReducerEntryTest.kt` |
| P10 | `ProgrammerReducer` (binary ops, chaining, equals, unary) | `presentation/ProgrammerReducer.kt` | `presentation/ProgrammerReducerOpsTest.kt` |
| P11 | `ProgrammerReducer` (word-size re-mask, radix switch, div-by-zero → error) | `presentation/ProgrammerReducer.kt` | `presentation/ProgrammerReducerModeTest.kt` |
| P12 | Wire into `CalcMode`, `CalculatorState`, `CalculatorReducer` branch + effect | `presentation/CalculatorState.kt`, `CalculatorIntent.kt`, `CalculatorReducer.kt` | `presentation/CalculatorReducerProgrammerTest.kt` |
| P13 | Keypad enablement (`Key.enabled`, `KeyButton`, styles) + `programmerKeyPad` | `keypad/Key.kt`, `keypad/CalculatorKeypad.kt`, `keypad/ProgrammerKeyPad.kt` | `keypad/ProgrammerKeyPadTest.kt` (radix→enablement) |
| P14 | `ProgrammerDisplay` + shared `ScrollingLine` refactor | `ui/ProgrammerDisplay.kt`, `ui/ScrollingLine.kt`, `ui/CalculatorDisplay.kt` | — (visual) |
| P15 | `CalculatorScreen` mode segmented control + branch | `ui/CalculatorScreen.kt` | — |
| P16 | Green builds: `assembleDebug` + `linkDebugFrameworkIosSimulatorArm64`; on-screen Android verification (screenshots light/dark, drive HEX↔BIN↔DEC, AND/XOR, shift, word-size truncation) | — | manual |

---

## 6. Unit tests — concrete inputs → expected

### P1 `RadixTest`
- `Radix.BIN.allowsDigit(0)` → true; `allowsDigit(1)` → true; `allowsDigit(2)` → false.
- `Radix.OCT.allowsDigit(7)` → true; `allowsDigit(8)` → false.
- `Radix.DEC.allowsDigit(9)` → true; `allowsDigit(10)` → false.
- `Radix.HEX.allowsDigit(15)` → true; `allowsDigit(16)` → false.
- `WordSize.DWORD.bits` → 32.

### P2 `BitsMaskTest`
- `mask(0x1FF, BYTE)` → `0xFF`.
- `mask(0x1FFFF, WORD)` → `0xFFFF`.
- `mask(-1L, BYTE)` → `0xFF` (255).
- `mask(-1L, QWORD)` → `-1L` (identity).
- `signed(0xFF, BYTE)` → `-1L`.
- `signed(0x80, BYTE)` → `-128L`.
- `signed(0x7F, BYTE)` → `127L`.
- `signed(0xFFFF, WORD)` → `-1L`.
- `signed(0x8000_0000, DWORD)` → `-2147483648L`.
- `signed(0x7F, QWORD)` → `127L`.

### P3 `BitsBitwiseTest` (ws = BYTE unless noted)
- `and(0xF0, 0x0F, BYTE)` → `0x00`.
- `and(0xFF, 0x3C, BYTE)` → `0x3C`.
- `or(0xF0, 0x0F, BYTE)` → `0xFF`.
- `xor(0xFF, 0x0F, BYTE)` → `0xF0`.
- `not(0x00, BYTE)` → `0xFF`; `not(0xFF, BYTE)` → `0x00`; `not(0x0F, BYTE)` → `0xF0`.
- `not(0x00, QWORD)` → `-1L`.
- `nand(0xFF, 0xFF, BYTE)` → `0x00`; `nand(0xF0, 0x0F, BYTE)` → `0xFF`.
- `nor(0x00, 0x00, BYTE)` → `0xFF`; `nor(0xF0, 0x0F, BYTE)` → `0x00`.
- `xnor(0xFF, 0x0F, BYTE)` → `0x0F`; `xnor(0xAA, 0xAA, BYTE)` → `0xFF`.
- `negate(0x01, BYTE)` → `0xFF` (i.e. signed -1).
- `negate(0x00, BYTE)` → `0x00`.
- `negate(0x80, BYTE)` → `0x80` (min-value overflow stays itself).
- `negate(0x01, QWORD)` → `-1L`.

### P4 `BitsShiftTest`
- `shl(0x01, 4, BYTE)` → `0x10`.
- `shl(0x0F, 4, BYTE)` → `0xF0`.
- `shl(0x0F, 8, BYTE)` → `0x00` (n == bits → 0).
- `shl(0x01, 0, BYTE)` → `0x01`.
- `shrLogical(0xF0, 4, BYTE)` → `0x0F`.
- `shrLogical(0xFF, 1, BYTE)` → `0x7F` (zero-fill).
- `shrLogical(0x80, 1, BYTE)` → `0x40`.
- `shrLogical(-1L, 1, QWORD)` → `Long.MAX_VALUE` (0x7FFF…FF) — verifies `ushr` on QWORD bit-63.
- `shrLogical(0xFF, 8, BYTE)` → `0x00`.
- `shrArithmetic(0xF0, 4, BYTE)` → `0xFF` (sign-fill: 0xF0 is negative in BYTE).
- `shrArithmetic(0x70, 4, BYTE)` → `0x07` (positive, zero-fill).
- `shrArithmetic(0x80, 1, BYTE)` → `0xC0`.
- `shrArithmetic(0xFF, 8, BYTE)` → `0xFF` (n ≥ bits, negative → all ones).
- `shrArithmetic(0x40, 8, BYTE)` → `0x00` (n ≥ bits, positive → 0).
- `shrArithmetic(-1L, 5, QWORD)` → `-1L`.

### P5 `BitsArithTest`
- `add(0xFF, 0x01, BYTE)` → `0x00` (wrap).
- `add(0x7F, 0x01, BYTE)` → `0x80` (i.e. signed 127+1 = -128 wrap).
- `sub(0x00, 0x01, BYTE)` → `0xFF`.
- `mul(0x10, 0x10, BYTE)` → `0x00` (256 mod 256).
- `mul(0x10, 0x10, WORD)` → `0x0100`.
- `div(signed -8 as 0xF8, 0x03, BYTE)` → `mask(-2)` = `0xFE` (signed truncation toward zero).
- `div(0x64, 0x0A, BYTE)` → `0x0A` (100/10).
- `rem(0x64, 0x07, BYTE)` → `0x02` (100 % 7).
- `assertFailsWith<ProgArithmeticException> { div(0x05, 0x00, BYTE) }`.

### P6 `BitsEntryTest`
- Build `FF` in HEX/QWORD: `appendDigit(appendDigit(0, 15, HEX, QWORD), 15, HEX, QWORD)` → `0xFF`.
- Build `FFFFFFFFFFFFFFFF` in HEX/QWORD (fold 16 F's) → `-1L` (no overflow crash).
- `appendDigit(0x0F, 1, HEX, BYTE)` → `0xF1`; then `appendDigit(..., 1, HEX, BYTE)` → masks to `0x11` (BYTE overflow of building third nibble).
- `appendDigit(1, 0, BIN, BYTE)` → `0b10` = `0x02`.
- `dropDigit(0xF1, HEX, BYTE)` → `0x0F`.
- `dropDigit(-1L, HEX, QWORD)` → `0x0FFFFFFFFFFFFFFF` (ULong divide by 16).
- `dropDigit(0x02, BIN, BYTE)` → `0x01`.

### P7 `RadixFormatterTest`
- `format(0xFF, HEX, BYTE)` → `"FF"`.
- `format(0xFF, DEC, BYTE)` → `"-1"` (signed).
- `format(0xFF, OCT, BYTE)` → `"377"`.
- `format(0xFF, BIN, BYTE)` → `"1111 1111"`.
- `format(0x7B, BIN, BYTE)` → `"111 1011"` (nibble grouping, no leading zero pad).
- `format(0x00, BIN, BYTE)` → `"0"`.
- `format(-1L, HEX, QWORD)` → `"FFFFFFFFFFFFFFFF"`.
- `format(-1L, DEC, QWORD)` → `"-1"`.
- `format(0x80, DEC, BYTE)` → `"-128"`.
- `format(0x7FFF, DEC, WORD)` → `"32767"`.
- `all(0x0A, DWORD)` → `{HEX="A", DEC="10", OCT="12", BIN="1010"}`.

### P9 `ProgrammerReducerEntryTest`
- Digit 5 then 3 in DEC → `entry` = 53; `displayValue()` = "53".
- In BIN, digit `2` (value 2) → no-op (entry unchanged, still 0).
- In DEC, digit `A` (value 10) → no-op.
- In HEX, digits A, F → `entry` = 0xAF; DEC-format would be signed but HEX shows "AF".
- Fresh entry: after a Binary op, next digit replaces (freshEntry true) — enter 5, `+`, 3 → entry 3 not 53.
- Delete: enter FF (HEX), Delete → entry 0x0F.
- Clear: enter something in WORD/HEX, Clear → entry 0, acc 0, pending null, but wordSize=WORD and radix=HEX preserved.

### P10 `ProgrammerReducerOpsTest` (QWORD/HEX unless noted)
- 0xF0 AND 0x0F = → Equals → entry 0x00.
- 0xC AND 0xA = → 0x8.
- 0xF0 OR 0x0F → 0xFF.
- 0xFF XOR 0x0F → 0xF0.
- 5 `+` 3 `=` → 8 (arithmetic in DEC).
- Chaining without Equals: 0xF0 AND 0x0F OR 0xFF → after second op the AND folds first (result 0x00), then OR pending; Equals with 0xFF → 0xFF.
- Unary NOT on 0x00 (BYTE) → 0xFF immediately (no Equals).
- Unary NEG on entry 1 (BYTE) → 0xFF.
- Shift: entry 0x01, `<<`, entry 4, `=` → 0x10.
- Shift: entry 0xFF (BYTE), `>>` (logical), 1, `=` → 0x7F; with `>>>` (arith) → 0xFF.
- Equals with no pending op → no change.
- Repeated ops: 2 `+` 3 `=` (→5) then `+` 10 `=` → 15 (acc carries).

### P11 `ProgrammerReducerModeTest`
- Word-size truncation: enter 0x1FF in WORD, SetWordSize(BYTE) → entry 0xFF.
- Word-size widening keeps value: 0xFF in BYTE, SetWordSize(WORD) → entry 0xFF (255, unsigned preserved as low bits).
- Sign reinterpretation on display: entry 0xFF; in BYTE `format DEC` = "-1"; SetWordSize(WORD) → DEC "255" (same bits, wider word).
- SetRadix leaves entry bits unchanged: entry from DEC 255, SetRadix(HEX) → entry unchanged, HEX shows "FF".
- Div by zero: 5 `÷` 0 `=` → `error == ProgError.DivByZero`, and the reduction from the wrapper emits `ErrorBlip`.
- After error, a Digit starts fresh and clears error.

### P12 `CalculatorReducerProgrammerTest`
- `CalculatorReducer.reduce(state, CalculatorIntent.Programmer(ProgIntent.Digit(7)))` updates `state.programmer.entry` to 7 and leaves Standard `input` untouched.
- Programmer div-by-zero reduction includes `CalculatorEffect.ErrorBlip` and `CalculatorEffect.Haptic`.
- `SwitchMode(Programmer)` sets `mode` without touching `programmer`.
- Standard intents still behave exactly as before (regression: run one existing Standard assertion through the same reducer).

### P13 `ProgrammerKeyPadTest`
- In `programmerKeyPad(ProgrammerState(radix = BIN))`: the key labelled "2" (if present) / "A".."F" are `enabled == false`; "0","1" enabled.
- In HEX: "A".."F" enabled.
- In OCT: "8","9" disabled, "7" enabled.
- Active radix tab has `KeyStyle.ToggleActive`; others `Toggle`.
- Active word-size tab has `ToggleActive`.

---

## 7. Edge cases explicitly covered

- **QWORD identity / sign bit:** `mask` and `signed` are identity for QWORD; logical vs arithmetic right shift differ on bit-63 (`ushr` vs `shr`) — tested (`shrLogical(-1, 1, QWORD)`).
- **Kotlin shift-count-mod-64 trap:** `shl/shr/ushr` on `Long` use only the low 6 bits of the count, so a shift by 64 would be a no-op; `Bits` guards every shift with `n >= bits → full-shift result` before delegating. Tested at `n == bits`.
- **Two's-complement min-value negate overflow:** `negate(0x80, BYTE) == 0x80`. Tested.
- **Entry building overflow at QWORD:** `appendDigit` relies on mod-2⁶⁴ wrap; building `FFFFFFFFFFFFFFFF` must not throw. Tested.
- **Signed division truncation toward zero** with negative operands. Tested.
- **Binary display grouping** with no zero-padding and correct nibble spacing. Tested.
- **Radix switch preserves bits; word-size switch re-masks (may truncate).** Tested.

---

## 8. Build & verification gates (unchanged commands)

- `./gradlew :composeApp:testDebugUnitTest` — all new + existing tests green.
- `./gradlew :composeApp:assembleDebug` — Android green.
- `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64` — iOS compile/link green (all new code is `commonMain`, no `expect/actual` — Programmer mode needs no platform API).
- On-screen Android: switch to Programmer, verify the four-radix panel updates live, type in HEX then flip to BIN (digits disable correctly), run `AND`/`XOR`, a `<<` shift, and a word-size truncation (`0x1FF` WORD → BYTE `0xFF`); capture light+dark screenshots.

---

### Summary of files

**New (domain):** `WordSize.kt`, `Radix.kt`, `Bits.kt`, `RadixFormatter.kt`
**New (presentation):** `ProgrammerState.kt`, `ProgrammerIntent.kt`, `ProgrammerReducer.kt`
**New (ui):** `ProgrammerDisplay.kt`, `ui/ScrollingLine.kt`, `keypad/ProgrammerKeyPad.kt`
**Changed:** `CalculatorState.kt` (+`programmer` field, `CalcMode.Programmer`, `displayValue()`), `CalculatorIntent.kt` (+`Programmer` wrapper), `CalculatorReducer.kt` (+one delegating branch), `keypad/Key.kt` (+`enabled`, toggle styles), `keypad/CalculatorKeypad.kt` (enabled rendering), `ui/CalculatorScreen.kt` (mode segmented control + branch), `ui/CalculatorDisplay.kt` (extract shared `ScrollingLine`).
**New tests (commonTest):** `domain/RadixTest.kt`, `BitsMaskTest.kt`, `BitsBitwiseTest.kt`, `BitsShiftTest.kt`, `BitsArithTest.kt`, `BitsEntryTest.kt`, `RadixFormatterTest.kt`; `presentation/ProgrammerReducerEntryTest.kt`, `ProgrammerReducerOpsTest.kt`, `ProgrammerReducerModeTest.kt`, `CalculatorReducerProgrammerTest.kt`; `keypad/ProgrammerKeyPadTest.kt`.

**No new dependencies. `CalculatorViewModel` unchanged. Standard mode untouched.**