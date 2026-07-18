package com.makeus.mody.feature.record.health.contract

/** 운동 종류 프리셋. [ETC] 는 사용자가 직접 입력. */
enum class ExerciseType(val label: String) {
    HEALTH("헬스"),
    RUNNING("러닝"),
    PILATES("필라테스"),
    SWIMMING("수영"),
    YOGA("요가"),
    ETC("기타"),
}
