package com.makeus.mody.feature.challenge.util

import androidx.core.content.FileProvider
import com.makeus.mody.feature.challenge.R

/**
 * 인증 사진 촬영 파일 전용 FileProvider.
 * 모듈마다 서브클래스를 둬야 매니페스트 병합에서 android:name 이 겹치지 않는다.
 */
class ChallengeFileProvider : FileProvider(R.xml.challenge_file_paths)
