# KMP Calculator — Standard Mode Slice Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship a runnable, verified Kotlin Multiplatform + Compose Multiplatform calculator delivering Standard mode end-to-end (high-precision arithmetic, live preview, interactive history, memory, gestures, adaptive dark/light UI), built on the full production architecture so later modes slot in.

**Architecture:** Single `composeApp` KMP module, clean-architecture package layering (`domain` / `data` / `presentation` / `ui`). Domain math is a pure pipeline: `Tokenizer → shunting-yard Parser (→ RPN) → RPN evaluator` over ionspin `BigDecimal` (no `Double`). Presentation is MVI: a pure `CalculatorReducer` + a common `CalculatorViewModel` exposing `StateFlow`/`SharedFlow`. History persists via SQLDelight behind a `HistoryRepository`. UI is a data-driven adaptive keypad.

**Tech Stack:** Kotlin 2.2.20, Compose Multiplatform 1.11.1, AGP 8.13.0, Gradle 8.14.3, SQLDelight 2.3.2, ionspin bignum 0.3.10, JetBrains lifecycle 2.11.0, material3-adaptive 1.2.0, kotlinx-coroutines 1.10.2. JDK 17. compileSdk/targetSdk 36, minSdk 24.

## Global Constraints

- **Project root:** `calc/` (a subdirectory of the `ForFun` git repo). All commands run from `calc/`. All file paths below are relative to `calc/`.
- **Package root:** `com.example.calc`. Android namespace + applicationId: `com.example.calc`. Generated DB package: `com.example.calc.db`.
- **No `Double` in core math.** All arithmetic uses `com.ionspin.kotlin.bignum.decimal.BigDecimal`.
- **Locked versions (do not bump; all resolvable from `google()` + `mavenCentral()` + Gradle Plugin Portal):**
  Kotlin `2.2.20`, Compose-compiler plugin `2.2.20` (== Kotlin, via `version.ref = "kotlin"`), Compose Multiplatform `1.11.1`, AGP `8.13.0`, Gradle wrapper `8.14.3`, SQLDelight `2.3.2`, bignum `0.3.10`, lifecycle `2.9.6` (2.11.0 requires AGP 9.1+/compileSdk 37 — downgraded during Task 1), material3-adaptive `1.2.0`, coroutines `1.10.2`, activity-compose `1.10.1`, core-ktx `1.16.0`, compileSdk/targetSdk `36`, minSdk `24`, JDK/jvmTarget `17`.
- **Version-resolution fallback:** these versions were web-researched for mid-2026 and cross-checked, but the build resolver is the source of truth. If Gradle reports `Could not resolve <coord:version>`, drop to the nearest existing stable of that artifact and record the change in the task's commit message. Do **not** bump Kotlin above 2.2.x (SQLDelight 2.3.2 ceiling).
- **bignum API reference (verified during research; adjust to the real symbol if a call fails at test time):** construct exact decimals with `BigDecimal.parseString("0.1")`; `BigDecimal.fromInt(n)`; constants `BigDecimal.ZERO/ONE/TEN`; operators `+ - * /` (add/sub/mul are exact by default); division needs a `DecimalMode(decimalPrecision = 34L, roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)` and throws `ArithmeticException` on zero divisor (guard with `divisor.isZero()`); compare with `compareTo`/`<`/`>`/`==`, `isZero()`, `signum()`; plain string via `toStringExpanded()` (NOT `toString()`, which is scientific). Package `com.ionspin.kotlin.bignum.decimal`.
- **TDD:** every domain/presentation task writes the failing test first, watches it fail, implements minimally, watches it pass, commits.
- **Commits:** frequent, one per task minimum. Commit trailer: `Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>`. Work stays on branch `build-kmp-calculator-slice`. Do not push.
- **Verification target:** Android on-screen (emulator AVD `Medium_Phone_2_2` already exists). iOS is compile/framework-link only.

---

## File Structure

```
calc/
  settings.gradle.kts
  build.gradle.kts
  gradle.properties
  local.properties                 # gitignored; sdk.dir
  .gitignore
  gradle/libs.versions.toml
  gradle/wrapper/gradle-wrapper.{jar,properties}
  gradlew  gradlew.bat
  composeApp/
    build.gradle.kts
    src/
      commonMain/kotlin/com/example/calc/
        App.kt                                   # root composable
        domain/
          Token.kt Operator.kt CalcResult.kt CalcErrors.kt
          Tokenizer.kt Rpn.kt Parser.kt RpnEvaluator.kt
          NumberFormatter.kt MathEngine.kt
        presentation/
          CalculatorState.kt CalculatorIntent.kt CalculatorEffect.kt
          CalculatorReducer.kt CalculatorViewModel.kt
        data/
          HistoryRecord.kt HistoryRepository.kt
          SqlDelightHistoryRepository.kt DatabaseDriverFactory.kt   # expect
        ui/
          theme/Theme.kt Color.kt                # appColorScheme expect
          keypad/Key.kt StandardKeyPad.kt CalculatorKeypad.kt
          CalculatorDisplay.kt MemoryRow.kt HistorySheet.kt CalculatorScreen.kt
      commonMain/sqldelight/com/example/calc/db/History.sq
      androidMain/kotlin/com/example/calc/
        MainActivity.kt
        data/DatabaseDriverFactory.android.kt    # actual
        ui/theme/Theme.android.kt                # actual appColorScheme (dynamic color)
      androidMain/AndroidManifest.xml
      androidMain/res/values/themes.xml
      iosMain/kotlin/com/example/calc/
        MainViewController.kt
        data/DatabaseDriverFactory.ios.kt        # actual
        ui/theme/Theme.ios.kt                    # actual appColorScheme (fallback)
      commonTest/kotlin/com/example/calc/
        domain/*Test.kt  presentation/*Test.kt
        support/FakeHistoryRepository.kt
```

---

## Task 1: Project scaffold, Gradle wrapper, green Android build + iOS framework link

**Files:**
- Create: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `local.properties`, `.gitignore`, `gradle/libs.versions.toml`, `composeApp/build.gradle.kts`
- Create: `composeApp/src/androidMain/AndroidManifest.xml`, `composeApp/src/androidMain/res/values/themes.xml`, `composeApp/src/androidMain/kotlin/com/example/calc/MainActivity.kt`
- Create: `composeApp/src/commonMain/kotlin/com/example/calc/App.kt` (placeholder)
- Create: `composeApp/src/iosMain/kotlin/com/example/calc/MainViewController.kt`
- Create: `composeApp/src/commonTest/kotlin/com/example/calc/SanityTest.kt`
- Generate: `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.{jar,properties}`

**Interfaces:**
- Produces: a buildable module `:composeApp`; `@Composable fun App()` (placeholder, replaced in Task 13); an Android launcher `MainActivity`; `fun MainViewController()` for iOS.

- [ ] **Step 1: Bootstrap the Gradle wrapper (no system Gradle installed).**

Download Gradle 8.14.3 to the scratchpad and use it once to generate a pinned wrapper in the project:

```bash
cd /Users/jonahskinner/Github/ForFun/calc
SP=/private/tmp/claude-501/-Users-jonahskinner-Github-ForFun-calc/e9e9d1b9-1f53-4a71-8f54-31988e1a19dd/scratchpad
curl -fsSL https://services.gradle.org/distributions/gradle-8.14.3-bin.zip -o "$SP/gradle.zip"
mkdir -p "$SP/gradle-dist" && unzip -q -o "$SP/gradle.zip" -d "$SP/gradle-dist"
"$SP/gradle-dist/gradle-8.14.3/bin/gradle" wrapper --gradle-version 8.14.3 --distribution-type bin
./gradlew --version
```
Expected: prints `Gradle 8.14.3`. If the download 404s, pick the nearest existing 8.14.x from https://services.gradle.org/distributions/ and update the wrapper accordingly.

- [ ] **Step 2: Write `gradle/libs.versions.toml`.**

```toml
[versions]
kotlin = "2.2.20"
agp = "8.13.0"
composeMultiplatform = "1.11.1"
lifecycle = "2.9.6"
m3Adaptive = "1.2.0"
sqldelight = "2.3.2"
bignum = "0.3.10"
coroutines = "1.10.2"
activityCompose = "1.10.1"
androidxCore = "1.16.0"

[libraries]
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
androidx-lifecycle-viewmodel-compose = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-lifecycle-runtime-compose = { module = "org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose", version.ref = "lifecycle" }
compose-material3-adaptive = { module = "org.jetbrains.compose.material3.adaptive:adaptive", version.ref = "m3Adaptive" }
bignum = { module = "com.ionspin.kotlin:bignum", version.ref = "bignum" }
sqldelight-runtime = { module = "app.cash.sqldelight:runtime", version.ref = "sqldelight" }
sqldelight-coroutines-extensions = { module = "app.cash.sqldelight:coroutines-extensions", version.ref = "sqldelight" }
sqldelight-android-driver = { module = "app.cash.sqldelight:android-driver", version.ref = "sqldelight" }
sqldelight-native-driver = { module = "app.cash.sqldelight:native-driver", version.ref = "sqldelight" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activityCompose" }
androidx-core-ktx = { module = "androidx.core:core-ktx", version.ref = "androidxCore" }
kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }

[plugins]
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
androidApplication = { id = "com.android.application", version.ref = "agp" }
androidLibrary = { id = "com.android.library", version.ref = "agp" }
composeMultiplatform = { id = "org.jetbrains.compose", version.ref = "composeMultiplatform" }
composeCompiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
sqldelight = { id = "app.cash.sqldelight", version.ref = "sqldelight" }
```

- [ ] **Step 3: Write `settings.gradle.kts`.**

```kotlin
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}
rootProject.name = "calc"
include(":composeApp")
```

- [ ] **Step 4: Write root `build.gradle.kts`.**

```kotlin
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.sqldelight) apply false
}
```

- [ ] **Step 5: Write `gradle.properties`, `local.properties`, `.gitignore`.**

`gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096M -Dfile.encoding=UTF-8
org.gradle.caching=true
android.useAndroidX=true
kotlin.code.style=official
```
`local.properties` (gitignored, machine-specific):
```properties
sdk.dir=/Users/jonahskinner/Library/Android/sdk
```
`.gitignore`:
```gitignore
.gradle/
build/
local.properties
.idea/
*.iml
.DS_Store
xcuserdata/
*.xcworkspace/xcuserdata/
```

- [ ] **Step 6: Write `composeApp/build.gradle.kts`.**

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.compose.material3.adaptive)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.bignum)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines.extensions)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.example.calc.db")
        }
    }
}

