package com.makeus.mody.core.data.cache

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.makeus.mody.core.domain.model.ExerciseSchedule
import com.makeus.mody.core.domain.model.MealSchedule
import com.makeus.mody.core.domain.model.MealType
import com.makeus.mody.core.domain.model.NotificationSettings
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 알림 설정 로컬 캐시(DataStore). 진입 시 서버 GET 스피너를 없애기 위한 stale-while-revalidate 용.
 * 서버가 source of truth 이므로 캐시는 hint — 화면은 캐시를 즉시 보여주고 백그라운드로 서버값과 재동기화한다.
 *
 * 스키마가 작고 고정이라 kotlinx.serialization 없이 Preferences 키로 직접 인코딩한다.
 *  - 끼니: "TYPE:hour:minute:skipped" 를 '|' 로 연결
 *  - 운동: "dayOfWeek:hour:minute" 를 '|' 로 연결
 */
@Singleton
class NotificationSettingsCache @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        // "캐시 없음" 과 "전부 false" 구분용 플래그.
        val HAS = booleanPreferencesKey("noti_has_cache")
        val RECORD_REMINDER = booleanPreferencesKey("noti_record_reminder")
        val COMMENT = booleanPreferencesKey("noti_comment")
        val CHALLENGE = booleanPreferencesKey("noti_challenge")
        val MEALS = stringPreferencesKey("noti_meals")
        val EXERCISES = stringPreferencesKey("noti_exercises")
    }

    /** 캐시된 설정. 아직 없으면 null. */
    suspend fun read(): NotificationSettings? {
        val p = dataStore.data.first()
        if (p[Keys.HAS] != true) return null
        return NotificationSettings(
            recordReminderEnabled = p[Keys.RECORD_REMINDER] ?: false,
            commentNotificationEnabled = p[Keys.COMMENT] ?: false,
            challengeNotificationEnabled = p[Keys.CHALLENGE] ?: false,
            meals = decodeMeals(p[Keys.MEALS]),
            exercises = decodeExercises(p[Keys.EXERCISES]),
        )
    }

    suspend fun write(settings: NotificationSettings) {
        dataStore.edit { p ->
            p[Keys.HAS] = true
            p[Keys.RECORD_REMINDER] = settings.recordReminderEnabled
            p[Keys.COMMENT] = settings.commentNotificationEnabled
            p[Keys.CHALLENGE] = settings.challengeNotificationEnabled
            p[Keys.MEALS] = encodeMeals(settings.meals)
            p[Keys.EXERCISES] = encodeExercises(settings.exercises)
        }
    }

    private fun encodeMeals(meals: List<MealSchedule>): String =
        meals.joinToString("|") { "${it.type.name}:${it.hour}:${it.minute}:${it.skipped}" }

    private fun decodeMeals(raw: String?): List<MealSchedule> =
        raw.orEmpty().split("|").mapNotNull { token ->
            if (token.isBlank()) return@mapNotNull null
            val parts = token.split(":")
            if (parts.size < 4) return@mapNotNull null
            val type = runCatching { MealType.valueOf(parts[0]) }.getOrNull() ?: return@mapNotNull null
            val hour = parts[1].toIntOrNull() ?: return@mapNotNull null
            val minute = parts[2].toIntOrNull() ?: return@mapNotNull null
            val skipped = parts[3].toBooleanStrictOrNull() ?: return@mapNotNull null
            MealSchedule(type = type, hour = hour, minute = minute, skipped = skipped)
        }

    private fun encodeExercises(exercises: List<ExerciseSchedule>): String =
        exercises.joinToString("|") { "${it.dayOfWeek}:${it.hour}:${it.minute}" }

    private fun decodeExercises(raw: String?): List<ExerciseSchedule> =
        raw.orEmpty().split("|").mapNotNull { token ->
            if (token.isBlank()) return@mapNotNull null
            val parts = token.split(":")
            if (parts.size < 3) return@mapNotNull null
            val day = parts[0].toIntOrNull() ?: return@mapNotNull null
            val hour = parts[1].toIntOrNull() ?: return@mapNotNull null
            val minute = parts[2].toIntOrNull() ?: return@mapNotNull null
            ExerciseSchedule(dayOfWeek = day, hour = hour, minute = minute)
        }
}
