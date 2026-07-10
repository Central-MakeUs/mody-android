package com.makeus.mody.feature.auth.social

/** 사용자가 소셜 로그인 UI를 취소했을 때. 에러 아님 → 화면에서 조용히 처리. */
class SocialLoginCancelledException : Exception("소셜 로그인이 취소되었습니다.")
