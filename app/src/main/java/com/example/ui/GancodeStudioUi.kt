package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.data.Project
import com.example.viewmodel.ProjectViewModel

// Custom styling colors for our Cybertech Gancode theme
val SlateDark = Color(0xFF0F111A)
val CardBackground = Color(0xFF161925)
val ElectricCyan = Color(0xFF00E5FF)
val OrangeRouter = Color(0xFFFF8C00)
val NeonGreen = Color(0xFF39FF14)
val LaserRed = Color(0xFFFF3366)
val SoftText = Color(0xFFA0A5C0)
val InsetConsole = Color(0xFF0A0C14)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GancodeStudioApp(viewModel: ProjectViewModel) {
    val selectedProject by viewModel.selectedProject.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    
    // Application state navigation
    var currentTab by remember { mutableStateOf(0) }

    // If a project is being generated right now, lock screen onto a live build modal
    Scaffold(
        bottomBar = {
            if (!isGenerating) {
                GancodeBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it }
                )
            }
        },
        containerColor = SlateDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // High fidelity Cybertech Title Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(ElectricCyan, OrangeRouter)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "G",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Gancode AI Studio",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "AI CLOUD SYSTEM ENGINE",
                            color = ElectricCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // API Key status indicator
                val hasApiKey = remember {
                    BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" && 
                    BuildConfig.GEMINI_API_KEY.trim().isNotEmpty()
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (hasApiKey) Color(0xFF1B3D2B) else Color(0xFF3B2519))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (hasApiKey) NeonGreen else OrangeRouter)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasApiKey) "LLM REALTIME" else "BLUEPRINT PRESET",
                            color = if (hasApiKey) NeonGreen else OrangeRouter,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Divider(color = InsetConsole, thickness = 1.dp)

            if (isGenerating) {
                // Live Forge Build logs view takes over
                GeneratingBuildView(viewModel = viewModel)
            } else {
                // Standard Workspace tab routing
                when (currentTab) {
                    0 -> PromptForgeScreen(viewModel = viewModel)
                    1 -> MemoryCoreScreen(viewModel = viewModel)
                    2 -> SandboxTerminalScreen(viewModel = viewModel)
                    3 -> NetworkDnsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun GancodeBottomBar(currentTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = CardBackground,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.navigationBarsPadding()
    ) {
        val tabs = listOf(
            TabItem("Forge", Icons.Default.Add, "Prompt & build"),
            TabItem("Memory", Icons.Default.List, "Saved Systems"),
            TabItem("Sandbox", Icons.Default.PlayArrow, "Simulated Runtime"),
            TabItem("Router", Icons.Default.Build, "Global DNS")
        )

        tabs.forEachIndexed { index, tab ->
            val isSelected = currentTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (isSelected) ElectricCyan else SoftText
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        color = if (isSelected) Color.White else SoftText,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color(0xFF232A40)
                ),
                modifier = Modifier.testTag("nav_tab_$index")
            )
        }
    }
}

