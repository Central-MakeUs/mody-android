package com.makeus.mody.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import java.util.regex.Pattern
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 앱 시작 경로의 baseline profile 을 만든다.
 *
 * 콜드 스타트 → 첫 화면(스플래시 게이트 → 로그인 또는 메인 탭)이 그려질 때까지가 대상이다.
 * 로그인 여부에 따라 첫 화면이 갈려 화면 조작은 넣지 않는다 — 시작 경로(Application·Hilt·MainActivity·
 * Compose 초기화)가 시작 시간의 대부분이다.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(
        packageName = TARGET_PACKAGE,
        includeInStartupProfile = true,
    ) {
        pressHome()
        startActivityAndWait()
        // startActivityAndWait 는 첫 프레임에서 돌아오는데, 그 프레임은 스플래시 게이트·시작 경로가
        // 정해지기 전의 빈 화면일 수 있다. 첫 화면이 실제로 그려질 때까지 기다려야 그 코드가 프로필에 담긴다.
        val firstScreenShown = device.wait(Until.hasObject(FIRST_SCREEN), FIRST_SCREEN_TIMEOUT_MS)
        check(firstScreenShown) { "첫 화면(로그인 또는 메인 탭)이 ${FIRST_SCREEN_TIMEOUT_MS}ms 안에 뜨지 않았다" }
    }

    private companion object {
        /** 로그인 화면 로고(contentDescription "MODY") 또는 메인 하단 탭 아이콘("피드"). 로그인 여부와 무관하게 잡힌다. */
        val FIRST_SCREEN = By.pkg(TARGET_PACKAGE).desc(Pattern.compile("MODY|피드"))
        const val FIRST_SCREEN_TIMEOUT_MS = 10_000L
    }
}
