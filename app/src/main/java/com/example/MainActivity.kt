package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.DivisionId
import com.example.ui.screens.BiometricGateScreen
import com.example.ui.screens.CompilerScreen
import com.example.ui.screens.DivisionSelectionScreen
import com.example.ui.screens.LaptopKernelScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.PdfHubScreen
import com.example.ui.screens.SubjectUnitsScreen
import com.example.ui.screens.TimetableScreen
import com.example.ui.theme.VyasBackground
import com.example.ui.theme.VyasTheme
import com.example.ui.viewmodel.VyasViewModel
import kotlinx.coroutines.launch

object VyasRoutes {
    const val LOCK = "lock"
    const val BIOMETRIC_GATE = "biometric_gate"
    const val LAPTOP_KERNEL = "laptop_kernel"
    const val DIVISIONS = "divisions"
    const val TIMETABLE = "timetable/{division}"
    const val UNITS = "units/{subject}"
    const val COMPILER = "compiler"
    const val PDF_HUB = "pdf/{subject}/{unit}"

    fun timetable(division: DivisionId) = "timetable/${division.code}"
    fun units(subject: String) = "units/${Uri.encode(subject)}"
    fun pdfHub(subject: String, unit: String) = "pdf/${Uri.encode(subject)}/${Uri.encode(unit)}"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            VyasTheme {
                val viewModel: VyasViewModel = viewModel()
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()

                val lockState by viewModel.lockState.collectAsStateWithLifecycle()
                val divisions by viewModel.divisions.collectAsStateWithLifecycle()
                val schedules by viewModel.schedules.collectAsStateWithLifecycle()
                val customizedDivisions by viewModel.customizedDivisions.collectAsStateWithLifecycle()
                val activeCompilerTab by viewModel.activeCompilerTab.collectAsStateWithLifecycle()
                val pdfHubState by viewModel.pdfHubState.collectAsStateWithLifecycle()

                val showMessage: (String) -> Unit = { msg ->
                    coroutineScope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(msg)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = VyasBackground,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = VyasRoutes.LOCK
                        ) {
                            composable(VyasRoutes.LOCK) {
                                LockScreen(
                                    uiState = lockState,
                                    onPasswordChange = viewModel::onPasswordChanged,
                                    onUnlockAttempt = {
                                        if (viewModel.verifyPassword()) {
                                            navController.navigate(VyasRoutes.BIOMETRIC_GATE) {
                                                popUpTo(VyasRoutes.LOCK) { inclusive = true }
                                            }
                                        }
                                    }
                                )
                            }

                            composable(VyasRoutes.BIOMETRIC_GATE) {
                                BiometricGateScreen(
                                    onSequenceComplete = {
                                        navController.navigate(VyasRoutes.LAPTOP_KERNEL) {
                                            popUpTo(VyasRoutes.BIOMETRIC_GATE) { inclusive = true }
                                        }
                                    },
                                    onSkip = {
                                        navController.navigate(VyasRoutes.DIVISIONS) {
                                            popUpTo(VyasRoutes.BIOMETRIC_GATE) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable(VyasRoutes.LAPTOP_KERNEL) {
                                LaptopKernelScreen(
                                    onSequenceComplete = {
                                        navController.navigate(VyasRoutes.DIVISIONS) {
                                            popUpTo(VyasRoutes.LAPTOP_KERNEL) { inclusive = true }
                                        }
                                    },
                                    onSkip = {
                                        navController.navigate(VyasRoutes.DIVISIONS) {
                                            popUpTo(VyasRoutes.LAPTOP_KERNEL) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable(VyasRoutes.DIVISIONS) {
                                DivisionSelectionScreen(
                                    divisions = divisions,
                                    onDivisionSelected = { division ->
                                        viewModel.selectDivision(division)
                                        navController.navigate(VyasRoutes.timetable(division))
                                    }
                                )
                            }

                            composable(
                                route = VyasRoutes.TIMETABLE,
                                arguments = listOf(navArgument("division") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val divCode = backStackEntry.arguments?.getString("division") ?: "E"
                                val divisionId = DivisionId.fromCode(divCode)
                                val divInfo = divisions.first { it.id == divisionId }
                                val divSchedule = schedules[divisionId].orEmpty()
                                TimetableScreen(
                                    divisionInfo = divInfo,
                                    schedule = divSchedule,
                                    isCustomized = divisionId in customizedDivisions,
                                    onBackToDivisions = { navController.popBackStack() },
                                    onSubjectClick = { subjectCode ->
                                        navController.navigate(VyasRoutes.units(subjectCode))
                                    },
                                    onUpdateCell = { dayIdx, isPre, cellIdx, newCell ->
                                        viewModel.updateTimetableCell(divisionId, dayIdx, isPre, cellIdx, newCell)
                                    },
                                    onSplitCell = { dayIdx, isPre, cellIdx ->
                                        viewModel.splitTimetableCell(divisionId, dayIdx, isPre, cellIdx)
                                    },
                                    onMergeNextCell = { dayIdx, isPre, cellIdx ->
                                        viewModel.mergeTimetableCells(divisionId, dayIdx, isPre, cellIdx)
                                    },
                                    onUpdateDivisionInfo = { room, wef ->
                                        viewModel.updateDivisionInfo(divisionId, room, wef)
                                    },
                                    onResetDefault = {
                                        viewModel.resetDivisionToDefault(divisionId)
                                    },
                                    onShowMessage = showMessage
                                )
                            }

                            composable(
                                route = VyasRoutes.UNITS,
                                arguments = listOf(navArgument("subject") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val subject = Uri.decode(
                                    backStackEntry.arguments?.getString("subject") ?: "PPS"
                                )
                                SubjectUnitsScreen(
                                    subjectCode = subject,
                                    onBackToTimetable = { navController.popBackStack() },
                                    onOpenCompilers = {
                                        viewModel.selectCompilerTab(1)
                                        navController.navigate(VyasRoutes.COMPILER)
                                    },
                                    onUnitClick = { unit ->
                                        navController.navigate(VyasRoutes.pdfHub(subject, unit))
                                    }
                                )
                            }

                            composable(VyasRoutes.COMPILER) {
                                CompilerScreen(
                                    activeTab = activeCompilerTab,
                                    onSelectTab = viewModel::selectCompilerTab,
                                    onBackToUnits = { navController.popBackStack() },
                                    onShowMessage = showMessage
                                )
                            }

                            composable(
                                route = VyasRoutes.PDF_HUB,
                                arguments = listOf(
                                    navArgument("subject") { type = NavType.StringType },
                                    navArgument("unit") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val subject = Uri.decode(
                                    backStackEntry.arguments?.getString("subject") ?: "PPS"
                                )
                                val unit = Uri.decode(
                                    backStackEntry.arguments?.getString("unit") ?: "Unit 1"
                                )
                                LaunchedEffect(subject, unit) {
                                    viewModel.loadPdfsForUnit(subject, unit)
                                }
                                PdfHubScreen(
                                    uiState = pdfHubState,
                                    onBackToUnits = { navController.popBackStack() },
                                    onImportPdf = viewModel::importPdf,
                                    onOpenPdf = viewModel::openPdfViewer,
                                    onClosePdfViewer = viewModel::closePdfViewer,
                                    onRequestSavePdf = viewModel::prepareExport,
                                    onCompleteExport = viewModel::completeExport,
                                    onShowMessage = showMessage,
                                    onClearStatusMessage = viewModel::clearStatusMessage
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
