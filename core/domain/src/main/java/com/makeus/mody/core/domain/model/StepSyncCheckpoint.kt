package com.makeus.mody.core.domain.model

/**
 * 걸음 수 동기화가 어디까지 끝났는지. 기기 로컬 기록이다.
 *
 * 이게 없으면 앱/탭 진입마다 카운트 시작 시각 이후의 **모든 날짜**를 다시 읽고 다시 올린다.
 * 챌린지 30일차면 진입 한 번에 Health Connect 읽기 30회 + 서버 PUT 30회다.
 *
 * 서버 업로드가 idempotent 라 결과는 같지만 비용만 커진다. 이미 끝난 과거 날짜는 건너뛴다.
 */
data class StepSyncCheckpoint(
    /** 어느 그룹의 챌린지였는지. 그룹이 바뀌면 체크포인트를 쓰지 않는다. */
    val groupId: Long,
    /**
     * 그때의 카운트 기준 시각(epoch ms) — 서버가 준 `fetchFromAt`.
     *
     * 챌린지를 바꾸면 이 값이 리셋 시각으로 바뀐다. 달라졌다는 건 세는 기준이 달라졌다는
     * 뜻이라 체크포인트를 버리고 처음부터 다시 센다.
     *
     * 읽기 창(30일) 하한으로 자르기 **전** 값을 쓴다. 자른 값은 날짜가 바뀔 때마다 같이
     * 밀려서, 그걸 기준으로 삼으면 30일 넘는 챌린지에서 매일 체크포인트가 무효가 된다.
     */
    val anchorEpochMs: Long,
    /** 이 날짜까지 업로드가 끝났다(그날 포함). ISO_LOCAL_DATE("2026-08-10"). */
    val syncedThrough: String,
)
