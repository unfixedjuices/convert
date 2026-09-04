package com.github.unfixedjuices.convert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.selectAll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.rememberKeyboardOptions
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightBottomBar
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightScrollView
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextInputEditor
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class ConvertMode { Main, PickFrom, PickTo, EnterAmount }

data class ConvertUiState(
    val category: Category,
    val units: List<ConvertUnit>,
    val fromIndex: Int = 0,
    val toIndex: Int = 1,
    val amountText: String = "1",
    val mode: ConvertMode = ConvertMode.Main,
) {
    val from: ConvertUnit get() = units[fromIndex]
    val to: ConvertUnit get() = units[toIndex]
    val amount: Double? get() = Units.parse(amountText)
    val result: String
        get() = amount?.let { Units.format(Units.convert(it, from, to)) } ?: "–"
}

class ConvertViewModel(category: Category) : LightViewModel<Unit>() {
    private val _uiState = MutableStateFlow(
        ConvertUiState(category = category, units = Units.of(category)),
    )
    val uiState: StateFlow<ConvertUiState> = _uiState.asStateFlow()

    fun pickFrom() = _uiState.update { it.copy(mode = ConvertMode.PickFrom) }
    fun pickTo() = _uiState.update { it.copy(mode = ConvertMode.PickTo) }
    fun enterAmount() = _uiState.update { it.copy(mode = ConvertMode.EnterAmount) }
    fun closeSub() = _uiState.update { it.copy(mode = ConvertMode.Main) }

    fun select(index: Int) = _uiState.update {
        when (it.mode) {
            ConvertMode.PickFrom -> it.copy(fromIndex = index, mode = ConvertMode.Main)
            ConvertMode.PickTo -> it.copy(toIndex = index, mode = ConvertMode.Main)
            else -> it
        }
    }

    fun swap() = _uiState.update { it.copy(fromIndex = it.toIndex, toIndex = it.fromIndex) }

    fun setAmount(text: CharSequence) = _uiState.update {
        val cleaned = text.toString().trim()
        it.copy(amountText = cleaned.ifEmpty { "0" }, mode = ConvertMode.Main)
    }

    override fun onBackPressed(): Boolean {
        if (_uiState.value.mode == ConvertMode.Main) return false
        closeSub()
        return true
    }
}

class ConvertScreen(
    sealedActivity: SealedLightActivity,
    private val category: Category,
) : LightScreen<Unit, ConvertViewModel>(sealedActivity) {

    override val viewModelClass: Class<ConvertViewModel>
        get() = ConvertViewModel::class.java

    override fun createViewModel(): ConvertViewModel = ConvertViewModel(category)

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()
        val state by viewModel.uiState.collectAsState()
        val textFieldState = rememberTextFieldState(state.amountText)
        val keyboardOptionsFlow = rememberKeyboardOptions()
        LaunchedEffect(state.mode) {
            if (state.mode == ConvertMode.EnterAmount) {
                textFieldState.edit {
                    replace(0, length, state.amountText)
                    selectAll()
                }
            }
        }
        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background),
            ) {
                when (state.mode) {
                    ConvertMode.Main -> MainContent(
                        state = state,
                        onBack = { goBack(Unit) },
                        onPickFrom = viewModel::pickFrom,
                        onPickTo = viewModel::pickTo,
                        onEnterAmount = viewModel::enterAmount,
                        onSwap = viewModel::swap,
                    )
                    ConvertMode.PickFrom, ConvertMode.PickTo -> PickContent(
                        title = if (state.mode == ConvertMode.PickFrom) "From" else "To",
                        units = state.units,
                        selectedIndex = if (state.mode == ConvertMode.PickFrom) state.fromIndex else state.toIndex,
                        onSelect = viewModel::select,
                        onBack = viewModel::closeSub,
                    )
                    ConvertMode.EnterAmount -> LightTextInputEditor(
                        title = "Amount",
                        state = textFieldState,
                        onSubmit = viewModel::setAmount,
                        onBack = viewModel::closeSub,
                        keyboardOptionsFlow = keyboardOptionsFlow,
                        submitLabel = "DONE",
                        singleLine = true,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.MainContent(
    state: ConvertUiState,
    onBack: () -> Unit,
    onPickFrom: () -> Unit,
    onPickTo: () -> Unit,
    onEnterAmount: () -> Unit,
    onSwap: () -> Unit,
) {
    LightTopBar(
        leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = onBack, contentDescription = "Back"),
        center = LightTopBarCenter.Text(state.category.label),
    )
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(horizontal = 1f.gridUnitsAsDp()),
    ) {
        Spacer(modifier = Modifier.height(1f.gridUnitsAsDp()))
        LightText(text = "FROM", variant = LightTextVariant.Detail, lighten = true)
        ValueText(
            text = state.amountText,
            modifier = Modifier
                .fillMaxWidth()
                .lightClickable(onClick = onEnterAmount)
                .padding(vertical = 0.25f.gridUnitsAsDp()),
        )
        LightText(
            text = "${state.from.name} · ${state.from.symbol}",
            variant = LightTextVariant.Copy,
            modifier = Modifier
                .fillMaxWidth()
                .lightClickable(onClick = onPickFrom)
                .padding(bottom = 1.5f.gridUnitsAsDp()),
            underline = true,
        )
        LightText(text = "TO", variant = LightTextVariant.Detail, lighten = true)
        ValueText(
            text = state.result,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.25f.gridUnitsAsDp()),
        )
        LightText(
            text = "${state.to.name} · ${state.to.symbol}",
            variant = LightTextVariant.Copy,
            modifier = Modifier
                .fillMaxWidth()
                .lightClickable(onClick = onPickTo),
            underline = true,
        )
    }
    LightBottomBar(
        items = listOf(
            LightBarButton.Text(text = "AMOUNT", onClick = onEnterAmount),
            LightBarButton.Text(text = "SWAP", onClick = onSwap),
        ),
    )
}

/** Title size while it fits on one line; Heading once it would wrap. */
@Composable
private fun ValueText(text: String, modifier: Modifier = Modifier) {
    LightText(
        text = text,
        variant = if (text.length <= 7) LightTextVariant.Title else LightTextVariant.Heading,
        modifier = modifier,
    )
}

@Composable
private fun ColumnScope.PickContent(
    title: String,
    units: List<ConvertUnit>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onBack: () -> Unit,
) {
    LightTopBar(
        leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = onBack, contentDescription = "Back"),
        center = LightTopBarCenter.Text(title),
        modifier = Modifier.padding(bottom = 0.5f.gridUnitsAsDp()),
    )
    LightScrollView(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(start = 1f.gridUnitsAsDp()),
    ) {
        units.forEachIndexed { index, unit ->
            LightText(
                text = "${unit.name}  ${unit.symbol}",
                variant = LightTextVariant.Copy,
                underline = index == selectedIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .lightClickable { onSelect(index) }
                    .padding(vertical = 0.5f.gridUnitsAsDp()),
            )
        }
    }
}