android {
    namespace = "com.example.calc"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.example.calc"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
```

- [ ] **Step 7: Write Android manifest + theme + `MainActivity`.**

`composeApp/src/androidMain/AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:label="Calc"
        android:theme="@style/Theme.Calc"
        android:supportsRtl="true">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:configChanges="orientation|screenSize|keyboardHidden|screenLayout|density|uiMode">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```
`composeApp/src/androidMain/res/values/themes.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.Calc" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```
`composeApp/src/androidMain/kotlin/com/example/calc/MainActivity.kt` (placeholder body until Task 13):
```kotlin
package com.example.calc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}
```

- [ ] **Step 8: Write placeholder `App.kt` and iOS entry.**

`composeApp/src/commonMain/kotlin/com/example/calc/App.kt`:
```kotlin
package com.example.calc

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun App() {
    MaterialTheme {
        Text("Calc")
    }
}
```
`composeApp/src/iosMain/kotlin/com/example/calc/MainViewController.kt`:
```kotlin
package com.example.calc

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { App() }
```

- [ ] **Step 9: Write the sanity test.**

`composeApp/src/commonTest/kotlin/com/example/calc/SanityTest.kt`:
```kotlin
package com.example.calc

import kotlin.test.Test
import kotlin.test.assertEquals

class SanityTest {
    @Test
    fun toolchainWiredUp() {
        assertEquals(4, 2 + 2)
    }
}
```

- [ ] **Step 10: Build Android + run tests + link iOS framework.**

```bash
cd /Users/jonahskinner/Github/ForFun/calc
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:testDebugUnitTest
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```
Expected: all three `BUILD SUCCESSFUL`. The first run downloads Gradle 8.14.3 + all dependencies (slow). If a dependency fails to resolve, apply the Global Constraints fallback rule. Common first-build issues: (a) `sdk.dir` missing → fix `local.properties`; (b) Compose Compiler plugin not applied → confirm `composeCompiler` alias is in `composeApp/build.gradle.kts` plugins block.

- [ ] **Step 11: Commit.**

```bash
cd /Users/jonahskinner/Github/ForFun
git add calc && git commit -m "$(printf 'Scaffold KMP+Compose Multiplatform calc module\n\nGreen assembleDebug, commonTest, and iOS framework link.\n\nCo-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>')"
```

---

## Task 2: Domain value types (Operator, Token, CalcResult, errors)

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/example/calc/domain/Operator.kt`, `Token.kt`, `CalcErrors.kt`, `CalcResult.kt`

**Interfaces:**
- Produces:
  - `enum class Operator(val glyph: Char, val precedence: Int) { Plus, Minus, Times, Divide }`
  - `sealed interface Token { data class Num(text:String); data class Op(operator:Operator); Percent; LParen; RParen; UnaryMinus }`
  - `enum class CalcError { DivByZero, Overflow, Malformed }`
  - `class MalformedExpressionException; class DivideByZeroException`
  - `sealed interface CalcResult { data class Success(value: BigDecimal, formatted: String); data class Error(error: CalcError); data object Empty }`

These are pure declarations; no separate test (exercised by later tasks). Fold into Task 3's commit if preferred, but a standalone commit is fine.

- [ ] **Step 1: Write `Operator.kt`.**
```kotlin
package com.example.calc.domain

enum class Operator(val glyph: Char, val precedence: Int) {
    Plus('+', 1),
    Minus('−', 1),
    Times('×', 2),
    Divide('÷', 2),
}
```

- [ ] **Step 2: Write `Token.kt`.**
```kotlin
package com.example.calc.domain

sealed interface Token {
    data class Num(val text: String) : Token
    data class Op(val operator: Operator) : Token
    data object Percent : Token
    data object LParen : Token
    data object RParen : Token
    data object UnaryMinus : Token
}
```

- [ ] **Step 3: Write `CalcErrors.kt`.**
```kotlin
package com.example.calc.domain

enum class CalcError { DivByZero, Overflow, Malformed }

class MalformedExpressionException : Exception("Malformed expression")
class DivideByZeroException : Exception("Division by zero")
```

- [ ] **Step 4: Write `CalcResult.kt`.**
```kotlin
package com.example.calc.domain

import com.ionspin.kotlin.bignum.decimal.BigDecimal

sealed interface CalcResult {
    data class Success(val value: BigDecimal, val formatted: String) : CalcResult
    data class Error(val error: CalcError) : CalcResult
    data object Empty : CalcResult
}
```

- [ ] **Step 5: Compile + commit.**
```bash
cd /Users/jonahskinner/Github/ForFun/calc && ./gradlew :composeApp:compileDebugKotlinAndroid
```
Expected: `BUILD SUCCESSFUL`. Then commit `calc` (`feat: domain value types`).

---

## Task 3: Tokenizer (TDD)

**Files:**
- Test: `composeApp/src/commonTest/kotlin/com/example/calc/domain/TokenizerTest.kt`
- Create: `composeApp/src/commonMain/kotlin/com/example/calc/domain/Tokenizer.kt`

**Interfaces:**
- Consumes: `Token`, `Operator` (Task 2).
- Produces: `object Tokenizer { fun tokenize(input: String): List<Token> }`. Accepts both ASCII (`* / -`) and glyphs (`× ÷ −`). Multi-char numbers incl. `.`. Distinguishes unary minus (start, or after an operator / `(` / another unary) from binary minus. Unknown chars are skipped.

- [ ] **Step 1: Write the failing test.**
```kotlin
package com.example.calc.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class TokenizerTest {
    @Test fun tokenizesNumbersAndOperators() {
        assertEquals(
            listOf(Token.Num("12"), Token.Op(Operator.Plus), Token.Num("3.5")),
            Tokenizer.tokenize("12+3.5"),
        )
    }
    @Test fun acceptsGlyphOperators() {
        assertEquals(
            listOf(Token.Num("2"), Token.Op(Operator.Times), Token.Num("4")),
            Tokenizer.tokenize("2×4"),
        )
    }
    @Test fun detectsLeadingUnaryMinus() {
        assertEquals(listOf(Token.UnaryMinus, Token.Num("3")), Tokenizer.tokenize("−3"))
    }
    @Test fun detectsUnaryMinusAfterOperator() {
        assertEquals(
            listOf(Token.Num("2"), Token.Op(Operator.Times), Token.UnaryMinus, Token.Num("3")),
            Tokenizer.tokenize("2×−3"),
        )
    }
    @Test fun binaryMinusBetweenNumbers() {
        assertEquals(
            listOf(Token.Num("5"), Token.Op(Operator.Minus), Token.Num("2")),
            Tokenizer.tokenize("5−2"),
        )
    }
    @Test fun handlesParensAndPercent() {
        assertEquals(
            listOf(Token.LParen, Token.Num("50"), Token.Percent, Token.RParen),
            Tokenizer.tokenize("(50%)"),
        )
    }
}
```

- [ ] **Step 2: Run — expect FAIL** (`Tokenizer` unresolved).
```bash
cd /Users/jonahskinner/Github/ForFun/calc && ./gradlew :composeApp:testDebugUnitTest --tests "com.example.calc.domain.TokenizerTest"
```

- [ ] **Step 3: Implement `Tokenizer.kt`.**
```kotlin
package com.example.calc.domain

object Tokenizer {
    fun tokenize(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < input.length) {
            val c = input[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || c == '.' -> {
                    val start = i
                    while (i < input.length && (input[i].isDigit() || input[i] == '.')) i++
                    tokens += Token.Num(input.substring(start, i))
                }
                c == '(' -> { tokens += Token.LParen; i++ }
                c == ')' -> { tokens += Token.RParen; i++ }
                c == '%' -> { tokens += Token.Percent; i++ }
                else -> {
                    val op = when (c) {
                        '+' -> Operator.Plus
                        '-', '−' -> Operator.Minus
                        '*', '×' -> Operator.Times
                        '/', '÷' -> Operator.Divide
                        else -> null
                    }
                    if (op == null) { i++ } else {
                        val prev = tokens.lastOrNull()
                        val unary = op == Operator.Minus &&
                            (prev == null || prev is Token.Op || prev is Token.LParen || prev is Token.UnaryMinus)
                        tokens += if (unary) Token.UnaryMinus else Token.Op(op)
                        i++
                    }
                }
            }
        }
        return tokens
    }
}
```

- [ ] **Step 4: Run — expect PASS.** Same command as Step 2.

- [ ] **Step 5: Commit** `calc` (`feat: expression tokenizer with unary-minus detection`).

---

## Task 4: Shunting-yard parser → RPN, with context-aware percent (TDD)

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/example/calc/domain/Rpn.kt`
- Test: `composeApp/src/commonTest/kotlin/com/example/calc/domain/ParserTest.kt`
- Create: `composeApp/src/commonMain/kotlin/com/example/calc/domain/Parser.kt`

**Interfaces:**
- Consumes: `Token`, `Operator`, `MalformedExpressionException` (Task 2/3).
- Produces:
  - `sealed interface Rpn { data class Value(v: BigDecimal); data class Bin(op: Operator); data object Neg; data object PctScale; data object PctAdd }`
  - `object Parser { fun toRpn(tokens: List<Token>): List<Rpn> }` (throws `MalformedExpressionException` on bad number / unbalanced parens).
- **Percent decision (made at parse time from the pending-operator context):** walk the operator stack from the top skipping `UnaryMinus`; the first `Binary(Plus|Minus)` ⇒ emit `PctAdd`; anything else (`Times/Divide`, `LParen`, or empty) ⇒ emit `PctScale`.

- [ ] **Step 1: Write `Rpn.kt`.**
```kotlin
package com.example.calc.domain

import com.ionspin.kotlin.bignum.decimal.BigDecimal

sealed interface Rpn {
    data class Value(val v: BigDecimal) : Rpn
    data class Bin(val op: Operator) : Rpn
    data object Neg : Rpn
    data object PctScale : Rpn   // b -> b / 100
    data object PctAdd : Rpn     // (a below top, b top) -> a * b / 100 ; leaves a in place
}
```

- [ ] **Step 2: Write the failing test.** (Asserts the RPN *shape*; values via `toStringExpanded()`.)
```kotlin
package com.example.calc.domain

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ParserTest {
    private fun rpn(s: String) = Parser.toRpn(Tokenizer.tokenize(s)).joinToString(" ") {
        when (it) {
            is Rpn.Value -> it.v.toStringExpanded()
            is Rpn.Bin -> it.op.glyph.toString()
            Rpn.Neg -> "neg"; Rpn.PctScale -> "pctS"; Rpn.PctAdd -> "pctA"
        }
    }

    @Test fun precedence() = assertEquals("2 3 4 × +", rpn("2+3×4"))
    @Test fun parentheses() = assertEquals("2 3 + 4 ×", rpn("(2+3)×4"))
    @Test fun unaryMinus() = assertEquals("3 neg", rpn("−3"))
    @Test fun percentMultiplicativeIsScale() = assertEquals("200 10 pctS ×", rpn("200×10%"))
    @Test fun percentAdditiveIsPctAdd() = assertEquals("100 10 pctA +", rpn("100+10%"))
    @Test fun barePercentIsScale() = assertEquals("50 pctS", rpn("50%"))
    @Test fun unbalancedParensThrow() {
        assertFailsWith<MalformedExpressionException> { Parser.toRpn(Tokenizer.tokenize("(2+3")) }
    }
}
```

- [ ] **Step 3: Run — expect FAIL.**
```bash
cd /Users/jonahskinner/Github/ForFun/calc && ./gradlew :composeApp:testDebugUnitTest --tests "com.example.calc.domain.ParserTest"
```

- [ ] **Step 4: Implement `Parser.kt`.**
```kotlin
package com.example.calc.domain

import com.ionspin.kotlin.bignum.decimal.BigDecimal

object Parser {
    private sealed interface StackItem {
        data class Binary(val op: Operator) : StackItem
        data object Unary : StackItem
        data object LParen : StackItem
    }

    fun toRpn(tokens: List<Token>): List<Rpn> {
        val output = mutableListOf<Rpn>()
        val ops = ArrayDeque<StackItem>()

        fun emit(item: StackItem) {
            when (item) {
                is StackItem.Binary -> output += Rpn.Bin(item.op)
                StackItem.Unary -> output += Rpn.Neg
                StackItem.LParen -> throw MalformedExpressionException()
            }
        }

        for (t in tokens) {
            when (t) {
                is Token.Num -> output += Rpn.Value(parse(t.text))
                Token.UnaryMinus -> ops.addLast(StackItem.Unary) // right-assoc, binds tightest
                is Token.Op -> {
                    while (ops.isNotEmpty()) {
                        val top = ops.last()
                        val pop = when (top) {
                            StackItem.LParen -> false
                            StackItem.Unary -> true
                            is StackItem.Binary -> top.op.precedence >= t.operator.precedence
                        }
                        if (pop) emit(ops.removeLast()) else break
                    }
                    ops.addLast(StackItem.Binary(t.operator))
                }
                Token.Percent -> output += percentOp(ops)
                Token.LParen -> ops.addLast(StackItem.LParen)
                Token.RParen -> {
                    while (ops.isNotEmpty() && ops.last() != StackItem.LParen) emit(ops.removeLast())
                    if (ops.isEmpty()) throw MalformedExpressionException()
                    ops.removeLast() // discard LParen
                }
            }
        }
        while (ops.isNotEmpty()) emit(ops.removeLast())
        return output
    }

    private fun percentOp(ops: ArrayDeque<StackItem>): Rpn {
        for (k in ops.indices.reversed()) {
            when (val it = ops[k]) {
                StackItem.Unary -> continue
                is StackItem.Binary ->
                    return if (it.op == Operator.Plus || it.op == Operator.Minus) Rpn.PctAdd else Rpn.PctScale
                StackItem.LParen -> return Rpn.PctScale
            }
        }
        return Rpn.PctScale
    }

    private fun parse(text: String): BigDecimal =
        try { BigDecimal.parseString(text) } catch (e: Throwable) { throw MalformedExpressionException() }
}
```

- [ ] **Step 5: Run — expect PASS.** Then **commit** `calc` (`feat: shunting-yard parser to RPN with context-aware percent`).

---

## Task 5: RPN evaluator over BigDecimal (TDD)

**Files:**
- Test: `composeApp/src/commonTest/kotlin/com/example/calc/domain/RpnEvaluatorTest.kt`
- Create: `composeApp/src/commonMain/kotlin/com/example/calc/domain/RpnEvaluator.kt`

**Interfaces:**
- Consumes: `Rpn`, `Operator`, `DivideByZeroException`, `MalformedExpressionException`.
- Produces: `object RpnEvaluator { fun eval(rpn: List<Rpn>): BigDecimal }` (throws `DivideByZeroException` / `MalformedExpressionException`).

- [ ] **Step 1: Write the failing test.**
```kotlin
package com.example.calc.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RpnEvaluatorTest {
    private fun eval(s: String) =
        RpnEvaluator.eval(Parser.toRpn(Tokenizer.tokenize(s))).toStringExpanded()

    @Test fun floatingPointIsExact() = assertEquals("0.3", eval("0.1+0.2"))
    @Test fun precedence() = assertEquals("14", eval("2+3×4"))
    @Test fun parentheses() = assertEquals("20", eval("(2+3)×4"))
    @Test fun unaryMinus() = assertEquals("-6", eval("2×−3"))
    @Test fun percentAdditive() = assertEquals("110", eval("100+10%"))
    @Test fun percentSubtractive() = assertEquals("90", eval("100−10%"))
    @Test fun percentMultiplicative() = assertEquals("20", eval("200×10%"))
    @Test fun percentDivide() = assertEquals("2000", eval("200÷10%"))
    @Test fun division() = assertEquals("2.5", eval("5÷2"))
    @Test fun divideByZeroThrows() {
        assertFailsWith<DivideByZeroException> { RpnEvaluator.eval(Parser.toRpn(Tokenizer.tokenize("5÷0"))) }
    }
}
```

- [ ] **Step 2: Run — expect FAIL.**
```bash
cd /Users/jonahskinner/Github/ForFun/calc && ./gradlew :composeApp:testDebugUnitTest --tests "com.example.calc.domain.RpnEvaluatorTest"
```

- [ ] **Step 3: Implement `RpnEvaluator.kt`.**
```kotlin
package com.example.calc.domain

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode

object RpnEvaluator {
    private val HUNDRED = BigDecimal.fromInt(100)
    private val NEG_ONE = BigDecimal.fromInt(-1)
    private val DIV_MODE = DecimalMode(decimalPrecision = 34L, roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)

    fun eval(rpn: List<Rpn>): BigDecimal {
        val st = ArrayDeque<BigDecimal>()
        for (e in rpn) {
            when (e) {
                is Rpn.Value -> st.addLast(e.v)
                Rpn.Neg -> st.addLast(pop(st).multiply(NEG_ONE))
                Rpn.PctScale -> st.addLast(divide(pop(st), HUNDRED))
                Rpn.PctAdd -> {
                    val b = pop(st)
                    val a = st.lastOrNull() ?: throw MalformedExpressionException()
                    st.addLast(divide(a.multiply(b), HUNDRED))
                }
                is Rpn.Bin -> {
                    val b = pop(st); val a = pop(st)
                    st.addLast(
                        when (e.op) {
                            Operator.Plus -> a.add(b)
                            Operator.Minus -> a.subtract(b)
                            Operator.Times -> a.multiply(b)
                            Operator.Divide -> divide(a, b)
                        }
                    )
                }
            }
        }
        return st.singleOrNull() ?: throw MalformedExpressionException()
    }

    private fun pop(st: ArrayDeque<BigDecimal>): BigDecimal =
        st.removeLastOrNull() ?: throw MalformedExpressionException()

    private fun divide(a: BigDecimal, b: BigDecimal): BigDecimal {
        if (b.isZero()) throw DivideByZeroException()
        return a.divide(b, DIV_MODE)
    }
}
```

- [ ] **Step 4: Run — expect PASS.** If a bignum symbol differs (e.g. `add`/`multiply` names), switch to the operator form (`a + b`, `a * b`) — both are documented. Then **commit** `calc` (`feat: high-precision RPN evaluator`).

---

## Task 6: NumberFormatter + MathEngine facade with live preview (TDD)

**Files:**
- Test: `composeApp/src/commonTest/kotlin/com/example/calc/domain/NumberFormatterTest.kt`, `MathEngineTest.kt`
- Create: `composeApp/src/commonMain/kotlin/com/example/calc/domain/NumberFormatter.kt`, `MathEngine.kt`

**Interfaces:**
- Consumes: `BigDecimal`, `CalcResult`, `CalcError`, tokenizer/parser/evaluator, exceptions.
- Produces:
  - `object NumberFormatter { fun format(value: BigDecimal): String }` — thousands grouping on the integer part, trailing-zero trimming, scientific (`m.mmmE±e`) for `|int digits| > 16` or `|value| < 1e-9`. Also `fun plain(value: BigDecimal): String = value.toStringExpanded()` for comma-free re-insertion.
  - `class MathEngine { fun evaluate(expr: String): CalcResult; fun preview(expr: String): CalcResult }` — `evaluate` maps exceptions to `CalcResult.Error`; `preview` strips a trailing operator / `.` / `(`, balances unclosed `(`, then evaluates.

- [ ] **Step 1: Write `NumberFormatterTest.kt`.**
```kotlin
package com.example.calc.domain

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NumberFormatterTest {
    private fun fmt(s: String) = NumberFormatter.format(BigDecimal.parseString(s))
    @Test fun zero() = assertEquals("0", fmt("0"))
    @Test fun groupsThousands() = assertEquals("1,000,000", fmt("1000000"))
    @Test fun trimsTrailingZeros() = assertEquals("2.5", fmt("2.50"))
    @Test fun keepsFraction() = assertEquals("1,234.5", fmt("1234.5"))
    @Test fun negative() = assertEquals("-42", fmt("-42"))
    @Test fun largeGoesScientific() = assertTrue(fmt("123400000000000000").contains("E"))
    @Test fun tinyGoesScientific() = assertTrue(fmt("0.0000000123").contains("E"))
}
```

- [ ] **Step 2: Run — expect FAIL.**
```bash
cd /Users/jonahskinner/Github/ForFun/calc && ./gradlew :composeApp:testDebugUnitTest --tests "com.example.calc.domain.NumberFormatterTest"
```

- [ ] **Step 3: Implement `NumberFormatter.kt`.**
```kotlin
package com.example.calc.domain

import com.ionspin.kotlin.bignum.decimal.BigDecimal

object NumberFormatter {
    private val NEG_ONE = BigDecimal.fromInt(-1)

    fun plain(value: BigDecimal): String = value.toStringExpanded()

    fun format(value: BigDecimal): String {
        if (value.isZero()) return "0"
        val negative = value.signum() < 0
        val abs = if (negative) value.multiply(NEG_ONE) else value
        val plain = abs.toStringExpanded()
        val dot = plain.indexOf('.')
        val intPart = if (dot >= 0) plain.substring(0, dot) else plain
        val fracPart = if (dot >= 0) plain.substring(dot + 1) else ""
        val intDigits = intPart.trimStart('0').ifEmpty { "0" }
        val leadingFracZeros = if (intDigits == "0") fracPart.takeWhile { it == '0' }.length else 0
        val useSci = intDigits.length > 16 || (intDigits == "0" && fracPart.isNotEmpty() && leadingFracZeros >= 6)
        val body = if (useSci) toScientific(intPart, fracPart) else groupAndTrim(intDigits, fracPart)
        return if (negative) "-$body" else body
    }

    private fun groupAndTrim(intDigits: String, fracPart: String): String {
        val grouped = intDigits.reversed().chunked(3).joinToString(",").reversed()
        val trimmedFrac = fracPart.trimEnd('0')
        return if (trimmedFrac.isEmpty()) grouped else "$grouped.$trimmedFrac"
    }

    private fun toScientific(intPart: String, fracPart: String): String {
        val all = intPart + fracPart
        val first = all.indexOfFirst { it != '0' }
        if (first < 0) return "0"
        val exp = (intPart.length - 1) - first
        val sig = all.substring(first).trimEnd('0').ifEmpty { "0" }
        val mantissa = if (sig.length == 1) sig else "${sig[0]}.${sig.substring(1)}"
        val sign = if (exp >= 0) "+" else "-"
        val mag = if (exp >= 0) exp else -exp
        return "${mantissa}E$sign$mag"
    }
}
```

- [ ] **Step 4: Run — expect PASS.**

- [ ] **Step 5: Write `MathEngineTest.kt`.**
```kotlin
package com.example.calc.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MathEngineTest {
    private val engine = MathEngine()

    @Test fun evaluatesAndFormats() {
        val r = engine.evaluate("0.1+0.2")
        assertTrue(r is CalcResult.Success)
        assertEquals("0.3", (r as CalcResult.Success).formatted)
    }
    @Test fun blankIsEmpty() = assertEquals(CalcResult.Empty, engine.evaluate("  "))
    @Test fun divByZeroIsError() {
        val r = engine.evaluate("5÷0")
        assertEquals(CalcResult.Error(CalcError.DivByZero), r)
    }
    @Test fun previewTrimsTrailingOperator() {
        val r = engine.preview("2+3+")
        assertEquals("5", (r as CalcResult.Success).formatted)
    }
    @Test fun previewBalancesParens() {
        val r = engine.preview("(2+3")
        assertEquals("5", (r as CalcResult.Success).formatted)
    }
    @Test fun previewOfBareNumberIsSuccess() {
        assertTrue(engine.preview("7") is CalcResult.Success)
    }
}
```

- [ ] **Step 6: Run — expect FAIL** (`MathEngine` unresolved).

- [ ] **Step 7: Implement `MathEngine.kt`.**
```kotlin
package com.example.calc.domain

import com.ionspin.kotlin.bignum.decimal.BigDecimal

class MathEngine {
    private val OVERFLOW = BigDecimal.parseString("1E1000")
    private val NEG_ONE = BigDecimal.fromInt(-1)
    private val trailing = charArrayOf('+', '−', '×', '÷', '(', '.', '-', '*', '/')

    fun evaluate(expression: String): CalcResult {
        if (expression.isBlank()) return CalcResult.Empty
        return try {
            val value = RpnEvaluator.eval(Parser.toRpn(Tokenizer.tokenize(expression)))
            val abs = if (value.signum() < 0) value.multiply(NEG_ONE) else value
            if (abs > OVERFLOW) CalcResult.Error(CalcError.Overflow)
            else CalcResult.Success(value, NumberFormatter.format(value))
        } catch (e: DivideByZeroException) {
            CalcResult.Error(CalcError.DivByZero)
        } catch (e: MalformedExpressionException) {
            CalcResult.Error(CalcError.Malformed)
        } catch (e: ArithmeticException) {
            CalcResult.Error(CalcError.DivByZero)
        } catch (e: Throwable) {
            CalcResult.Error(CalcError.Malformed)
        }
    }

    fun preview(expression: String): CalcResult {
        var s = expression.trimEnd()
        while (s.isNotEmpty() && s.last() in trailing) s = s.dropLast(1)
        if (s.isBlank()) return CalcResult.Empty
        val open = s.count { it == '(' }
        val close = s.count { it == ')' }
        if (open > close) s += ")".repeat(open - close)
        return evaluate(s)
    }
}
```

- [ ] **Step 8: Run — expect PASS.** Then **commit** `calc` (`feat: number formatter + math engine with live preview`).

---

## Task 7: MVI contracts + pure CalculatorReducer (TDD)

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/example/calc/data/HistoryRecord.kt` (needed by state)
- Create: `.../presentation/CalculatorState.kt`, `CalculatorIntent.kt`, `CalculatorEffect.kt`, `CalculatorReducer.kt`
- Test: `composeApp/src/commonTest/kotlin/com/example/calc/presentation/CalculatorReducerTest.kt`

**Interfaces:**
- Consumes: `MathEngine`, `CalcResult`, `CalcError`, `NumberFormatter`, `BigDecimal`, `HistoryRecord`.
- Produces:
  - `data class HistoryRecord(id: Long, expression: String, result: String, timestamp: Long)`
  - `enum class CalcMode { Standard }`  (scaffolding for later modes)
  - `enum class MemoryAction { MC, MR, MPlus, MMinus, MS }`
  - `data class CalculatorState(input: String = "", preview: String = "", result: BigDecimal? = null, error: CalcError? = null, justEvaluated: Boolean = false, memory: BigDecimal? = null, history: List<HistoryRecord> = emptyList(), historyVisible: Boolean = false, mode: CalcMode = CalcMode.Standard)`
  - `sealed interface CalculatorIntent { Digit(d: Char); Decimal; Op(op: Operator); Percent; OpenParen; CloseParen; ToggleSign; Delete; Clear; Equals; Memory(action: MemoryAction); ShowHistory; HideHistory; InjectExpression(expr: String); InjectResult(value: String); HistoryLoaded(items: List<HistoryRecord>); SwitchMode(mode: CalcMode) }`
  - `sealed interface CalculatorEffect { Haptic; ErrorBlip; PersistHistory(expression: String, result: String) }`
  - `data class Reduction(state: CalculatorState, effects: List<CalculatorEffect> = emptyList())`
  - `class CalculatorReducer(engine: MathEngine) { fun reduce(state, intent): Reduction }`
- **Display derivation (used by UI, defined here as helpers on state):** `fun CalculatorState.displayText(): String` = error message if `error != null`, else formatted `result` if `justEvaluated`, else `input.ifEmpty { "0" }`. `fun CalculatorState.resultToInject(): String?` = `result?.let { NumberFormatter.plain(it) }`.

- [ ] **Step 1: Write `HistoryRecord.kt`.**
```kotlin
package com.example.calc.data

data class HistoryRecord(
    val id: Long,
    val expression: String,
    val result: String,
    val timestamp: Long,
)
```

- [ ] **Step 2: Write `CalculatorState.kt`, `CalculatorIntent.kt`, `CalculatorEffect.kt`.**

`CalculatorState.kt`:
```kotlin
package com.example.calc.presentation

import com.example.calc.data.HistoryRecord
import com.example.calc.domain.CalcError
import com.example.calc.domain.NumberFormatter
import com.ionspin.kotlin.bignum.decimal.BigDecimal

enum class CalcMode { Standard }

data class CalculatorState(
    val input: String = "",
    val preview: String = "",
    val result: BigDecimal? = null,
    val error: CalcError? = null,
    val justEvaluated: Boolean = false,
    val memory: BigDecimal? = null,
    val history: List<HistoryRecord> = emptyList(),
    val historyVisible: Boolean = false,
    val mode: CalcMode = CalcMode.Standard,
)

fun CalculatorState.displayText(): String = when {
    error != null -> when (error) {
        CalcError.DivByZero -> "Can't divide by 0"
        CalcError.Overflow -> "Number too large"
        CalcError.Malformed -> "Invalid expression"
    }
    justEvaluated && result != null -> NumberFormatter.format(result)
    else -> input.ifEmpty { "0" }
}

fun CalculatorState.resultToInject(): String? = result?.let { NumberFormatter.plain(it) }
```

`CalculatorIntent.kt`:
```kotlin
package com.example.calc.presentation

import com.example.calc.data.HistoryRecord
import com.example.calc.domain.Operator

enum class MemoryAction { MC, MR, MPlus, MMinus, MS }

sealed interface CalculatorIntent {
    data class Digit(val d: Char) : CalculatorIntent
    data object Decimal : CalculatorIntent
    data class Op(val op: Operator) : CalculatorIntent
    data object Percent : CalculatorIntent
    data object OpenParen : CalculatorIntent
    data object CloseParen : CalculatorIntent
    data object ToggleSign : CalculatorIntent
    data object Delete : CalculatorIntent
    data object Clear : CalculatorIntent
    data object Equals : CalculatorIntent
    data class Memory(val action: MemoryAction) : CalculatorIntent
    data object ShowHistory : CalculatorIntent
    data object HideHistory : CalculatorIntent
    data class InjectExpression(val expr: String) : CalculatorIntent
    data class InjectResult(val value: String) : CalculatorIntent
    data class HistoryLoaded(val items: List<HistoryRecord>) : CalculatorIntent
    data class SwitchMode(val mode: CalcMode) : CalculatorIntent
}
```

`CalculatorEffect.kt`:
```kotlin
package com.example.calc.presentation

sealed interface CalculatorEffect {
    data object Haptic : CalculatorEffect
    data object ErrorBlip : CalculatorEffect
    data class PersistHistory(val expression: String, val result: String) : CalculatorEffect
}

data class Reduction(
    val state: CalculatorState,
    val effects: List<CalculatorEffect> = emptyList(),
)
```

- [ ] **Step 3: Write the failing `CalculatorReducerTest.kt`.**
```kotlin
package com.example.calc.presentation

import com.example.calc.domain.CalcError
import com.example.calc.domain.MathEngine
import com.example.calc.domain.Operator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CalculatorReducerTest {
    private val reducer = CalculatorReducer(MathEngine())
    private fun run(start: CalculatorState, vararg intents: CalculatorIntent): CalculatorState {
        var s = start
        for (i in intents) s = reducer.reduce(s, i).state
        return s
    }

    @Test fun appendsDigits() {
        val s = run(CalculatorState(), CalculatorIntent.Digit('1'), CalculatorIntent.Digit('2'))
        assertEquals("12", s.input)
    }
    @Test fun livePreviewComputed() {
        val s = run(CalculatorState(), CalculatorIntent.Digit('2'), CalculatorIntent.Op(Operator.Plus), CalculatorIntent.Digit('3'))
        assertEquals("5", s.preview)
    }
    @Test fun replacesTrailingOperator() {
        val s = run(CalculatorState(input = "5+"), CalculatorIntent.Op(Operator.Times))
        assertEquals("5×", s.input)
    }
    @Test fun leadingMinusAllowedOthersIgnored() {
        assertEquals("−", run(CalculatorState(), CalculatorIntent.Op(Operator.Minus)).input)
        assertEquals("", run(CalculatorState(), CalculatorIntent.Op(Operator.Times)).input)
    }
    @Test fun decimalGuards() {
        val s = run(CalculatorState(), CalculatorIntent.Digit('5'), CalculatorIntent.Decimal, CalculatorIntent.Decimal)
        assertEquals("5.", s.input)
    }
    @Test fun equalsSetsResultAndPersists() {
        val red = reducer.reduce(CalculatorState(input = "2+3"), CalculatorIntent.Equals)
        assertTrue(red.state.justEvaluated)
        assertEquals("5", red.state.result?.toStringExpanded())
        assertTrue(red.effects.any { it is CalculatorEffect.PersistHistory && it.expression == "2+3" && it.result == "5" })
    }
    @Test fun digitAfterEqualsStartsFresh() {
        val afterEq = reducer.reduce(CalculatorState(input = "2+3"), CalculatorIntent.Equals).state
        val s = reducer.reduce(afterEq, CalculatorIntent.Digit('7')).state
        assertEquals("7", s.input)
        assertNull(s.result)
    }
    @Test fun operatorAfterEqualsContinues() {
        val afterEq = reducer.reduce(CalculatorState(input = "2+3"), CalculatorIntent.Equals).state
        val s = reducer.reduce(afterEq, CalculatorIntent.Op(Operator.Times)).state
        assertEquals("5×", s.input)
    }
    @Test fun equalsDivByZeroSetsError() {
        val s = reducer.reduce(CalculatorState(input = "5÷0"), CalculatorIntent.Equals).state
        assertEquals(CalcError.DivByZero, s.error)
        assertNull(s.result)
    }
    @Test fun clearKeepsMemory() {
        val start = CalculatorState(input = "9", memory = com.ionspin.kotlin.bignum.decimal.BigDecimal.fromInt(4))
        val s = reducer.reduce(start, CalculatorIntent.Clear).state
        assertEquals("", s.input)
        assertEquals("4", s.memory?.toStringExpanded())
    }
    @Test fun memoryStoreRecall() {
        var s = reducer.reduce(CalculatorState(input = "8"), CalculatorIntent.Memory(MemoryAction.MS)).state
        assertEquals("8", s.memory?.toStringExpanded())
        s = reducer.reduce(s.copy(input = ""), CalculatorIntent.Memory(MemoryAction.MR)).state
        assertEquals("8", s.input)
    }
    @Test fun memoryPlus() {
        var s = reducer.reduce(CalculatorState(input = "8"), CalculatorIntent.Memory(MemoryAction.MS)).state
        s = reducer.reduce(s.copy(input = "2"), CalculatorIntent.Memory(MemoryAction.MPlus)).state
        assertEquals("10", s.memory?.toStringExpanded())
    }
    @Test fun deleteRemovesLastChar() {
        assertEquals("1", reducer.reduce(CalculatorState(input = "12"), CalculatorIntent.Delete).state.input)
    }
    @Test fun injectResultStripsGroupingImplicitlyViaPlain() {
        val s = reducer.reduce(CalculatorState(), CalculatorIntent.InjectResult("1234.5")).state
        assertEquals("1234.5", s.input)
        assertTrue(!s.historyVisible)
    }
}
```

- [ ] **Step 4: Run — expect FAIL.**
```bash
cd /Users/jonahskinner/Github/ForFun/calc && ./gradlew :composeApp:testDebugUnitTest --tests "com.example.calc.presentation.CalculatorReducerTest"
```

- [ ] **Step 5: Implement `CalculatorReducer.kt`.**
```kotlin
package com.example.calc.presentation

import com.example.calc.domain.CalcResult
import com.example.calc.domain.MathEngine
import com.example.calc.domain.NumberFormatter
import com.example.calc.domain.Operator
import com.ionspin.kotlin.bignum.decimal.BigDecimal

private val OPERATOR_GLYPHS = charArrayOf('+', '−', '×', '÷')

class CalculatorReducer(private val engine: MathEngine) {

    fun reduce(state: CalculatorState, intent: CalculatorIntent): Reduction = when (intent) {
        is CalculatorIntent.Digit -> {
            val base = if (state.justEvaluated || state.error != null) "" else state.input
            fresh(state, base + intent.d)
        }
        CalculatorIntent.Decimal -> {
            val base = if (state.justEvaluated || state.error != null) "" else state.input
            val current = base.takeLastWhile { it.isDigit() || it == '.' }
            if (current.contains('.')) Reduction(state)
            else {
                val glue = base.isEmpty() || base.last() in OPERATOR_GLYPHS || base.last() == '('
                fresh(state, base + if (glue) "0." else ".")
            }
        }
        is CalculatorIntent.Op -> {
            val base = when {
                state.error != null -> ""
                state.justEvaluated && state.result != null -> state.result.toStringExpanded()
                else -> state.input
            }
            if (base.isEmpty()) {
                if (intent.op == Operator.Minus) fresh(state, "−") else Reduction(state)
            } else {
                val newBase = if (base.last() in OPERATOR_GLYPHS) base.dropLast(1) + intent.op.glyph
                else base + intent.op.glyph
                fresh(state, newBase)
            }
        }
        CalculatorIntent.Percent -> {
            val base = if (state.justEvaluated && state.result != null) state.result.toStringExpanded() else state.input
            if (base.isEmpty() || base.last() in OPERATOR_GLYPHS) Reduction(state)
            else fresh(state, base + "%")
        }
        CalculatorIntent.OpenParen -> {
            val base = if (state.justEvaluated || state.error != null) "" else state.input
            fresh(state, base + "(")
        }
        CalculatorIntent.CloseParen -> {
            val base = state.input
            val open = base.count { it == '(' }; val close = base.count { it == ')' }
            if (open <= close || base.isEmpty() || base.last() in OPERATOR_GLYPHS || base.last() == '(') Reduction(state)
            else fresh(state, base + ")")
        }
        CalculatorIntent.ToggleSign -> {
            val base = if (state.justEvaluated && state.result != null) state.result.toStringExpanded() else state.input
            if (base.isEmpty()) fresh(state, "−")
            else {
                var idx = base.length
                while (idx > 0 && (base[idx - 1].isDigit() || base[idx - 1] == '.')) idx--
                if (idx == base.length) Reduction(state)
                else {
                    val head = base.substring(0, idx); val num = base.substring(idx)
                    val newBase = if (head.endsWith("−") && (idx - 2 < 0 || !head[idx - 2].isDigit()))
                        head.dropLast(1) + num else head + "−" + num
                    fresh(state, newBase)
                }
            }
        }
        CalculatorIntent.Delete -> {
            val base = state.input
            if (base.isEmpty()) Reduction(state.copy(error = null)) else fresh(state, base.dropLast(1))
        }
        CalculatorIntent.Clear -> Reduction(CalculatorState(memory = state.memory, history = state.history))
        CalculatorIntent.Equals -> equals(state)
        is CalculatorIntent.Memory -> memory(state, intent.action)
        CalculatorIntent.ShowHistory -> Reduction(state.copy(historyVisible = true))
        CalculatorIntent.HideHistory -> Reduction(state.copy(historyVisible = false))
        is CalculatorIntent.InjectExpression -> {
            val base = if (state.justEvaluated || state.error != null) "" else state.input
            fresh(state, base + intent.expr).let { it.copy(state = it.state.copy(historyVisible = false)) }
        }
        is CalculatorIntent.InjectResult -> {
            val base = if (state.justEvaluated || state.error != null) "" else state.input
            fresh(state, base + intent.value).let { it.copy(state = it.state.copy(historyVisible = false)) }
        }
        is CalculatorIntent.HistoryLoaded -> Reduction(state.copy(history = intent.items))
        is CalculatorIntent.SwitchMode -> Reduction(state.copy(mode = intent.mode))
    }

    private fun fresh(state: CalculatorState, newInput: String): Reduction {
        val preview = when (val p = engine.preview(newInput)) {
            is CalcResult.Success -> if (hasOperator(newInput)) p.formatted else ""
            else -> ""
        }
        return Reduction(state.copy(input = newInput, preview = preview, result = null, error = null, justEvaluated = false))
    }

    private fun hasOperator(s: String): Boolean = s.any { it in OPERATOR_GLYPHS } || s.contains('%')

    private fun equals(state: CalculatorState): Reduction {
        if (state.input.isBlank()) return Reduction(state)
        return when (val r = engine.evaluate(state.input)) {
            is CalcResult.Success -> Reduction(
                state.copy(result = r.value, preview = "", error = null, justEvaluated = true),
                listOf(CalculatorEffect.Haptic, CalculatorEffect.PersistHistory(state.input, NumberFormatter.plain(r.value))),
            )
            is CalcResult.Error -> Reduction(
                state.copy(error = r.error, result = null, preview = "", justEvaluated = true),
                listOf(CalculatorEffect.ErrorBlip),
            )
            CalcResult.Empty -> Reduction(state)
        }
    }

    private fun currentValue(state: CalculatorState): BigDecimal? = when {
        state.justEvaluated && state.result != null -> state.result
        else -> (engine.evaluate(state.input) as? CalcResult.Success)?.value
    }

    private fun memory(state: CalculatorState, action: MemoryAction): Reduction = when (action) {
        MemoryAction.MC -> Reduction(state.copy(memory = null))
        MemoryAction.MS -> currentValue(state)?.let { Reduction(state.copy(memory = it)) } ?: Reduction(state)
        MemoryAction.MPlus -> currentValue(state)?.let {
            Reduction(state.copy(memory = (state.memory ?: BigDecimal.ZERO).add(it)))
        } ?: Reduction(state)
        MemoryAction.MMinus -> currentValue(state)?.let {
            Reduction(state.copy(memory = (state.memory ?: BigDecimal.ZERO).subtract(it)))
        } ?: Reduction(state)
        MemoryAction.MR -> {
            val m = state.memory ?: return Reduction(state)
            val base = if (state.justEvaluated || state.error != null) "" else state.input
            fresh(state, base + NumberFormatter.plain(m))
        }
    }
}
```

- [ ] **Step 6: Run — expect PASS.** Then **commit** `calc` (`feat: MVI contracts + pure calculator reducer`).

---

## Task 8: CalculatorViewModel wiring StateFlow/effects/repository (TDD)

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/example/calc/data/HistoryRepository.kt` (interface only in this task)
- Create: `composeApp/src/commonMain/kotlin/com/example/calc/presentation/CalculatorViewModel.kt`
- Create: `composeApp/src/commonTest/kotlin/com/example/calc/support/FakeHistoryRepository.kt`
- Test: `composeApp/src/commonTest/kotlin/com/example/calc/presentation/CalculatorViewModelTest.kt`

**Interfaces:**
- Produces:
  - `interface HistoryRepository { fun observeHistory(): Flow<List<HistoryRecord>>; suspend fun add(expression: String, result: String); suspend fun clear() }`
  - `class CalculatorViewModel(repository: HistoryRepository, engine: MathEngine = MathEngine()) : ViewModel() { val state: StateFlow<CalculatorState>; val effects: SharedFlow<CalculatorEffect>; fun dispatch(intent: CalculatorIntent) }`
- Consumes: `CalculatorReducer`, `CalculatorState/Intent/Effect`, `HistoryRepository`, `HistoryRecord`.
- Behavior: `dispatch` reduces synchronously, updates `state`; `PersistHistory` effects call `repository.add` on `viewModelScope`; other effects are emitted on `effects`. On init, collects `repository.observeHistory()` and dispatches `HistoryLoaded`.

- [ ] **Step 1: Write `HistoryRepository.kt`.**
```kotlin
package com.example.calc.data

import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun observeHistory(): Flow<List<HistoryRecord>>
    suspend fun add(expression: String, result: String)
    suspend fun clear()
}
```

- [ ] **Step 2: Write `FakeHistoryRepository.kt`.**
```kotlin
package com.example.calc.support

import com.example.calc.data.HistoryRecord
import com.example.calc.data.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeHistoryRepository : HistoryRepository {
    val flow = MutableStateFlow<List<HistoryRecord>>(emptyList())
    val added = mutableListOf<Pair<String, String>>()
    override fun observeHistory(): Flow<List<HistoryRecord>> = flow
    override suspend fun add(expression: String, result: String) {
        added += expression to result
        flow.value = flow.value + HistoryRecord(flow.value.size + 1L, expression, result, 0L)
    }
    override suspend fun clear() { flow.value = emptyList() }
}
```

- [ ] **Step 3: Write the failing `CalculatorViewModelTest.kt`.**
```kotlin
package com.example.calc.presentation

import com.example.calc.domain.Operator
import com.example.calc.support.FakeHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CalculatorViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @BeforeTest fun setup() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test fun dispatchUpdatesState() = runTest(dispatcher) {
        val vm = CalculatorViewModel(FakeHistoryRepository())
        vm.dispatch(CalculatorIntent.Digit('2'))
        vm.dispatch(CalculatorIntent.Op(Operator.Plus))
        vm.dispatch(CalculatorIntent.Digit('3'))
        assertEquals("2+3", vm.state.value.input)
        assertEquals("5", vm.state.value.preview)
    }

    @Test fun equalsPersistsHistory() = runTest(dispatcher) {
        val repo = FakeHistoryRepository()
        val vm = CalculatorViewModel(repo)
        vm.dispatch(CalculatorIntent.Digit('2'))
        vm.dispatch(CalculatorIntent.Op(Operator.Plus))
        vm.dispatch(CalculatorIntent.Digit('3'))
        vm.dispatch(CalculatorIntent.Equals)
        testScheduler.advanceUntilIdle()
        assertTrue(repo.added.contains("2+3" to "5"))
        assertEquals(1, vm.state.value.history.size)
    }
}
```

- [ ] **Step 4: Run — expect FAIL.**
```bash
cd /Users/jonahskinner/Github/ForFun/calc && ./gradlew :composeApp:testDebugUnitTest --tests "com.example.calc.presentation.CalculatorViewModelTest"
```

- [ ] **Step 5: Implement `CalculatorViewModel.kt`.**
```kotlin
package com.example.calc.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calc.data.HistoryRepository
import com.example.calc.domain.MathEngine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CalculatorViewModel(
    private val repository: HistoryRepository,
    engine: MathEngine = MathEngine(),
) : ViewModel() {

    private val reducer = CalculatorReducer(engine)
    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()
    private val _effects = MutableSharedFlow<CalculatorEffect>(extraBufferCapacity = 16)
    val effects: SharedFlow<CalculatorEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.observeHistory().collect { items ->
                _state.value = reducer.reduce(_state.value, CalculatorIntent.HistoryLoaded(items)).state
            }
        }
    }

    fun dispatch(intent: CalculatorIntent) {
        val reduction = reducer.reduce(_state.value, intent)
        _state.value = reduction.state
        for (effect in reduction.effects) {
            when (effect) {
                is CalculatorEffect.PersistHistory ->
                    viewModelScope.launch { repository.add(effect.expression, effect.result) }
                else -> _effects.tryEmit(effect)
            }
        }
    }

    fun clearHistory() { viewModelScope.launch { repository.clear() } }
}
```

- [ ] **Step 6: Run — expect PASS.** Then **commit** `calc` (`feat: calculator view model with reactive history`).

---

## Task 9: SQLDelight schema + drivers + repository implementation

**Files:**
- Create: `composeApp/src/commonMain/sqldelight/com/example/calc/db/History.sq`
- Create: `composeApp/src/commonMain/kotlin/com/example/calc/data/DatabaseDriverFactory.kt` (expect)
- Create: `composeApp/src/androidMain/kotlin/com/example/calc/data/DatabaseDriverFactory.android.kt` (actual)
- Create: `composeApp/src/iosMain/kotlin/com/example/calc/data/DatabaseDriverFactory.ios.kt` (actual)
- Create: `composeApp/src/commonMain/kotlin/com/example/calc/data/SqlDelightHistoryRepository.kt`

**Interfaces:**
- Consumes: generated `com.example.calc.db.AppDatabase` (+ `historyQueries`, row type `History`), `HistoryRepository`, `HistoryRecord`.
- Produces:
  - `expect class DatabaseDriverFactory { fun createDriver(): SqlDriver }` (+ android `actual class DatabaseDriverFactory(context: Context)`, ios `actual class DatabaseDriverFactory()`).
  - `fun createDatabase(factory: DatabaseDriverFactory): AppDatabase`
  - `class SqlDelightHistoryRepository(db: AppDatabase, dispatcher: CoroutineDispatcher = Dispatchers.Default) : HistoryRepository`
- **Testing note:** functional persistence is verified on-device in Task 14. Schema compilation is verified by the build in this task (a malformed `.sq` fails codegen). Unit tests already cover repository *consumers* via `FakeHistoryRepository`.

- [ ] **Step 1: Write `History.sq`.** (Timestamp defaulted by SQLite — no common `Clock` dependency needed.)
```sql
CREATE TABLE history (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    expression TEXT NOT NULL,
    result TEXT NOT NULL,
    timestamp INTEGER NOT NULL DEFAULT (strftime('%s','now'))
);

insertEntry:
INSERT INTO history(expression, result)
VALUES (?, ?);

selectAll:
SELECT id, expression, result, timestamp
FROM history
ORDER BY id DESC
LIMIT 100;

deleteAll:
DELETE FROM history;
```

- [ ] **Step 2: Generate SQLDelight sources + confirm the schema compiles.**
```bash
cd /Users/jonahskinner/Github/ForFun/calc && ./gradlew :composeApp:generateCommonMainAppDatabaseInterface :composeApp:compileDebugKotlinAndroid
```
Expected: `BUILD SUCCESSFUL`, generating `com.example.calc.db.AppDatabase` and `HistoryQueries`. (Task name follows `generate<SourceSet><DbName>Interface`; if it differs, run `./gradlew :composeApp:tasks --all | grep -i generate` to find it, or just build — codegen runs as a build dependency.)

- [ ] **Step 3: Write the expect factory + `createDatabase`.**

`composeApp/src/commonMain/kotlin/com/example/calc/data/DatabaseDriverFactory.kt`:
```kotlin
package com.example.calc.data

import app.cash.sqldelight.db.SqlDriver
import com.example.calc.db.AppDatabase

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

fun createDatabase(factory: DatabaseDriverFactory): AppDatabase =
    AppDatabase(factory.createDriver())
```

- [ ] **Step 4: Write the Android actual.**

`composeApp/src/androidMain/kotlin/com/example/calc/data/DatabaseDriverFactory.android.kt`:
```kotlin
package com.example.calc.data

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.calc.db.AppDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(AppDatabase.Schema, context, "calc.db")
}
```

- [ ] **Step 5: Write the iOS actual.**

`composeApp/src/iosMain/kotlin/com/example/calc/data/DatabaseDriverFactory.ios.kt`:
```kotlin
package com.example.calc.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.example.calc.db.AppDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver =
        NativeSqliteDriver(AppDatabase.Schema, "calc.db")
}
```

- [ ] **Step 6: Write `SqlDelightHistoryRepository.kt`.**
```kotlin
package com.example.calc.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.calc.db.AppDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SqlDelightHistoryRepository(
    db: AppDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : HistoryRepository {
    private val queries = db.historyQueries

    override fun observeHistory(): Flow<List<HistoryRecord>> =
        queries.selectAll().asFlow().mapToList(dispatcher).map { rows ->
            rows.map { HistoryRecord(it.id, it.expression, it.result, it.timestamp) }
        }

    override suspend fun add(expression: String, result: String) = withContext(dispatcher) {
        queries.insertEntry(expression, result)
    }

    override suspend fun clear() = withContext(dispatcher) {
        queries.deleteAll()
    }
}
```

- [ ] **Step 7: Build all targets (incl. iOS link) to confirm actuals resolve.**
```bash
cd /Users/jonahskinner/Github/ForFun/calc
./gradlew :composeApp:compileDebugKotlinAndroid
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```
Expected: both `BUILD SUCCESSFUL`. If `mapToList` needs a `CoroutineContext` not a `CoroutineDispatcher`, it already accepts a dispatcher (a `CoroutineContext`); no change needed.

- [ ] **Step 8: Commit** `calc` (`feat: SQLDelight history persistence with android+ios drivers`).

---

## Task 10: Theme — expect/actual color scheme (Material You on Android, brand fallback elsewhere)

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/example/calc/ui/theme/Color.kt`, `Theme.kt` (expect)
- Create: `composeApp/src/androidMain/kotlin/com/example/calc/ui/theme/Theme.android.kt` (actual)
- Create: `composeApp/src/iosMain/kotlin/com/example/calc/ui/theme/Theme.ios.kt` (actual)

**Interfaces:**
- Produces: `@Composable fun AppTheme(darkTheme: Boolean = isSystemInDarkTheme(), dynamicColor: Boolean = true, content: @Composable () -> Unit)`; brand `LightColors`/`DarkColors: ColorScheme`; `@Composable expect fun appColorScheme(darkTheme: Boolean, dynamicColor: Boolean): ColorScheme`.
- UI-only; no unit test. Verified visually in Task 14 (dark/light + dynamic color).

- [ ] **Step 1: Write `Color.kt`.**
```kotlin
package com.example.calc.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val BrandPrimary = Color(0xFF3B6EF6)
private val BrandOnPrimary = Color(0xFFFFFFFF)
private val BrandAccent = Color(0xFFFF8A3D)

val LightColors = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = BrandOnPrimary,
    secondary = BrandAccent,
    background = Color(0xFFF6F7FB),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE8EAF1),
    onBackground = Color(0xFF12141A),
    onSurface = Color(0xFF12141A),
)

