package com.makeus.mody.core.domain.repository

/** 이미지 업로드 공통. 로컬 이미지를 서버에 올리고 서버가 참조하는 imageKey 를 돌려준다. */
interface ImageUploadRepository {
    /**
     * 로컬 이미지 [imageUri] 를 [domain]("record"/"profile" 등)으로 업로드하고 imageKey 반환.
     * @param fileNameBase 확장자 앞 파일명(확장자는 MIME 로 결정). 예: "profile".
     */
    suspend fun uploadImage(imageUri: String, domain: String, fileNameBase: String): String
}
