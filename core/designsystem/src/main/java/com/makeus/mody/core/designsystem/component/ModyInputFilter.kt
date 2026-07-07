package com.makeus.mody.core.designsystem.component

/**
 * 텍스트필드 입력 정제 프리셋. 이모지/특수문자 차단용.
 *
 * 한글 IME 조합 중에는 완성형(가-힣) 외에 자모(ㄱ-ㅎ, ㅏ-ㅣ)가 잠깐 들어오므로
 * 이들을 허용 집합에 포함해야 한글 입력이 끊기지 않는다.
 */
object ModyInputFilter {

    // 한글(완성형+자모) · 영문 · 숫자만 허용. 그 외(이모지/특수문자/공백)는 제거.
    private val HANGUL_ALPHANUMERIC = Regex("[^0-9A-Za-z가-힣ㄱ-ㅎㅏ-ㅣ]")

    // 영문 대문자 · 숫자만 허용(초대 코드 등).
    private val UPPER_ALPHANUMERIC = Regex("[^0-9A-Z]")

    /** 한글/영문/숫자만 남긴다. */
    fun hangulAlphaNumeric(input: String): String = input.replace(HANGUL_ALPHANUMERIC, "")

    /** 영대문자/숫자만 남긴다(입력을 대문자로 정규화 후 필터). */
    fun upperAlphaNumeric(input: String): String =
        input.uppercase().replace(UPPER_ALPHANUMERIC, "")
}