val DarkColors = darkColorScheme(
    primary = BrandPrimary,
    onPrimary = BrandOnPrimary,
    secondary = BrandAccent,
    background = Color(0xFF0B0C10),
    surface = Color(0xFF15171E),
    surfaceVariant = Color(0xFF23262F),
    onBackground = Color(0xFFF2F3F7),
    onSurface = Color(0xFFF2F3F7),
)
```

- [ ] **Step 2: Write `Theme.kt` (common, with expect).**
```kotlin
package com.example.calc.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
expect fun appColorScheme(darkTheme: Boolean, dynamicColor: Boolean): ColorScheme

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = appColorScheme(darkTheme, dynamicColor),
        content = content,
    )
}
```

- [ ] **Step 3: Write `Theme.android.kt` (actual, dynamic color on API 31+).**
```kotlin
package com.example.calc.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun appColorScheme(darkTheme: Boolean, dynamicColor: Boolean): ColorScheme {
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val context = LocalContext.current
    return when {
        dynamicColor && supportsDynamic && darkTheme -> dynamicDarkColorScheme(context)
        dynamicColor && supportsDynamic && !darkTheme -> dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
}
```

- [ ] **Step 4: Write `Theme.ios.kt` (actual, brand fallback).**
```kotlin
package com.example.calc.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun appColorScheme(darkTheme: Boolean, dynamicColor: Boolean): ColorScheme =
    if (darkTheme) DarkColors else LightColors
```

- [ ] **Step 5: Build Android + iOS link, then commit.**
```bash
cd /Users/jonahskinner/Github/ForFun/calc && ./gradlew :composeApp:compileDebugKotlinAndroid :composeApp:linkDebugFrameworkIosSimulatorArm64
```
Expected: `BUILD SUCCESSFUL`. Commit `calc` (`feat: adaptive theme with Material You dynamic color`).

---

## Task 11: Keypad model + reusable CalculatorKeypad composable

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/example/calc/ui/keypad/Key.kt`, `StandardKeyPad.kt`, `CalculatorKeypad.kt`

**Interfaces:**
- Consumes: `CalculatorIntent`, `MemoryAction`, `Operator`.
- Produces:
  - `enum class KeyStyle { Number, Operator, Function, Accent, Equals }`
  - `data class Key(val label: String, val style: KeyStyle, val intent: CalculatorIntent, val span: Int = 1)`
  - `typealias KeyPad = List<List<Key>>`
  - `val standardKeyPad: KeyPad`
  - `@Composable fun CalculatorKeypad(pad: KeyPad, onKey: (CalculatorIntent) -> Unit, modifier: Modifier = Modifier)` — data-driven grid, per-`style` colors, weight = `span`, haptic on press via `LocalHapticFeedback`, stable `key` per cell, no per-frame allocation.
- UI-only; verified in Task 14.

- [ ] **Step 1: Write `Key.kt`.**
```kotlin
package com.example.calc.ui.keypad

import com.example.calc.presentation.CalculatorIntent

enum class KeyStyle { Number, Operator, Function, Accent, Equals }

data class Key(
    val label: String,
    val style: KeyStyle,
    val intent: CalculatorIntent,
    val span: Int = 1,
)

typealias KeyPad = List<List<Key>>
```

- [ ] **Step 2: Write `StandardKeyPad.kt`.**
```kotlin
package com.example.calc.ui.keypad

import com.example.calc.presentation.CalculatorIntent
import com.example.calc.domain.Operator

private fun digit(c: Char) = Key(c.toString(), KeyStyle.Number, CalculatorIntent.Digit(c))

val standardKeyPad: KeyPad = listOf(
    listOf(
        Key("C", KeyStyle.Accent, CalculatorIntent.Clear),
        Key("(", KeyStyle.Function, CalculatorIntent.OpenParen),
        Key(")", KeyStyle.Function, CalculatorIntent.CloseParen),
        Key("⌫", KeyStyle.Function, CalculatorIntent.Delete),
    ),
    listOf(digit('7'), digit('8'), digit('9'), Key("÷", KeyStyle.Operator, CalculatorIntent.Op(Operator.Divide))),
    listOf(digit('4'), digit('5'), digit('6'), Key("×", KeyStyle.Operator, CalculatorIntent.Op(Operator.Times))),
    listOf(digit('1'), digit('2'), digit('3'), Key("−", KeyStyle.Operator, CalculatorIntent.Op(Operator.Minus))),
    listOf(
        Key("±", KeyStyle.Function, CalculatorIntent.ToggleSign),
        digit('0'),
        Key(".", KeyStyle.Number, CalculatorIntent.Decimal),
        Key("+", KeyStyle.Operator, CalculatorIntent.Op(Operator.Plus)),
    ),
    listOf(
        Key("%", KeyStyle.Function, CalculatorIntent.Percent, span = 2),
        Key("=", KeyStyle.Equals, CalculatorIntent.Equals, span = 2),
    ),
)
```

- [ ] **Step 3: Write `CalculatorKeypad.kt`.**
```kotlin
package com.example.calc.ui.keypad

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.clickable

@Composable
fun CalculatorKeypad(
    pad: KeyPad,
    onKey: (com.example.calc.presentation.CalculatorIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in pad) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (key in row) {
                    KeyButton(
                        key = key,
                        modifier = Modifier.weight(key.span.toFloat()),
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onKey(key.intent)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyButton(key: Key, modifier: Modifier, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val (bg, fg) = when (key.style) {
        KeyStyle.Number -> scheme.surfaceVariant to scheme.onSurface
        KeyStyle.Operator -> scheme.secondaryContainer to scheme.onSecondaryContainer
        KeyStyle.Function -> scheme.surface to scheme.onSurface
        KeyStyle.Accent -> scheme.errorContainer to scheme.onErrorContainer
        KeyStyle.Equals -> scheme.primary to scheme.onPrimary
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .then(if (key.span == 1) Modifier.aspectRatio(1f) else Modifier.aspectRatio(2.1f))
            .clickable(onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = key.label,
                color = fg,
                fontSize = 26.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
```
Note: `LocalHapticFeedback` on non-Android targets is a no-op — safe in common code.

- [ ] **Step 4: Build Android + commit.**
```bash
cd /Users/jonahskinner/Github/ForFun/calc && ./gradlew :composeApp:compileDebugKotlinAndroid
```
Expected: `BUILD SUCCESSFUL`. Commit `calc` (`feat: data-driven adaptive keypad component`).

---

## Task 12: Display, memory row, and interactive history sheet

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/example/calc/ui/CalculatorDisplay.kt`, `MemoryRow.kt`, `HistorySheet.kt`

**Interfaces:**
- Consumes: `CalculatorState` + `displayText()`, `CalculatorIntent`, `MemoryAction`, `HistoryRecord`, `NumberFormatter`, `BigDecimal`.
- Produces:
  - `@Composable fun CalculatorDisplay(state, onSwipeDown, onSwipeLeft, modifier)` — right-aligned; big display line = `state.displayText()`; faint preview line = `state.preview` (only when non-empty and not just-evaluated); vertical drag down ⇒ `onSwipeDown`, horizontal drag left ⇒ `onSwipeLeft`; horizontally scrollable so long numbers never wrap.
  - `@Composable fun MemoryRow(hasMemory: Boolean, onMemory: (MemoryAction) -> Unit, modifier)`.
  - `@Composable fun HistorySheet(history: List<HistoryRecord>, onInjectExpression: (String) -> Unit, onInjectResult: (String) -> Unit, onClear: () -> Unit, onDismiss: () -> Unit)` (Material3 `ModalBottomSheet`).
- Verified in Task 14.

- [ ] **Step 1: Write `CalculatorDisplay.kt`.**
```kotlin
package com.example.calc.ui

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calc.presentation.CalculatorState
import com.example.calc.presentation.displayText

@Composable
fun CalculatorDisplay(
    state: CalculatorState,
    onSwipeDown: () -> Unit,
    onSwipeLeft: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount -> if (dragAmount > 24f) onSwipeDown() }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount -> if (dragAmount < -24f) onSwipeLeft() }
            },
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom,
    ) {
        val showPreview = state.preview.isNotEmpty() && !state.justEvaluated && state.error == null
        Text(
            text = state.displayText(),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 56.sp,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        )
        Text(
            text = if (showPreview) "= ${state.preview}" else " ",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
            fontSize = 24.sp,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        )
    }
}
```

- [ ] **Step 2: Write `MemoryRow.kt`.**
```kotlin
package com.example.calc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calc.presentation.MemoryAction

