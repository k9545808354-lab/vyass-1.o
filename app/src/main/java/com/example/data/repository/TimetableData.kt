package com.example.data.repository

import com.example.data.model.BatchAccent
import com.example.data.model.BatchEntry
import com.example.data.model.CodeSnippet
import com.example.data.model.DaySchedule
import com.example.data.model.DivisionId
import com.example.data.model.DivisionInfo
import com.example.data.model.PdfNoteItem
import com.example.data.model.TimetableCell

object TimetableData {

    val divisions = listOf(
        DivisionInfo(
            id = DivisionId.E,
            title = "Division E",
            subtitle = "Computer Engineering • Class Room 230",
            classRoom = "230",
            wefDate = "28/09/2026"
        ),
        DivisionInfo(
            id = DivisionId.F,
            title = "Division F",
            subtitle = "Computer Engineering • Class Room 234",
            classRoom = "234",
            wefDate = "31/10/2026"
        ),
        DivisionInfo(
            id = DivisionId.G,
            title = "Division G",
            subtitle = "Computer Engineering • Class Room 236",
            classRoom = "236",
            wefDate = "31/10/2026"
        )
    )

    val unitNames = listOf("Unit 1", "Unit 2", "Unit 3", "Unit 4", "Unit 5")

    val codeSnippets = listOf(
        CodeSnippet(
            id = "hello",
            label = "Hello World",
            code = """
                #include <stdio.h>

                int main() {
                    printf("Welcome to SSBT COET - PPS Lab!\n");
                    return 0;
                }
            """.trimIndent()
        ),
        CodeSnippet(
            id = "prime",
            label = "Prime Check",
            code = """
                #include <stdio.h>

                int main() {
                    int n = 29, isPrime = 1;
                    for(int i = 2; i <= n/2; ++i) {
                        if(n % i == 0) { isPrime = 0; break; }
                    }
                    if (isPrime) printf("%d is a Prime number.\n", n);
                    else printf("%d is Not a Prime number.\n", n);
                    return 0;
                }
            """.trimIndent()
        ),
        CodeSnippet(
            id = "factorial",
            label = "Factorial",
            code = """
                #include <stdio.h>

                int main() {
                    int n = 5;
                    long long fact = 1;
                    for(int i = 1; i <= n; ++i) fact *= i;
                    printf("Factorial of %d = %lld\n", n, fact);
                    return 0;
                }
            """.trimIndent()
        ),
        CodeSnippet(
            id = "fibonacci",
            label = "Fibonacci",
            code = """
                #include <stdio.h>

                int main() {
                    int n = 7, t1 = 0, t2 = 1, nextTerm;
                    printf("Fibonacci Series: ");
                    for (int i = 1; i <= n; ++i) {
                        printf("%d ", t1);
                        nextTerm = t1 + t2;
                        t1 = t2;
                        t2 = nextTerm;
                    }
                    printf("\n");
                    return 0;
                }
            """.trimIndent()
        )
    )

    fun getDivisionInfo(divisionId: DivisionId): DivisionInfo =
        divisions.first { it.id == divisionId }

    fun getSchedule(divisionId: DivisionId): List<DaySchedule> = when (divisionId) {
        DivisionId.E -> divisionESchedule
        DivisionId.F -> divisionFSchedule
        DivisionId.G -> divisionGSchedule
    }

    val defaultUnit1Pdfs: Map<String, List<PdfNoteItem>> = mapOf(
        "EM-I-Unit 1" to listOf(
            PdfNoteItem("def_emi_u1", "EM-I_Unit1_Differential_Calculus.pdf", "EM-I", "Unit 1")
        ),
        "EP-Unit 1" to listOf(
            PdfNoteItem("def_ep_u1", "EP_Unit1_Lasers_and_Optics.pdf", "EP", "Unit 1")
        ),
        "BEEE-Unit 1" to listOf(
            PdfNoteItem("def_beee_u1", "BEEE_Unit1_DC_Circuits.pdf", "BEEE", "Unit 1")
        ),
        "PPS-Unit 1" to listOf(
            PdfNoteItem("def_pps_u1", "PPS_Unit1_Intro_to_C_Programming.pdf", "PPS", "Unit 1")
        ),
        "ENG-Unit 1" to listOf(
            PdfNoteItem("def_eng_u1", "ENG_Unit1_Communication_Skills.pdf", "ENG", "Unit 1")
        ),
        "WPL-Unit 1" to listOf(
            PdfNoteItem("def_wpl_u1", "WPL_Carpentry_Fitting_Manual.pdf", "WPL", "Unit 1")
        ),
        "CC-Unit 1" to listOf(
            PdfNoteItem("def_cc_u1", "CC_Unit1_Professional_Ethics.pdf", "CC", "Unit 1")
        )
    )