data class TabItem(val label: String, val icon: ImageVector, val desc: String)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PromptForgeScreen(viewModel: ProjectViewModel) {
    var ideaText by remember { mutableStateOf("") }
    val integrations = listOf("Stripe", "Supabase", "Google Maps", "OpenAI / LLM API")
    val selectedIntegrations = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Welcoming card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2235)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Gancode Infinite Cloud Generator",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Provide your architectural design concept. The Gancode Cloud Engine will convert your concept to Docker containers, provision live endpoints, wire integrations, and compile standard interactive Web Views instantly.",
                    color = SoftText,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Project text idea box
        Text(
            text = "STEP 1: DEFINE SYSTEM CRITERIA",
            color = ElectricCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = ideaText,
            onValueChange = { ideaText = it },
            placeholder = {
                Text(
                    text = "Describe your system... e.g. An appointment booking app for fitness coaches with Stripe subscriptions...",
                    color = SoftText,
                    fontSize = 13.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .testTag("system_prompt_input"),
            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = Color(0xFF2A2E42),
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Modular Integration Blocks
        Text(
            text = "STEP 2: ENFORCE INTEGRATION BLOCKS",
            color = ElectricCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            integrations.forEach { service ->
                val isSelected = selectedIntegrations.contains(service)
                val serviceColor = when (service) {
                    "Stripe" -> Color(0xFF6772E5)
                    "Supabase" -> Color(0xFF3ECF8E)
                    "Google Maps" -> Color(0xFF4285F4)
                    else -> Color(0xFF00A3FF)
                }

                Card(
                    modifier = Modifier
                        .padding(end = 8.dp, bottom = 8.dp)
                        .clickable {
                            if (isSelected) selectedIntegrations.remove(service)
                            else selectedIntegrations.add(service)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) serviceColor.copy(alpha = 0.2f) else CardBackground
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) serviceColor else Color(0xFF2A2E42)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = {
                                if (isSelected) selectedIntegrations.remove(service)
                                else selectedIntegrations.add(service)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = serviceColor,
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = service,
                            color = if (isSelected) Color.White else SoftText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Forge Button
        Button(
            onClick = {
                if (ideaText.trim().isNotEmpty()) {
                    viewModel.startGeneratingProject(ideaText, selectedIntegrations)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("forge_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = ElectricCyan,
                disabledContainerColor = Color(0xFF202A36)
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = ideaText.trim().isNotEmpty()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Forge icon",
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FORGE LIVE CLOUD SYSTEM",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Warnings / Info Section
        val apiKey = remember { BuildConfig.GEMINI_API_KEY }
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.trim().isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2F241E)),
                border = BorderStroke(1.dp, OrangeRouter),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning API key",
                        tint = OrangeRouter,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Aesthetic Fallback System Active",
                            color = OrangeRouter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "To unlock full dynamic AI capabilities that write real, fully custom structures for any idea, please insert a GEMINI_API_KEY inside the secure AI Studio Secrets panel. The system is presently using compiled architectural layers to generate beautiful representations safely.",
                            color = SoftText,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun GeneratingBuildView(viewModel: ProjectViewModel) {
    val liveLogs by viewModel.currentLogsDisplay.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    // Keep logs scrolled down
    LaunchedEffect(liveLogs.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDark)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ENGINE ACTIVE FORGING",
                color = ElectricCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
            CircularProgressIndicator(
                color = ElectricCyan,
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Live virtualization stats
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "DOCKER CONTAINER", color = SoftText, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "gancode-iso-pod", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Column {
                    Text(text = "CPU ALLOCATION", color = SoftText, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "2.0 Cores (Burstable)", color = ElectricCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Column {
                    Text(text = "RAM MEMORY", color = SoftText, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Text(text = "4096 MB", color = OrangeRouter, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Terminal text viewport
        Text(
            text = "CONTAINER LOG STREAM:",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(InsetConsole)
                .border(BorderStroke(1.dp, Color(0xFF1F2335)), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                liveLogs.forEach { log ->
                    Text(
                        text = log,
                        color = when {
                            log.contains("[GANCODE") -> ElectricCyan
                            log.contains("[DOCKER") -> SoftText
                            log.contains("[DB]") || log.contains("[DB") -> OrangeRouter
                            log.contains("[SYSTEM]") || log.contains("succeeded") -> NeonGreen
                            else -> Color.White
                        },
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryCoreScreen(viewModel: ProjectViewModel) {
    val projects by viewModel.filteredProjects.collectAsStateWithLifecycle()
    val selected by viewModel.selectedProject.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Search filter bar
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text(text = "Search systems database...", color = SoftText, fontSize = 13.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = SoftText) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = Color(0xFF2E334D),
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            if (projects.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Empty memory",
                            tint = Color(0xFF2F3452),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No systems found in memory graph.",
                            color = SoftText,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(projects) { proj ->
                    val isCurrent = selected?.id == proj.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clickable { viewModel.selectProject(proj) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) Color(0xFF1B243B) else CardBackground
                        ),
                        border = BorderStroke(
                            width = 1.1.dp,
                            color = if (isCurrent) ElectricCyan else Color(0xFF2E334D)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = proj.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )

                                // Green active light
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (proj.status == "ACTIVE") NeonGreen else LaserRed)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = proj.status,
                                        color = if (proj.status == "ACTIVE") NeonGreen else LaserRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "gancode.ai → ${proj.subdomain}",
                                color = ElectricCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Concept: ${proj.idea}",
                                color = SoftText,
                                fontSize = 12.sp,
                                maxLines = 2,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = proj.backendFramework,
                                    color = SoftText,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF232A44))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                )

                                IconButton(
                                    onClick = { viewModel.deleteProject(proj) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = LaserRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SandboxTerminalScreen(viewModel: ProjectViewModel) {
    val selected by viewModel.selectedProject.collectAsStateWithLifecycle()
    var currentSubTab by remember { mutableStateOf(0) } // 0: Sandbox API Client, 1: Live Web Preview, 2: Terminal Logs

    if (selected == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "No project selected",
                tint = Color(0xFF232944),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No System Selected",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Launch a system in 'Forge' or select an active database graph in 'Memory' to inspect running services, compile web previews, or dispatch requests.",
                color = SoftText,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )
        }
        return
    }

    val proj = selected!!

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick project overview banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1F31))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "CONTAINER INSTANCE: ${proj.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(text = "URL: project.gancode.ai/${proj.subdomain}", color = ElectricCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }

            // Power control
            Button(
                onClick = { viewModel.toggleProjectState() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (proj.status == "ACTIVE") LaserRed.copy(alpha = 0.2f) else NeonGreen.copy(alpha = 0.2f)
                ),
                border = BorderStroke(1.dp, if (proj.status == "ACTIVE") LaserRed else NeonGreen),
                modifier = Modifier.height(30.dp),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (proj.status == "ACTIVE") NeonGreen else LaserRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (proj.status == "ACTIVE") "STOP POD" else "START POD",
                        color = if (proj.status == "ACTIVE") Color.White else NeonGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Subtabs: API Sandbox, Web Preview, Logs console
        TabRow(
            selectedTabIndex = currentSubTab,
            containerColor = CardBackground,
            contentColor = ElectricCyan,
            divider = { Divider(color = InsetConsole) }
        ) {
            Tab(
                selected = currentSubTab == 0,
                onClick = { currentSubTab = 0 },
                text = { Text("API CLIENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) }
            )
            Tab(
                selected = currentSubTab == 1,
                onClick = { currentSubTab = 1 },
                text = { Text("WEB PREVIEW", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) }
            )
            Tab(
                selected = currentSubTab == 2,
                onClick = { currentSubTab = 2 },
                text = { Text("POD LOGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) }
            )
        }

        // Inner Subscreen router
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentSubTab) {
                0 -> ApiClientSandbox(proj = proj, viewModel = viewModel)
                1 -> LiveWebPreviewTab(proj = proj, viewModel = viewModel)
                2 -> PodLogsTerminal(proj = proj, viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiClientSandbox(proj: Project, viewModel: ProjectViewModel) {
    if (proj.status != "ACTIVE") {
        ContainerStoppedPlaceholder()
        return
    }

    val endpoints = remember(proj) { viewModel.parseEndpoints(proj.backendEndpointsJson) }
    val selEndpoint by viewModel.sandboxSelectedEndpoint.collectAsStateWithLifecycle()
    var reqBody by remember { mutableStateOf("") }
    val sandboxResponse by viewModel.sandboxResponse.collectAsStateWithLifecycle()
    val isLoading by viewModel.sandboxLoading.collectAsStateWithLifecycle()

    val matchedEndpoint = endpoints.firstOrNull { it["path"] == selEndpoint }

    // Synchronize request body when endpoint changes
    LaunchedEffect(selEndpoint) {
        if (matchedEndpoint != null) {
            reqBody = matchedEndpoint["inputDesc"] ?: "{}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "DISPATCH SYNTHETIC ENDPOINTS",
            color = ElectricCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "Test live virtual microservices and route databases instantly.",
            color = SoftText,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Select API Dropdown list
        endpoints.forEach { end ->
            val path = end["path"] ?: "/"
            val method = end["method"] ?: "GET"
            val isSelected = path == selEndpoint
            
            val mColor = when(method) {
                "POST" -> NeonGreen
                "PUT" -> OrangeRouter
                "DELETE" -> LaserRed
                else -> ElectricCyan
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clickable { viewModel.updateSelectedEndpoint(path, end["inputDesc"] ?: "{}") },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF1E263D) else CardBackground
                ),
                border = BorderStroke(1.dp, if (isSelected) ElectricCyan else Color(0xFF282C40)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = method,
                        color = mColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(mColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .width(50.dp),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = path, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Text(text = end["desc"] ?: "", color = SoftText, fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (matchedEndpoint != null) {
            val method = matchedEndpoint["method"] ?: "GET"

            if (method == "POST" || method == "PUT") {
                Text(
                    text = "EDIT JSON REQUEST BODY:",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                OutlinedTextField(
                    value = reqBody,
                    onValueChange = { reqBody = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = Color(0xFF282C40),
                        focusedContainerColor = InsetConsole,
                        unfocusedContainerColor = InsetConsole,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Click send button
            Button(
                onClick = {
                    viewModel.triggerVirtualRequest(method, selEndpoint ?: "/", reqBody)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                shape = RoundedCornerShape(6.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "DISPATCH $method TO RUNTIME", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // REST Client response view
            if (sandboxResponse != null || isLoading) {
                Text(
                    text = "RESPONSE STACK:",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(InsetConsole)
                        .border(BorderStroke(1.dp, Color(0xFF2C324D)), RoundedCornerShape(6.dp))
                        .padding(12.dp)
                ) {
                    if (isLoading) {
                        Text(text = "HTTP/1.1 100 Continue...\nWaiting for isolated secure pod sandbox resolution...", color = SoftText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    } else {
                        Text(
                            text = "HTTP/1.1 200 OK\nContent-Type: application/json\n\n${sandboxResponse}",
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiveWebPreviewTab(proj: Project, viewModel: ProjectViewModel) {
    if (proj.status != "ACTIVE") {
        ContainerStoppedPlaceholder()
        return
    }

    val pages = remember(proj) { viewModel.parsePages(proj.frontendPagesJson) }
    var selectedScreenIndex by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text(
            text = "LIVE COMPILED REACT WEB VIEW",
            color = ElectricCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "Direct deployment preview for user subdomain route: ${proj.subdomain}.gancode.ai",
            color = SoftText,
            fontSize = 10.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Screen routing tabs
        ScrollableTabRow(
            selectedTabIndex = selectedScreenIndex,
            containerColor = CardBackground,
            contentColor = ElectricCyan,
            edgePadding = 0.dp
        ) {
            pages.forEachIndexed { i, p ->
                Tab(
                    selected = selectedScreenIndex == i,
                    onClick = { selectedScreenIndex = i },
                    text = { Text(p["name"] ?: "Page", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // High-Fidelity UI Simulator viewport
        if (pages.isNotEmpty() && selectedScreenIndex < pages.size) {
            val page = pages[selectedScreenIndex]
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(BorderStroke(1.5.dp, Color(0xFF2F3652)), RoundedCornerShape(12.dp))
                    .background(SlateDark)
            ) {
                // Header of simulated webpage browser
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardBackground)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Colored windows buttons
                        Row(modifier = Modifier.padding(end = 12.dp)) {
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(LaserRed))
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(OrangeRouter))
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(NeonGreen))
                        }

                        // Web url bar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(InsetConsole)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "https://${proj.subdomain}.gancode.ai${page["route"]}",
                                color = SoftText,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Simulated Page Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp)
                    ) {
                        Text(
                            text = page["name"]?.uppercase() ?: "DASHBOARD",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                        Text(
                            text = page["desc"] ?: "",
                            color = SoftText,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Render beautiful mock component list blocks
                        val comps = page["components"]?.split(",")?.map { it.trim() } ?: emptyList()
                        comps.forEach { comp ->
                            Spacer(modifier = Modifier.height(8.dp))
                            ComponentPreviewBlock(compName = comp, projId = proj.id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComponentPreviewBlock(compName: String, projId: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, Color(0xFF242A3E))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(RoundedCornerShape(3.5.dp))
                            .background(ElectricCyan)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = compName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "React Live Component",
                    color = ElectricCyan,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Simulated graphic component
            when {
                compName.contains("Chart") || compName.contains("Metrics") -> {
                    // Mock mini charts
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(InsetConsole)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            // Draw dynamic cybertech curves
                            val points = listOf(
                                h * 0.8f, h * 0.5f, h * 0.6f, h * 0.2f, h * 0.4f, h * 0.1f
                            )
                            val step = w / 5f
                            for (i in 0..4) {
                                drawLine(
                                    color = ElectricCyan,
                                    start = androidx.compose.ui.geometry.Offset(i * step, points[i]),
                                    end = androidx.compose.ui.geometry.Offset((i + 1) * step, points[i + 1]),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                        }
                        Text(text = "LIVE TELEMETRY CURVE: +33.8% ACTIVE", color = NeonGreen, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    }
                }
                compName.contains("Card") || compName.contains("Summary") -> {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Users: 1.2K", "Load: 12%", "API: OK").forEach { label ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(InsetConsole)
                                    .padding(6.dp)
                            ) {
                                Text(text = label, color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                else -> {
                    // List / Table dynamic rows
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(2) { index ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(InsetConsole)
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Secure Payload ID-${index + 101}", color = Color.White, fontSize = 9.sp)
                                Text(text = "PROCESSED", color = NeonGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PodLogsTerminal(proj: Project, viewModel: ProjectViewModel) {
    val scrollState = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CONTAINER TERMINAL LOG STACK",
                color = ElectricCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Reload logs",
                tint = ElectricCyan,
                modifier = Modifier.size(16.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(InsetConsole)
                .border(BorderStroke(1.dp, Color(0xFF2C324D)), RoundedCornerShape(6.dp))
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                // If the container is stopped, display standard shutdown logs
                val lines = if (proj.status == "ACTIVE") {
                    proj.terminalLogsJson.split("\n")
                } else {
                    listOf(
                        "[SYSTEM] INITIATING SECURE SHUTDOWN...",
                        "[SYSTEM] Forwarding exit code state signal to Kubernetes orchestration master...",
                        "[SYSTEM] Docker services stop hook completed.",
                        "[CONTAINER] Pod gancode-deployment successfully paused. State: OFFLINE"
                    )
                }

                lines.forEach { line ->
                    Text(
                        text = line,
                        color = when {
                            line.contains("[SYSTEM]") || line.contains("succeeded") -> NeonGreen
                            line.contains("[GANCODE") -> ElectricCyan
                            line.contains("SHUTDOWN") || line.contains("OFFLINE") -> LaserRed
                            else -> SoftText
                        },
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ContainerStoppedPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Offline container",
            tint = LaserRed,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "CONTAINER HUB STOPPED",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "The isolated micro-pod container is currently powered off to preserve cloud resource slots. Click 'START POD' on the top banner router to spins up endpoints and React preview web views interactively.",
            color = SoftText,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun NetworkDnsScreen(viewModel: ProjectViewModel) {
    val projects by viewModel.allProjects.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "GLOBAL PROXY ROUTER (CLOUDFLARE DNS)",
            color = ElectricCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "All project domains are resolved instantly through gancode.ai DNS registers.",
            color = SoftText,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // DNS Diagram Mapping
        Card(
            colors = CardDefaults.cardColors(containerColor = InsetConsole),
            border = BorderStroke(1.dp, Color(0xFF272D40)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "GANCODE INTEL SYSTEM MAPPING",
                    color = OrangeRouter,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Master Domain
                DnsMappingLine(label = "gancode.ai", resolvesTo = "Primary UI Landing Portal (gancode_web)")
                Spacer(modifier = Modifier.height(8.dp))
                DnsMappingLine(label = "api.gancode.ai", resolvesTo = "Orchestrator Core Gateway Proxy Daemon")
                
                // Active container Subdomains
                projects.filter { it.status == "ACTIVE" }.forEach { proj ->
                    Spacer(modifier = Modifier.height(8.dp))
                    DnsMappingLine(
                        label = "${proj.subdomain}.gancode.ai",
                        resolvesTo = "Live Pod Container ID-k8s_${proj.subdomain.take(6)}"
                    )
                }
            }
        }

        // DNS request analytics chart
        Text(
            text = "LIVE ROUTER INCOMING TRAFFIC:",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Proxy Requests Rate", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "432 Req/Sec", color = NeonGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(InsetConsole),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        // Custom wave animation representation using static Canvas draws
                        val brush = Brush.linearGradient(listOf(ElectricCyan, OrangeRouter))
                        val points = listOf(0.4f, 0.7f, 0.3f, 0.8f, 0.2f, 0.5f, 0.4f, 0.9f, 0.1f)
                        val step = w / 8f
                        for (i in 0..7) {
                            drawCircle(
                                brush = brush,
                                center = androidx.compose.ui.geometry.Offset(i * step, h * points[i]),
                                radius = 4.dp.toPx()
                            )
                            drawLine(
                                brush = brush,
                                start = androidx.compose.ui.geometry.Offset(i * step, h * points[i]),
                                end = androidx.compose.ui.geometry.Offset((i + 1) * step, h * points[i + 1]),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Cloudflare Cache ratio: 94.2%", color = SoftText, fontSize = 10.sp)
                    Text(text = "SSL Status: Active (TLS 1.3)", color = ElectricCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun DnsMappingLine(label: String, resolvesTo: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF141724))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "DNS router arrow",
                tint = ElectricCyan,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Text(
            text = resolvesTo,
            color = SoftText,
            fontSize = 10.sp
        )
    }
}
