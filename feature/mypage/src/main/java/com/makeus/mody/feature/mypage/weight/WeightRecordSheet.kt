package com.makeus.mody.feature.mypage.weight

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.makeus.mody.core.designsystem.R
import com.makeus.mody.core.designsystem.component.ModyBottomSheet
import com.makeus.mody.core.designsystem.component.ModyButton
import com.makeus.mody.core.designsystem.component.ModyButtonVariant
import com.makeus.mody.core.designsystem.component.ModyTextField
import com.makeus.mody.core.designsystem.component.WheelPicker
import com.makeus.mody.core.designsystem.theme.ModyTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** 체중 휠 범위(서버 허용 20~300, 정수 단위). */
private val WEIGHTS = (20..300).toList()
private val DISPLAY_FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd")
/** yyyy.MM.dd 형식(2자리 월/일) 강제. */
private val DATE_REGEX = Regex("""\d{4}\.\d{2}\.\d{2}""")

/** 표시 문자열(yyyy.MM.dd) → LocalDate. 형식/실제 날짜 아니면 null. */
private fun parseDate(text: String): LocalDate? {
    if (!DATE_REGEX.matches(text)) return null
    return runCatching { LocalDate.parse(text.replace('.', '-')) }.getOrNull()
}

/**
 * 체중 기록 바텀시트. 날짜(형식 검증) + 현재 체중(휠) 입력 → "기록 완료".
 * @param initialWeightKg 휠 기본 선택값(현재 체중). 없으면 호출 측에서 기본값 지정.
 * @param onConfirm (recordedOn=ISO yyyy-MM-dd, weightKg)
 */
@Composable
fun WeightRecordSheet(
    initialWeightKg: Int,
    isSaving: Boolean,
    onConfirm: (recordedOn: String, weightKg: Double) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = remember { LocalDate.now() }
    var dateText by remember { mutableStateOf(today.format(DISPLAY_FMT)) }
    var weight by remember { mutableIntStateOf(initialWeightKg.coerceIn(WEIGHTS.first(), WEIGHTS.last())) }
    val parsed = remember(dateText) { parseDate(dateText) }
    val dateInvalid = dateText.isNotEmpty() && parsed == null

    // 내용(날짜+휠+버튼)이 화면 절반보다 길어 반만 펼치면 버튼이 접힘 → 항상 완전 확장.
    ModyBottomSheet(onDismissRequest = onDismiss, skipPartiallyExpanded = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
        ) {
            FieldLabel("날짜")
            DateField(value = dateText, invalid = dateInvalid, onValueChange = { dateText = it })

            Spacer(modifier = Modifier.height(24.dp))
            FieldLabel("현재 체중")
            WeightWheel(value = weight, onChange = { weight = it })

            Spacer(modifier = Modifier.height(24.dp))
            ModyButton(
                text = "기록 완료",
                onClick = { parsed?.let { onConfirm(it.toString(), weight.toDouble()) } },
                variant = ModyButtonVariant.Primary,
                enabled = parsed != null && !isSaving,
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = ModyTheme.typography.b7,
        color = ModyTheme.colors.gray08,
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
private fun DateField(value: String, invalid: Boolean, onValueChange: (String) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val borderColor = when {
        invalid -> ModyTheme.colors.error
        focused -> ModyTheme.colors.primary100
        else -> ModyTheme.colors.gray02
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        ModyTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = "2026.01.01",
            interactionSource = interactionSource,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            // 숫자와 점만 허용(yyyy.MM.dd).
            inputFilter = { raw -> raw.filter { it.isDigit() || it == '.' } },
            alertIcon = if (invalid) R.drawable.ic_alert_filled else null,
            trailingIcon = if (value.isNotEmpty()) R.drawable.ic_clear else null,
            onTrailingIconClick = { onValueChange("") },
            trailingIconContentDescription = "입력 지우기",
        )
    }
    if (invalid) {
        Spacer(modifier = Modifier.height(9.dp))
        Text(
            text = "날짜 형식이 맞지 않아요",
            style = ModyTheme.typography.c1,
            color = ModyTheme.colors.error,
        )
    }
}

/** 체중 휠. 온보딩 WeightScreen과 동일 패턴(좌측 투명 kg balancer로 선택박스 정중앙). */
@Composable
private fun WeightWheel(value: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KgUnit(modifier = Modifier.alpha(0f))
        Spacer(modifier = Modifier.width(4.dp))
        WheelPicker(
            items = WEIGHTS,
            selectedIndex = remember(value) { WEIGHTS.indexOf(value).coerceAtLeast(0) },
            onSelectedChange = { onChange(WEIGHTS[it]) },
            modifier = Modifier.width(72.dp),
            itemHeight = 36.dp,
            fillItemWidth = true,
            label = { "$it" },
        )
        Spacer(modifier = Modifier.width(4.dp))
        KgUnit()
    }
}

@Composable
private fun KgUnit(modifier: Modifier = Modifier) {
    Text(
        text = "kg",
        style = ModyTheme.typography.b7,
        color = ModyTheme.colors.gray04,
        modifier = modifier,
    )
}