    private fun ppsLecture(faculty: String, colSpan: Int = 1) = TimetableCell.Lecture(
        subjectCode = "PPS",
        faculty = faculty,
        isPpsHighlight = true,
        colSpan = colSpan
    )

    private fun wplLecture(faculty: String) = TimetableCell.Lecture(
        subjectCode = "WPL",
        faculty = faculty,
        isWplHighlight = true,
        colSpan = 2
    )

    private fun guardianLecture() = TimetableCell.Lecture(
        subjectCode = "TGS",
        faculty = "Guardian Hour",
        isGuardianHour = true,
        clickable = false,
        colSpan = 1
    )

    private val divisionESchedule = listOf(
        DaySchedule(
            day = "MON",
            preLunchCells = listOf(
                TimetableCell.Lecture("EP", "CUN"),
                TimetableCell.Lecture("BEEE", "RC")
            ),
            postLunchCells = listOf(
                TimetableCell.BatchSplit(
                    accent = BatchAccent.CYAN,
                    batches = listOf(
                        BatchEntry("E1:", "ENG", "ENG-SC", "(244)", BatchAccent.CYAN),
                        BatchEntry("E2:", "CC", "CC-PRS", "(230)", BatchAccent.CYAN),
                        BatchEntry("E3:", null, "LIB", null, BatchAccent.CYAN)
                    )
                ),
                TimetableCell.Empty(colSpan = 2)
            )
        ),
        DaySchedule(
            day = "TUE",
            preLunchCells = listOf(
                TimetableCell.Lecture("EM-I", "SSP"),
                TimetableCell.Lecture("EP", "CUN")
            ),
            postLunchCells = listOf(
                ppsLecture("PRS"),
                TimetableCell.Lecture("BEEE", "RC"),
                TimetableCell.BatchSplit(
                    accent = BatchAccent.PURPLE,
                    batches = listOf(
                        BatchEntry("E1:", "CC", "CC-JRT", "(238)", BatchAccent.PURPLE),
                        BatchEntry("E2:", null, "LIB", null, BatchAccent.PURPLE),
                        BatchEntry("E3:", "ENG", "ENG-TYC", "(244)", BatchAccent.PURPLE)
                    )
                )
            )
        ),
        DaySchedule(
            day = "WED",
            preLunchCells = listOf(
                TimetableCell.Lecture("EM-I", "SSP"),
                TimetableCell.Lecture("BEEE", "RC")
            ),
            postLunchCells = listOf(
                TimetableCell.BatchSplit(
                    accent = BatchAccent.GREEN,
                    batches = listOf(
                        BatchEntry("E1:", "BEEE", "BEEE-RC", "(217)", BatchAccent.GREEN),
                        BatchEntry("E2:", "EP", "EP-CUN", "(242)", BatchAccent.GREEN),
                        BatchEntry("E3:", "PPS", "PPS-PRS", "(128)", BatchAccent.GREEN)
                    )
                ),
                TimetableCell.Empty(colSpan = 2)
            )
        ),
        DaySchedule(
            day = "THU",
            preLunchCells = listOf(
                TimetableCell.BatchSplit(
                    accent = BatchAccent.GREEN,
                    batches = listOf(
                        BatchEntry("E1:", "EP", "EP-CUN", "(242)", BatchAccent.GREEN),
                        BatchEntry("E2:", "PPS", "PPS-PRS", "(128)", BatchAccent.GREEN),
                        BatchEntry("E3:", "BEEE", "BEEE-NAB", "(217)", BatchAccent.GREEN)
                    )
                )
            ),
            postLunchCells = listOf(
                ppsLecture("PRS"),
                TimetableCell.Lecture("EM-I", "SSP"),
                wplLecture("DBS / PRS / RC")
            )
        ),
        DaySchedule(
            day = "FRI",
            preLunchCells = listOf(
                TimetableCell.BatchSplit(
                    accent = BatchAccent.CYAN,
                    batches = listOf(
                        BatchEntry("E1:", null, "LIB", null, BatchAccent.CYAN),
                        BatchEntry("E2:", "ENG", "ENG-SC", "(244)", BatchAccent.CYAN),
                        BatchEntry("E3:", "CC", "CC-UMP", "(230)", BatchAccent.CYAN)
                    )
                )
            ),
            postLunchCells = listOf(
                TimetableCell.BatchSplit(
                    accent = BatchAccent.GREEN,
                    batches = listOf(
                        BatchEntry("E1:", "PPS", "PPS-PRS", "(128)", BatchAccent.GREEN),
                        BatchEntry("E2:", "BEEE", "BEEE-RC", "(217)", BatchAccent.GREEN),
                        BatchEntry("E3:", "EP", "EP-CUN", "(242)", BatchAccent.GREEN)
                    )
                ),
                TimetableCell.Lecture("CC", "SB"),
                TimetableCell.Empty(colSpan = 1)
            )
        ),
        DaySchedule(
            day = "SAT",
            preLunchCells = listOf(
                TimetableCell.Lecture("EM-I", "SSP"),
                guardianLecture()
            ),
            postLunchCells = listOf(
                TimetableCell.Lecture("EP", "CUN"),
                ppsLecture("PRS"),
                TimetableCell.Lecture("ENG", "SC"),
                TimetableCell.Empty(colSpan = 1)
            )
        )
    )

