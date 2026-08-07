package com.makeus.mody.core.domain.repository

/** 원격 이미지를 다른 앱에 넘길 수 있는 형태로 준비한다. */
interface ImageShareRepository {
    /**
     * [imageUrl] 을 앱 캐시로 내려받고 공유 가능한 `content://` URI 를 돌려준다.
     * 원격 URL 을 그대로 공유 인텐트에 넣으면 받는 앱이 읽지 못하는 경우가 많아 파일로 내린다.
     *
     * @param fileNameBase 확장자 앞 파일명. 같은 이름은 덮어쓴다.
     */
    suspend fun downloadForSharing(imageUrl: String, fileNameBase: String): String
}
