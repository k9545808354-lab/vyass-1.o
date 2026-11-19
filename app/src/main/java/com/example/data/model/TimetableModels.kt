package com.example.data.model

import androidx.compose.runtime.Immutable

enum class DivisionId(val code: String) {
    E("E"),
    F("F"),
    G("G");

    companion object {
        fun fromCode(code: String): DivisionId =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: E
    }
}

@Immutable
data class DivisionInfo(
    val id: DivisionId,
    val title: String,
    val subtitle: String,
    val classRoom: String,
    val wefDate: String
)

enum class BatchAccent {
    CYAN,
    GREEN,
    PURPLE
}

@Immutable
data class BatchEntry(
    val batchLabel: String,
    val subjectCode: String?,
    val displayLabel: String,
    val room: String? = null,
    val accent: BatchAccent = BatchAccent.CYAN
)

sealed interface TimetableCell {
    val colSpan: Int

    @Immutable
    data class Lecture(
        val subjectCode: String,
        val faculty: String,
        val isPpsHighlight: Boolean = false,
        val isWplHighlight: Boolean = false,
        val isGuardianHour: Boolean = false,
        val clickable: Boolean = true,
        override val colSpan: Int = 1
    ) : TimetableCell

    @Immutable
    data class BatchSplit(
        val batches: List<BatchEntry>,
        val accent: BatchAccent,
        override val colSpan: Int = 2
    ) : TimetableCell

    @Immutable
    data class Empty(
        override val colSpan: Int = 1
    ) : TimetableCell
}

@Immutable
data class DaySchedule(
    val day: String,
    val preLunchCells: List<TimetableCell>,
    val postLunchCells: List<TimetableCell>
)

@Immutable
data class CodeSnippet(
    val id: String,
    val label: String,
    val code: String
)

@Immutable
data class PdfNoteItem(
    val id: String,
    val name: String,
    val subject: String,
    val unit: String,
    val localFileName: String? = null
)