    private val divisionFSchedule = listOf(
        DaySchedule(
            day = "MON",
            preLunchCells = listOf(
                ppsLecture("DDP"),
                TimetableCell.Lecture("EM-I", "JRT")
            ),
            postLunchCells = listOf(
                TimetableCell.BatchSplit(
                    accent = BatchAccent.GREEN,
                    batches = listOf(
                        BatchEntry("F1:", "PPS", "PPS-DST", "(128)", BatchAccent.GREEN),
                        BatchEntry("F2:", "BEEE", "BEEE-PSC", "(217)", BatchAccent.GREEN),
                        BatchEntry("F3:", "EP", "EP-KSP", "(242)", BatchAccent.GREEN)
                    )
                ),
                TimetableCell.Empty(colSpan = 2)
            )
        ),
        DaySchedule(
            day = "TUE",
            preLunchCells = listOf(
                TimetableCell.Lecture("BEEE", "PSC"),
                TimetableCell.Lecture("EP", "KSP")
            ),
            postLunchCells = listOf(
                TimetableCell.Lecture("EM-I", "JRT"),
                TimetableCell.Lecture("ENG", "SC"),
                TimetableCell.BatchSplit(
                    accent = BatchAccent.PURPLE,
                    batches = listOf(
                        BatchEntry("F1:", "ENG", "ENG-TYC", "(244)", BatchAccent.PURPLE),
                        BatchEntry("F2:", null, "LIB", null, BatchAccent.PURPLE),
                        BatchEntry("F3:", "CC", "CC-CUN", "(234)", BatchAccent.PURPLE)
                    )
                )
            )
        ),
        DaySchedule(
            day = "WED",
            preLunchCells = listOf(
                TimetableCell.Lecture("EP", "KSP"),
                ppsLecture("DDP")
            ),
            postLunchCells = listOf(
                TimetableCell.Lecture("BEEE", "PSC", colSpan = 2),
                TimetableCell.BatchSplit(
                    accent = BatchAccent.GREEN,
                    batches = listOf(
                        BatchEntry("F1:", "EP", "EP-CUN", "(242)", BatchAccent.GREEN),
                        BatchEntry("F2:", "PPS", "PPS-SCN", "(128)", BatchAccent.GREEN),
                        BatchEntry("F3:", "BEEE", "BEEE-PSC", "(217)", BatchAccent.GREEN)
                    )
                )
            )
        ),
        DaySchedule(
            day = "THU",
            preLunchCells = listOf(
                TimetableCell.Lecture("EM-I", "JRT"),
                TimetableCell.Lecture("BEEE", "PSC")
            ),
            postLunchCells = listOf(
                TimetableCell.BatchSplit(
                    accent = BatchAccent.CYAN,
                    batches = listOf(
                        BatchEntry("F1:", null, "LIB", null, BatchAccent.CYAN),
                        BatchEntry("F2:", "CC", "CC-TYC", "(234)", BatchAccent.CYAN),
                        BatchEntry("F3:", "ENG", "ENG-SC", "(244)", BatchAccent.CYAN)
                    )
                ),
                wplLecture("DBS / DST / PSC")
            )
        ),
        DaySchedule(
            day = "FRI",
            preLunchCells = listOf(
                TimetableCell.BatchSplit(
                    accent = BatchAccent.GREEN,
                    batches = listOf(
                        BatchEntry("F1:", "BEEE", "BEEE-PSC", "(217)", BatchAccent.GREEN),
                        BatchEntry("F2:", "EP", "EP-KSP", "(242)", BatchAccent.GREEN),
                        BatchEntry("F3:", "PPS", "PPS-DST", "(128)", BatchAccent.GREEN)
                    )
                )
            ),
            postLunchCells = listOf(
                ppsLecture("DDP"),
                TimetableCell.Lecture("EM-I", "JRT"),
                TimetableCell.Lecture("CC", "UMP"),
                TimetableCell.Empty(colSpan = 1)
            )
        ),
        DaySchedule(
            day = "SAT",
            preLunchCells = listOf(
                TimetableCell.Lecture("EP", "KSP"),
                guardianLecture()
            ),
            postLunchCells = listOf(
                TimetableCell.Lecture("ENG", "SC"),
                TimetableCell.Lecture("CC", "UMP"),
                TimetableCell.Empty(colSpan = 2)
            )
        )
    )

