package com.example.data.repository

import android.content.Context
import com.example.data.model.BatchAccent
import com.example.data.model.BatchEntry
import com.example.data.model.DaySchedule
import com.example.data.model.DivisionId
import com.example.data.model.DivisionInfo
import com.example.data.model.TimetableCell
import org.json.JSONArray
import org.json.JSONObject

class TimetableRepository(context: Context) {

    private val prefs = context.getSharedPreferences("vyas_timetable_store", Context.MODE_PRIVATE)

    fun loadAllDivisions(): List<DivisionInfo> =
        DivisionId.entries.map { loadDivisionInfo(it) }

    fun loadDivisionInfo(divisionId: DivisionId): DivisionInfo {
        val defaultInfo = TimetableData.getDivisionInfo(divisionId)
        val raw = prefs.getString(infoKey(divisionId), null) ?: return defaultInfo
        return try {
            val obj = JSONObject(raw)
            val room = obj.optString("classRoom", defaultInfo.classRoom)
            val wef = obj.optString("wefDate", defaultInfo.wefDate)
            defaultInfo.copy(
                classRoom = room,
                wefDate = wef,
                subtitle = "Computer Engineering • Class Room $room"
            )
        } catch (_: Exception) {
            defaultInfo
        }
    }

    fun saveDivisionInfo(info: DivisionInfo) {
        val obj = JSONObject().apply {
            put("classRoom", info.classRoom)
            put("wefDate", info.wefDate)
        }
        prefs.edit().putString(infoKey(info.id), obj.toString()).apply()
    }

    fun loadSchedule(divisionId: DivisionId): List<DaySchedule> {
        val raw = prefs.getString(scheduleKey(divisionId), null)
            ?: return TimetableData.getSchedule(divisionId)
        return try {
            decodeSchedule(raw)
        } catch (_: Exception) {
            TimetableData.getSchedule(divisionId)
        }
    }

    fun saveSchedule(divisionId: DivisionId, schedule: List<DaySchedule>) {
        prefs.edit().putString(scheduleKey(divisionId), encodeSchedule(schedule)).apply()
    }

    fun hasCustomChanges(divisionId: DivisionId): Boolean =
        prefs.contains(scheduleKey(divisionId)) || prefs.contains(infoKey(divisionId))

    fun resetDivision(divisionId: DivisionId) {
        prefs.edit()
            .remove(scheduleKey(divisionId))
            .remove(infoKey(divisionId))
            .apply()
    }

    private fun encodeSchedule(schedule: List<DaySchedule>): String {
        val arr = JSONArray()
        schedule.forEach { day ->
            val dayObj = JSONObject().apply {
                put("day", day.day)
                put("pre", encodeCells(day.preLunchCells))
                put("post", encodeCells(day.postLunchCells))
            }
            arr.put(dayObj)
        }
        return arr.toString()
    }

    private fun encodeCells(cells: List<TimetableCell>): JSONArray {
        val arr = JSONArray()
        cells.forEach { cell ->
            val obj = JSONObject()
            obj.put("colSpan", cell.colSpan)
            when (cell) {
                is TimetableCell.Empty -> obj.put("type", "empty")
                is TimetableCell.Lecture -> {
                    obj.put("type", "lecture")
                    obj.put("subjectCode", cell.subjectCode)
                    obj.put("faculty", cell.faculty)
                    obj.put("isPpsHighlight", cell.isPpsHighlight)
                    obj.put("isWplHighlight", cell.isWplHighlight)
                    obj.put("isGuardianHour", cell.isGuardianHour)
                    obj.put("clickable", cell.clickable)
                }
                is TimetableCell.BatchSplit -> {
                    obj.put("type", "batch")
                    obj.put("accent", cell.accent.name)
                    val batchesArr = JSONArray()
                    cell.batches.forEach { b ->
                        batchesArr.put(
                            JSONObject().apply {
                                put("batchLabel", b.batchLabel)
                                put("subjectCode", b.subjectCode ?: JSONObject.NULL)
                                put("displayLabel", b.displayLabel)
                                put("room", b.room ?: JSONObject.NULL)
                                put("accent", b.accent.name)
                            }
                        )
                    }
                    obj.put("batches", batchesArr)
                }
            }
            arr.put(obj)
        }
        return arr
    }

    private fun decodeSchedule(raw: String): List<DaySchedule> {
        val arr = JSONArray(raw)
        return buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                add(
                    DaySchedule(
                        day = obj.getString("day"),
                        preLunchCells = decodeCells(obj.getJSONArray("pre")),
                        postLunchCells = decodeCells(obj.getJSONArray("post"))
                    )
                )
            }
        }
    }

    private fun decodeCells(arr: JSONArray): List<TimetableCell> = buildList {
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val colSpan = obj.optInt("colSpan", 1).coerceIn(1, 2)
            when (obj.getString("type")) {
                "empty" -> add(TimetableCell.Empty(colSpan = colSpan))
                "lecture" -> add(
                    TimetableCell.Lecture(
                        subjectCode = obj.getString("subjectCode"),
                        faculty = obj.optString("faculty", ""),
                        isPpsHighlight = obj.optBoolean("isPpsHighlight", false),
                        isWplHighlight = obj.optBoolean("isWplHighlight", false),
                        isGuardianHour = obj.optBoolean("isGuardianHour", false),
                        clickable = obj.optBoolean("clickable", true),
                        colSpan = colSpan
                    )
                )
                "batch" -> {
                    val accent = runCatching {
                        BatchAccent.valueOf(obj.optString("accent", "CYAN"))
                    }.getOrDefault(BatchAccent.CYAN)
                    val bArr = obj.getJSONArray("batches")
                    val batches = buildList {
                        for (j in 0 until bArr.length()) {
                            val bObj = bArr.getJSONObject(j)
                            val sub = if (bObj.isNull("subjectCode")) null else bObj.optString("subjectCode")
                            val room = if (bObj.isNull("room")) null else bObj.optString("room")
                            val bAccent = runCatching {
                                BatchAccent.valueOf(bObj.optString("accent", accent.name))
                            }.getOrDefault(accent)
                            add(
                                BatchEntry(
                                    batchLabel = bObj.getString("batchLabel"),
                                    subjectCode = sub?.takeIf { it.isNotBlank() },
                                    displayLabel = bObj.getString("displayLabel"),
                                    room = room?.takeIf { it.isNotBlank() },
                                    accent = bAccent
                                )
                            )
                        }
                    }
                    add(TimetableCell.BatchSplit(batches = batches, accent = accent, colSpan = colSpan))
                }
            }
        }
    }

    private fun scheduleKey(id: DivisionId) = "schedule_${id.code}"
    private fun infoKey(id: DivisionId) = "info_${id.code}"
}
