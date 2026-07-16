package com.example.calc.ui.convert

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calc.domain.convert.UnitCategory
import com.example.calc.domain.convert.UnitDef
import com.example.calc.domain.convert.currency.RatesSource
import com.example.calc.presentation.convert.ConverterField
import com.example.calc.presentation.convert.ConverterIntent
import com.example.calc.presentation.convert.ConverterState
import com.example.calc.ui.ScrollingLine
import com.example.calc.ui.keypad.CalculatorKeypad
import com.example.calc.ui.keypad.converterKeyPad

@Composable
fun ConverterBody(
    state: ConverterState,
    onIntent: (ConverterIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.Bottom) {
        Column(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp)) {
            CategorySelector(
                selected = state.category,
                onSelect = { onIntent(ConverterIntent.SelectCategory(it)) },
                modifier = Modifier.padding(vertical = 12.dp),
            )
            Spacer(Modifier.weight(1f))
            ValueRow(
                value = state.textA.ifEmpty { "0" },
                unit = state.unitA,
                units = state.category.units,
                active = state.editing == ConverterField.A,
                onSelectField = { onIntent(ConverterIntent.SelectField(ConverterField.A)) },
                onSelectUnit = { onIntent(ConverterIntent.SelectUnit(ConverterField.A, it)) },
            )
            SwapButton(onClick = { onIntent(ConverterIntent.SwapUnits) })
            ValueRow(
                value = state.textB.ifEmpty { "0" },
                unit = state.unitB,
                units = state.category.units,
                active = state.editing == ConverterField.B,
                onSelectField = { onIntent(ConverterIntent.SelectField(ConverterField.B)) },
                onSelectUnit = { onIntent(ConverterIntent.SelectUnit(ConverterField.B, it)) },
            )
            if (state.category == UnitCategory.Currency) {
                RatesFootnote(state.ratesSource)
            }
            Spacer(Modifier.weight(1f))
        }
        CalculatorKeypad(
            pad = converterKeyPad(signAllowed = state.category == UnitCategory.Temperature),
            onKey = onIntent,
            keyAspect = 1.2f,
        )
    }
}

@Composable
private fun ValueRow(
    value: String,
    unit: UnitDef,
    units: List<UnitDef>,
    active: Boolean,
    onSelectField: () -> Unit,
    onSelectUnit: (UnitDef) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) scheme.primary.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onSelectField)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ScrollingLine(
            text = value,
            fontSizeSp = 40,
            color = if (active) scheme.onBackground else scheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
        )
        Spacer(Modifier.width(8.dp))
        UnitDropdown(unit = unit, units = units, onSelect = onSelectUnit)
    }
}

@Composable
private fun UnitDropdown(unit: UnitDef, units: List<UnitDef>, onSelect: (UnitDef) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(scheme.surfaceVariant)
                .clickable { expanded = true }.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(unit.symbol, color = scheme.onSurfaceVariant, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(" ▾", color = scheme.onSurfaceVariant, fontSize = 12.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            for (u in units) {
                DropdownMenuItem(
                    text = { Text("${u.symbol} — ${u.name}") },
                    onClick = { onSelect(u); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun SwapButton(onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(scheme.secondaryContainer)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text("⇅", color = scheme.onSecondaryContainer, fontSize = 20.sp)
        }
    }
}

@Composable
private fun RatesFootnote(source: RatesSource?) {
    val label = when (source) {
        RatesSource.LIVE -> "Live rates"
        RatesSource.CACHED -> "Cached rates"
        RatesSource.BUNDLED -> "Offline rates"
        null -> "Loading rates…"
    }
    Text(
        text = "$label · Rates by ExchangeRate-API",
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        fontSize = 11.sp,
        modifier = Modifier.padding(top = 8.dp, start = 8.dp),
    )
}