@Composable
fun MemoryRow(
    hasMemory: Boolean,
    onMemory: (MemoryAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        "MC" to MemoryAction.MC,
        "MR" to MemoryAction.MR,
        "M+" to MemoryAction.MPlus,
        "M−" to MemoryAction.MMinus,
        "MS" to MemoryAction.MS,
    )
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        for ((label, action) in items) {
            val enabled = !(action == MemoryAction.MC || action == MemoryAction.MR) || hasMemory
            TextButton(onClick = { onMemory(action) }, enabled = enabled) {
                Text(
                    text = label,
                    fontSize = 15.sp,
                    color = if (enabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                )
            }
        }
    }
}
```

- [ ] **Step 3: Write `HistorySheet.kt`.**
```kotlin
package com.example.calc.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calc.data.HistoryRecord
import com.example.calc.domain.NumberFormatter
import com.ionspin.kotlin.bignum.decimal.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(
    history: List<HistoryRecord>,
    onInjectExpression: (String) -> Unit,
    onInjectResult: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("History", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
            TextButton(onClick = onClear) { Text("Clear") }
        }
        if (history.isEmpty()) {
            Text(
                "No calculations yet",
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                items(history, key = { it.id }) { entry ->
                    val formatted = runCatching { NumberFormatter.format(BigDecimal.parseString(entry.result)) }
                        .getOrDefault(entry.result)
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            entry.expression,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.clickable { onInjectExpression(entry.expression) },
                        )
                        Text(
                            "= $formatted",
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.clickable { onInjectResult(entry.result) },
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                }
            }
        }
    }
}
```

- [ ] **Step 4: Build Android + commit.**
```bash
cd /Users/jonahskinner/Github/ForFun/calc && ./gradlew :composeApp:compileDebugKotlinAndroid
```
Expected: `BUILD SUCCESSFUL`. Commit `calc` (`feat: display, memory row, interactive history sheet`).

---

## Task 13: CalculatorScreen + App root + platform wiring

**Files:**
- Create: `composeApp/src/commonMain/kotlin/com/example/calc/ui/CalculatorScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/com/example/calc/App.kt`
- Modify: `composeApp/src/androidMain/kotlin/com/example/calc/MainActivity.kt`
- Modify: `composeApp/src/iosMain/kotlin/com/example/calc/MainViewController.kt`

**Interfaces:**
- Consumes: `CalculatorViewModel`, `CalculatorState/Intent`, keypad, display, memory row, history sheet, `AppTheme`, `HistoryRepository`, `DatabaseDriverFactory`, `createDatabase`, `SqlDelightHistoryRepository`.
- Produces: `@Composable fun CalculatorScreen(vm: CalculatorViewModel)`; `@Composable fun App(repository: HistoryRepository)`.
- Adaptive: constrain content to `widthIn(max = 560.dp)` and center on wide windows (foldable/tablet) via `currentWindowAdaptiveInfo()`; compact fills width.

- [ ] **Step 1: Write `CalculatorScreen.kt`.**
```kotlin
package com.example.calc.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calc.presentation.CalculatorIntent
import com.example.calc.presentation.CalculatorViewModel
import com.example.calc.presentation.MemoryAction
import com.example.calc.ui.keypad.CalculatorKeypad
import com.example.calc.ui.keypad.standardKeyPad