    private val divisionGSchedule = listOf(
        DaySchedule(
            day = "MON",
            preLunchCells = listOf(
                TimetableCell.Lecture("EM-I", "JRT"),
                ppsLecture("DDP")
            ),
            postLunchCells = listOf(
                TimetableCell.Lecture("BEEE", "PSC", colSpan = 2),
                TimetableCell.Empty(colSpan = 2)
            )
        ),
        DaySchedule(
            day = "TUE",
            preLunchCells = listOf(
                TimetableCell.Lecture("EP", "KSP"),
                TimetableCell.Lecture("BEEE", "PSC")
            ),
            postLunchCells = listOf(
                TimetableCell.BatchSplit(
                    accent = BatchAccent.CYAN,
                    batches = listOf(
                        BatchEntry("G1:", null, "LIB", null, BatchAccent.CYAN),
                        BatchEntry("G2:", "CC", "CC-CUN", "(236)", BatchAccent.CYAN),
                        BatchEntry("G3:", "ENG", "ENG-TYC", "(244)", BatchAccent.CYAN)
                    )
                ),
                TimetableCell.BatchSplit(
                    accent = BatchAccent.GREEN,
                    batches = listOf(
                        BatchEntry("G1:", "BEEE", "BEEE-PSC", "(217)", BatchAccent.GREEN),
                        BatchEntry("G2:", "PPS", "PPS-DST", "(128)", BatchAccent.GREEN),
                        BatchEntry("G3:", "EP", "EP-KSP", "(242)", BatchAccent.GREEN)
                    )
                )
            )
        ),
        DaySchedule(
            day = "WED",
            preLunchCells = listOf(
                TimetableCell.Lecture("BEEE", "PSC"),
                TimetableCell.Lecture("EM-I", "JRT")
            ),
            postLunchCells = listOf(
                ppsLecture("DDP"),
                TimetableCell.Lecture("ENG", "SC"),
                TimetableCell.BatchSplit(
                    accent = BatchAccent.GREEN,
                    batches = listOf(
                        BatchEntry("G1:", "EP", "EP-CUN", "(242)", BatchAccent.GREEN),
                        BatchEntry("G2:", "BEEE", "BEEE-PSC", "(217)", BatchAccent.GREEN),
                        BatchEntry("G3:", "PPS", "PPS-SCN", "(128)", BatchAccent.GREEN)
                    )
                )
            )
        ),
        DaySchedule(
            day = "THU",
            preLunchCells = listOf(
                ppsLecture("DDP"),
                TimetableCell.Lecture("EM-I", "JRT")
            ),
            postLunchCells = listOf(
                TimetableCell.BatchSplit(
                    accent = BatchAccent.CYAN,
                    batches = listOf(
                        BatchEntry("G1:", "CC", "CC-TYC", "(236)", BatchAccent.CYAN),
                        BatchEntry("G2:", "ENG", "ENG-SC", "(244)", BatchAccent.CYAN),
                        BatchEntry("G3:", null, "LIB", null, BatchAccent.CYAN)
                    )
                ),
                TimetableCell.BatchSplit(
                    accent = BatchAccent.GREEN,
                    batches = listOf(
                        BatchEntry("G1:", "PPS", "PPS-SCN", "(128)", BatchAccent.GREEN),
                        BatchEntry("G2:", "EP", "EP-KSP", "(242)", BatchAccent.GREEN),
                        BatchEntry("G3:", "BEEE", "BEEE-PSC", "(217)", BatchAccent.GREEN)
                    )
                )
            )
        ),
        DaySchedule(
            day = "FRI",
            preLunchCells = listOf(
                TimetableCell.Lecture("EP", "KSP"),
                TimetableCell.Lecture("EM-I", "JRT")
            ),
            postLunchCells = listOf(
                TimetableCell.BatchSplit(
                    accent = BatchAccent.CYAN,
                    batches = listOf(
                        BatchEntry("G1:", "ENG", "ENG-TYC", "(244)", BatchAccent.CYAN),
                        BatchEntry("G2:", null, "LIB", null, BatchAccent.CYAN),
                        BatchEntry("G3:", "CC", "CC-SB", "(236)", BatchAccent.CYAN)
                    )
                ),
                wplLecture("DBS / DST / PSC")
            )
        ),
        DaySchedule(
            day = "SAT",
            preLunchCells = listOf(
                TimetableCell.Lecture("EP", "KSP"),
                guardianLecture()
            ),
            postLunchCells = listOf(
                TimetableCell.Lecture("CC", "UMP", colSpan = 2),
                TimetableCell.Empty(colSpan = 2)
            )
        )
    )
}
