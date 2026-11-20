package com.makeus.mody.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
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
        device.waitForIdle()
    }
}