@Composable
fun CalculatorScreen(vm: CalculatorViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 560.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
            ) {
                CalculatorDisplay(
                    state = state,
                    onSwipeDown = { vm.dispatch(CalculatorIntent.ShowHistory) },
                    onSwipeLeft = { vm.dispatch(CalculatorIntent.Delete) },
                    modifier = Modifier.weight(1f, fill = true),
                )
                MemoryRow(
                    hasMemory = state.memory != null,
                    onMemory = { vm.dispatch(CalculatorIntent.Memory(it)) },
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                CalculatorKeypad(pad = standardKeyPad, onKey = { vm.dispatch(it) })
            }
        }
    }
    if (state.historyVisible) {
        HistorySheet(
            history = state.history,
            onInjectExpression = { vm.dispatch(CalculatorIntent.InjectExpression(it)) },
            onInjectResult = { vm.dispatch(CalculatorIntent.InjectResult(it)) },
            onClear = { vm.clearHistory() },
            onDismiss = { vm.dispatch(CalculatorIntent.HideHistory) },
        )
    }
}
```
Note: `MemoryAction` import kept for readability even though passed through lambda; remove if the linter flags it unused.

- [ ] **Step 2: Rewrite `App.kt`.**
```kotlin
package com.example.calc

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calc.data.HistoryRepository
import com.example.calc.presentation.CalculatorViewModel
import com.example.calc.ui.CalculatorScreen
import com.example.calc.ui.theme.AppTheme

