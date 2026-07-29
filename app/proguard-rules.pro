# 크래시 스택트레이스 가독성: 라인 번호 유지, 원본 파일명은 숨김
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- kotlinx.serialization ---
# Retrofit 컨버터가 런타임에 serializer(KType) 리플렉션 조회를 하므로
# @Serializable 클래스(core/network DTO, core/navigation Route)의
# 생성 serializer/Companion 이 제거·리네임되면 API 파싱과 type-safe 네비게이션이 전부 깨진다.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keep,includedescriptorclasses class com.makeus.mody.**$$serializer { *; }
-keepclassmembers class com.makeus.mody.** {
    *** Companion;
}
-keepclasseswithmembers class com.makeus.mody.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# @Serializable 붙은 클래스는 필드까지 통째로 보존 (직렬화 이름 = 필드 이름)
-if @kotlinx.serialization.Serializable class **
-keep class <1> { *; }

# --- Compose type-safe Navigation ---
# Route 인자로 쓰는 enum(GroupEntrySource, TermsType 등)은 @Serializable 이 안 붙어 있어
# 위 규칙에서 누락된다. 네비게이션은 런타임에 이 클래스를 FQN(serialName)으로 조회하므로
# 난독화되면 "Cannot find class ... GroupEntrySource" 로 앱이 죽는다. navigation 패키지 통째 보존.
-keep class com.makeus.mody.core.navigation.** { *; }

# --- Kakao SDK (공식 가이드 규칙) ---
-keep class com.kakao.sdk.**.model.* { <fields>; }
-keep class * extends com.google.gson.TypeAdapter
