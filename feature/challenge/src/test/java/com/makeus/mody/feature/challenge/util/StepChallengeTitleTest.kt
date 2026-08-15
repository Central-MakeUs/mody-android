package com.makeus.mody.feature.challenge.util

import org.junit.Assert.assertEquals
import org.junit.Test

class StepChallengeTitleTest {

    /** 서버가 실제로 내려주는 6개 기본 챌린지. */
    @Test
    fun `출발지-도착지를 문장으로 바꾼다`() {
        assertEquals("서울에서 인천까지", stepChallengeTitle("서울-인천"))
        assertEquals("서울에서 천안까지", stepChallengeTitle("서울-천안"))
        assertEquals("서울에서 대전까지", stepChallengeTitle("서울-대전"))
        assertEquals("서울에서 대구까지", stepChallengeTitle("서울-대구"))
        assertEquals("서울에서 부산까지", stepChallengeTitle("서울-부산"))
        assertEquals("서울에서 제주까지", stepChallengeTitle("서울-제주"))
    }

    /** 출발지가 서울 밖으로 확장돼도 규칙은 같다. */
    @Test
    fun `출발지가 서울이 아니어도 된다`() {
        assertEquals("부산에서 제주까지", stepChallengeTitle("부산-제주"))
    }

    @Test
    fun `구분자 주변 공백은 지운다`() {
        assertEquals("서울에서 인천까지", stepChallengeTitle("서울 - 인천"))
        assertEquals("서울에서 인천까지", stepChallengeTitle(" 서울-인천 "))
    }

    // --- 형식이 다르면 건드리지 않는다 ---
    //
    // 잘못 쪼개서 이상한 문장을 만드는 것보다 원문을 그대로 보여주는 쪽이 낫다.
    // 서버가 형식을 바꿔도 화면이 깨지지 않고, 원인이 화면에 그대로 보인다.

    @Test
    fun `하이픈이 없으면 원문 그대로다`() {
        assertEquals("수원까지 걸어가기", stepChallengeTitle("수원까지 걸어가기"))
        assertEquals("서울에서 인천까지", stepChallengeTitle("서울에서 인천까지"))
    }

    @Test
    fun `하이픈이 둘 이상이면 원문 그대로다`() {
        // 지역명 자체에 하이픈이 들어가면 어디서 끊어야 할지 알 수 없다.
        assertEquals("서울-인천-부산", stepChallengeTitle("서울-인천-부산"))
    }

    @Test
    fun `한쪽이 비어 있으면 원문 그대로다`() {
        assertEquals("서울-", stepChallengeTitle("서울-"))
        assertEquals("-인천", stepChallengeTitle("-인천"))
        assertEquals("서울-   ", stepChallengeTitle("서울-   "))
    }

    @Test
    fun `빈 문자열은 그대로다`() {
        assertEquals("", stepChallengeTitle(""))
    }
}
