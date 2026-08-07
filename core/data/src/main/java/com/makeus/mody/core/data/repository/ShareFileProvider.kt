package com.makeus.mody.core.data.repository

import androidx.core.content.FileProvider
import com.makeus.mody.core.data.R

/**
 * 공유용 이미지 전용 FileProvider.
 *
 * `androidx.core.content.FileProvider` 를 그대로 등록하면 다른 모듈의 provider 와
 * android:name 이 같아 매니페스트 병합이 충돌한다. 모듈마다 서브클래스를 둬서 이름을 분리한다.
 *
 * 생성자에 경로 리소스를 넘겨도 매니페스트의 `FILE_PROVIDER_PATHS` meta-data 는 여전히 필요하다
 * — 정적 `getUriForFile()` 은 provider 인스턴스를 거치지 않고 meta-data 로 경로 전략을 만든다.
 */
class ShareFileProvider : FileProvider(R.xml.share_file_paths)