@Composable
fun App(repository: HistoryRepository) {
    AppTheme {
        val vm = viewModel { CalculatorViewModel(repository) }
        CalculatorScreen(vm)
    }
}
```

- [ ] **Step 3: Rewrite `MainActivity.kt` (build the real repository).**
```kotlin
package com.example.calc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.calc.data.DatabaseDriverFactory
import com.example.calc.data.SqlDelightHistoryRepository
import com.example.calc.data.createDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = SqlDelightHistoryRepository(
            createDatabase(DatabaseDriverFactory(applicationContext))
        )
        setContent { App(repository) }
    }
}
```

- [ ] **Step 4: Rewrite `MainViewController.kt`.**
```kotlin
package com.example.calc

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.example.calc.data.DatabaseDriverFactory
import com.example.calc.data.SqlDelightHistoryRepository
import com.example.calc.data.createDatabase

fun MainViewController() = ComposeUIViewController {
    val repository = remember {
        SqlDelightHistoryRepository(createDatabase(DatabaseDriverFactory()))
    }
    App(repository)
}
```

- [ ] **Step 5: Build Android + link iOS + run full test suite.**
```bash
cd /Users/jonahskinner/Github/ForFun/calc
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:testDebugUnitTest
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```
Expected: all `BUILD SUCCESSFUL`; test suite green. If `enableEdgeToEdge` is unresolved, it lives in `androidx.activity:activity-compose` (already a dependency); otherwise drop the call. Commit `calc` (`feat: wire calculator screen, app root, and platform entry points`).

---

## Task 14: On-screen verification on Android emulator + screenshots

**Files:** none (verification only). Produces screenshots under the scratchpad.

**Goal:** Prove the app launches and the interactions work: digit entry, live preview, `=`, history via swipe-down + tap-to-inject, backspace via swipe-left, memory keys, dark/light.

- [ ] **Step 1: Boot the existing emulator AVD in the background.**
```bash
SDK=~/Library/Android/sdk
"$SDK/emulator/emulator" -avd Medium_Phone_2_2 -no-snapshot -no-boot-anim >/tmp/emu.log 2>&1 &
"$SDK/platform-tools/adb" wait-for-device
# Poll until fully booted:
until [ "$("$SDK/platform-tools/adb" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do sleep 3; done
"$SDK/platform-tools/adb" shell input keyevent 82   # dismiss keyguard
echo "BOOTED"
```
Run the emulator launch with `run_in_background: true`. Expected: eventually prints `BOOTED`. (Emulator boot can take several minutes.)

- [ ] **Step 2: Install and launch.**
```bash
cd /Users/jonahskinner/Github/ForFun/calc
./gradlew :composeApp:installDebug
SDK=~/Library/Android/sdk
"$SDK/platform-tools/adb" shell monkey -p com.example.calc -c android.intent.category.LAUNCHER 1
sleep 3
```
Expected: `installDebug` succeeds; the app appears.

- [ ] **Step 3: Drive the UI and screenshot each checkpoint.** Use `adb shell input tap <x> <y>` (get coordinates from a screenshot) or, more robustly, drive via keyboard-independent taps after reading the layout. Capture with:
```bash
SDK=~/Library/Android/sdk
shot() { "$SDK/platform-tools/adb" exec-out screencap -p > "$1"; }
SP=/private/tmp/claude-501/-Users-jonahskinner-Github-ForFun-calc/e9e9d1b9-1f53-4a71-8f54-31988e1a19dd/scratchpad
shot "$SP/01-launch.png"
```
Then, for each checkpoint below, tap the relevant keys and screenshot. Read each screenshot with the Read tool to confirm visually:
  1. `01-launch.png` — app shows "0".
  2. Enter `2 + 3` → confirm faint live preview shows `= 5`. Screenshot `02-preview.png`.
  3. Tap `=` → big display shows `5`. Screenshot `03-equals.png`.
  4. Swipe down on the display → history sheet opens showing `2+3 = 5`. Screenshot `04-history.png`.
  5. Tap the result in history → sheet closes, `5` injected. Screenshot `05-inject.png`.
  6. Type `÷ 0 =` → shows "Can't divide by 0". Screenshot `06-divzero.png`.
  7. Enter a few digits, swipe left on the display → last digit removed. Screenshot `07-backspace.png`.
  8. Tap `MS`, `C`, `MR` → memory recalls the stored value. Screenshot `08-memory.png`.
  9. Toggle dark mode and re-screenshot: `"$SDK/platform-tools/adb" shell "cmd uimode night yes"` → `09-dark.png`; revert with `night no`.

- [ ] **Step 4: Fix any defect found on-screen**, then rebuild/reinstall and re-verify the affected checkpoint. Loop until all nine pass. (Systematic-debugging skill for any non-obvious failure.)

- [ ] **Step 5: Send the key screenshots to the user** (`SendUserFile` with `01`,`02`,`03`,`04`,`06`,`09`) as proof, and stop the emulator if desired:
```bash
~/Library/Android/sdk/platform-tools/adb emu kill
```

- [ ] **Step 6: Final commit.**
```bash
cd /Users/jonahskinner/Github/ForFun && git add calc && git commit -m "$(printf 'Verify Standard-mode calculator on Android emulator\n\nDrove digit entry, live preview, equals, history sheet + inject,\ndivide-by-zero, swipe-backspace, memory, dark/light. Screenshots captured.\n\nCo-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>')"
```

---

## Self-Review

**Spec coverage** (spec §1–§9 → task):
- High-precision engine (no `Double`), shunting-yard, real-time validation → Tasks 2–6. ✅
- Percentage `100+10%=110` → Task 4 (`PctAdd`) + Task 5 tests. ✅
- Division-by-zero / overflow error handling → Task 5/6 (`CalcResult.Error`). ✅
- Live instant preview → Task 6 (`preview`) + Task 7 (reducer wires it) + Task 12 (faint line). ✅
- MVI state/intent/effect contracts → Task 7. ✅
- Reusable performance-oriented adaptive keypad → Task 11 + Task 13 (adaptive width). ✅
- SQLDelight history schema + repository → Task 9. ✅
- Interactive history (tap expression/result to inject) → Task 12 + reducer Task 7. ✅
- Memory MC/MR/M+/M−/MS → Task 7 (logic) + Task 12 (row). ✅
- Gestures (swipe down = history, swipe left = delete) → Task 12/13. ✅
- Adaptive dark/light + Material You → Task 10. ✅
- Android + iOS targets; Android on-screen verify, iOS compile/link → Tasks 1, 9, 10, 13, 14. ✅
- Out of scope by design: Scientific (dropped), Programmer, Unit Converter. ✅

**Placeholder scan:** the only intentional placeholder is `App.kt`/`MainActivity.kt`/`MainViewController.kt` in Task 1, explicitly replaced in Task 13. No `TODO`/`TBD` in shipped code steps. ✅

**Type consistency:** `CalculatorIntent`, `CalculatorEffect`, `Reduction`, `CalculatorState` (with `result: BigDecimal?`), `HistoryRecord(id,expression,result,timestamp)`, `HistoryRepository(observeHistory/add/clear)`, `Operator.glyph`, `Rpn.{Value,Bin,Neg,PctScale,PctAdd}`, `CalcResult.{Success(value,formatted),Error(error),Empty}`, `MemoryAction`, `Key/KeyStyle/KeyPad`, `appColorScheme`, `DatabaseDriverFactory.createDriver`, `createDatabase` — names are used identically across the tasks that define and consume them. `NumberFormatter.plain` (comma-free) is used everywhere a value re-enters `input`; `NumberFormatter.format` (grouped) only for display. ✅

**Known assumptions the build will confirm (fallback rules in Global Constraints):** exact mid-2026 artifact versions; a handful of bignum method names (operator forms available as fallback); the SQLDelight codegen task name; `enableEdgeToEdge` availability. Each has an inline fallback noted in its task.
