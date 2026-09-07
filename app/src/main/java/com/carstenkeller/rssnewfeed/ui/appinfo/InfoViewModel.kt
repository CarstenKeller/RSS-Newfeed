package com.carstenkeller.rssnewfeed.ui.appinfo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.BufferedReader
import java.io.InputStreamReader

sealed interface PlanLine {
    data class Heading(val text: String, val level: Int) : PlanLine
    data class Todo(val text: String, val done: Boolean, val isNextStep: Boolean) : PlanLine
    data class Bullet(val text: String) : PlanLine
    data class Paragraph(val text: String) : PlanLine
    data object Blank : PlanLine
}

class InfoViewModel(application: Application) : AndroidViewModel(application) {

    private val _planLines = MutableStateFlow<List<PlanLine>>(emptyList())
    val planLines: StateFlow<List<PlanLine>> = _planLines

    init {
        _planLines.value = loadPlan()
    }

    private fun loadPlan(): List<PlanLine> {
        val rawLines = try {
            getApplication<Application>().assets.open("PLAN.md").use { stream ->
                BufferedReader(InputStreamReader(stream)).readLines()
            }
        } catch (e: Exception) {
            listOf("PLAN.md konnte nicht geladen werden: ${e.message}")
        }

        var nextStepAssigned = false
        return rawLines.map { line ->
            val trimmed = line.trim()
            when {
                trimmed.isEmpty() -> PlanLine.Blank
                trimmed.startsWith("## ") -> PlanLine.Heading(trimmed.removePrefix("## "), level = 2)
                trimmed.startsWith("# ") -> PlanLine.Heading(trimmed.removePrefix("# "), level = 1)
                trimmed.startsWith("- [x]", ignoreCase = true) -> {
                    PlanLine.Todo(trimmed.substring(5).trim(), done = true, isNextStep = false)
                }
                trimmed.startsWith("- [ ]") -> {
                    val isNext = !nextStepAssigned
                    if (isNext) nextStepAssigned = true
                    PlanLine.Todo(trimmed.substring(5).trim(), done = false, isNextStep = isNext)
                }
                trimmed.startsWith("- ") -> PlanLine.Bullet(trimmed.removePrefix("- "))
                else -> PlanLine.Paragraph(trimmed)
            }
        }
    }
}
