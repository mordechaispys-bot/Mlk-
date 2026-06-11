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
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
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

// Helper translation lookup dictionary for bilingual localization
fun getTxt(key: String, lang: String): String {
    val en = mapOf(
        "app_title" to "Gancode AI Studio",
        "app_subtitle" to "AI LOCAL CLOUD SYSTEM",
        "nav_forge" to "Forge",
        "nav_memory" to "Memory",
        "nav_sandbox" to "Sandbox",
        "nav_router" to "Router",
        "forge_title" to "Gancode Infinite Cloud Generator",
        "forge_desc" to "Provide your architectural design concept. The Gancode Cloud Engine will convert your concept to Docker containers, provision local port endpoints, wire integrations, and compile standard interactive Web Views instantly.",
        "step1" to "STEP 1: DEFINE SYSTEM CRITERIA",
        "step2" to "STEP 2: ENFORCE INTEGRATION BLOCKS",
        "placeholder_desc" to "Describe your system... e.g. An appointment booking app for fitness coaches with Stripe subscriptions...",
        "forge_button" to "FORGE LIVE LOCAL CLOUD SYSTEM",
        "mem_title" to "GANCODE MEMORY CORE",
        "mem_desc" to "All compiled microservice deployments stored in local device memory slot banks.",
        "mem_empty" to "No microservices deployed yet. Head to the Forge deck to design and launch your database backend structure.",
        "sandbox_title" to "VIRTUAL SYSTEM PLAYGROUND",
        "sandbox_desc" to "Test mock live HTTP endpoints and execute database queries directly against the container host.",
        "router_title" to "LOCAL PROXY ROUTER (PORT ALLOCATOR)",
        "router_desc" to "All project containers are hosted locally via dynamic port forwarding allocations.",
        "search_label" to "Search deployments...",
        "status_active" to "STATION RUNNING",
        "status_stopped" to "STATION STOPPED",
        "auth_gate" to "GANCODE SECURE AUTH GATEWAY",
        "google_sign_in" to "Sign In with Google",
        "email_sign_in" to "Sign In with Developer Email",
        "email_placeholder" to "Enter email (e.g. mrdkyspys55@gmail.com)",
        "sql_terminal" to "POSTGRESQL SHELL CONSOLE",
        "sql_placeholder" to "Enter SQL query... e.g., SELECT * FROM tables",
        "run_sql" to "RUN SQL",
        "local_network" to "LOCAL HOSTPORT BINDINGS",
        "switch_lang" to "עברית",
        "sign_out" to "SIGN OUT",
        "connected_as" to "CONNECTED AS",
        "port_tester" to "PORT FIREWALL TESTER",
        "check_port" to "TEST LINK",
        "active_ports" to "MONITORED INTERNAL PORTS"
    )
    
    val he = mapOf(
        "app_title" to "גנקוד איי איי סטודיו",
        "app_subtitle" to "פורטל ענן ודוקר מקומי",
        "nav_forge" to "יצירה",
        "nav_memory" to "זיכרון מערכות",
        "nav_sandbox" to "ארגז חול",
        "nav_router" to "נתב פורטים",
        "forge_title" to "מחולל הענן האינסופי של גנקוד",
        "forge_desc" to "הזן את רעיון הארכיטקטורה שלך. מנוע גנקוד ימיר את הרעיון לקונטיינרים של Docker מקומיים, יקצה פורטים וסביבות ריצה פנימיות, ויבנה ממשקים אינטראקטיביים מעולים באופן מיידי.",
        "step1" to "שלב 1: הגדרת דרישות המערכת",
        "step2" to "שלב 2: שילוב אפליקטיבי ואינטגרציות",
        "placeholder_desc" to "תאר את המערכת שלך... לדוגמה: אפליקציית זימון תורים למאמני כושר עם תשלומים דרך Stripe...",
        "forge_button" to "צור והפעל מערכת מקומית חיה",
        "mem_title" to "ליבת הזיכרון של גנקוד",
        "mem_desc" to "כל שירותי המיקרו הפעילים המאוחסנים בבנק הזיכרון המקומי של המכשיר.",
        "mem_empty" to "לא נמצאו מערכות פעילות. עבור לטאב 'יצירה' כדי לבנות ולפרוס פרויקטים חדשים.",
        "sandbox_title" to "סביבת בדיקות ופיתוח",
        "sandbox_desc" to "בדוק נקודות קצה של HTTP והרץ שאילתות ישירות מול מסד הנתונים של הקונטיינר.",
        "router_title" to "נתב פרוקסי מקומי (מיפוי פורטים)",
        "router_desc" to "כל קונטיינרי הפרויקטים מאוחסנים ומנותבים מקומית על גבי פורטים דינמיים פנימיים.",
        "search_label" to "חפש פרויקטים...",
        "status_active" to "מערכת פועלת",
        "status_stopped" to "מערכת כבויה",
        "auth_gate" to "שער גישה מאובטח של גנקוד",
        "google_sign_in" to "התחברות מהירה באמצעות Google",
        "email_sign_in" to "התחברות באמצעות אימייל מפתח",
        "email_placeholder" to "הזן אימייל (למשל mrdkyspys55@gmail.com)",
        "sql_terminal" to "טרמינל שאילתות PostgreSQL",
        "sql_placeholder" to "הקלד שאילתת SQL... למשל: SELECT * FROM tables",
        "run_sql" to "הרץ שאילתה",
        "local_network" to "מיפויי רשת מקומיים (IP/Port)",
        "switch_lang" to "English",
        "sign_out" to "התנתק",
        "connected_as" to "מחובר כ-",
        "port_tester" to "בודק חומת אש ופורטים מקומי",
        "check_port" to "בדוק פורט",
        "active_ports" to "פורטים פנימיים מנוטרים"
    )
    
    return if (lang == "he") he[key] ?: en[key] ?: "" else en[key] ?: ""
}

// Custom design interactive Google Account auth gateway screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GancodeAuthScreen(viewModel: ProjectViewModel) {
    val lang by viewModel.language.collectAsStateWithLifecycle()
    var emailInput by remember { mutableStateOf("mrdkyspys55@gmail.com") }
    var isSimulatingLogin by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = SlateDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant top language switch bar during login
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { viewModel.toggleLanguage() },
                    colors = ButtonDefaults.textButtonColors(contentColor = ElectricCyan)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Language", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = getTxt("switch_lang", lang), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Cybernetic secure padlock canvas animation
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBackground)
                    .border(BorderStroke(1.5.dp, ElectricCyan), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(60.dp)) {
                    val w = size.width
                    val h = size.height
                    // Draw outer radar circles
                    drawCircle(
                        color = ElectricCyan.copy(alpha = 0.15f),
                        radius = h * 0.45f
                    )
                    drawCircle(
                        color = OrangeRouter.copy(alpha = 0.1f),
                        radius = h * 0.3f
                    )
                }
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock Secure",
                    tint = ElectricCyan,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "GANCODE CLOUD WORKSTATION",
                color = ElectricCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )

            Text(
                text = getTxt("auth_gate", lang),
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, Color(0xFF262A3E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = getTxt("email_sign_in", lang).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { 
                            emailInput = it 
                            authError = ""
                        },
                        placeholder = { Text(text = getTxt("email_placeholder", lang), color = SoftText, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("email_login_field"),
                        textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = Color(0xFF2A2F45),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )

                    if (authError.isNotEmpty()) {
                        Text(
                            text = authError,
                            color = LaserRed,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email auth login trigger
                    Button(
                        onClick = {
                            if (emailInput.trim().isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput.trim()).matches()) {
                                authError = if (lang == "he") "אימייל לא תקין" else "Please enter a valid developer email format"
                            } else {
                                isSimulatingLogin = true
                                scope.launch {
                                    delay(1000)
                                    viewModel.loginUser(emailInput.trim())
                                    isSimulatingLogin = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSimulatingLogin
                    ) {
                        if (isSimulatingLogin) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(text = getTxt("email_sign_in", lang), color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Separation Line
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(modifier = Modifier.weight(1f), color = Color(0xFF24283A))
                        Text(
                            text = if (lang == "he") "או" else "OR",
                            color = SoftText,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                        Divider(modifier = Modifier.weight(1f), color = Color(0xFF24283A))
                    }

                    // High Fidelity brand Google Sign-In button
                    Button(
                        onClick = {
                            isSimulatingLogin = true
                            scope.launch {
                                delay(1200) // Realistic Google API callback delay
                                // Log in as user mrdkyspys55@gmail.com instantly
                                viewModel.loginUser("mrdkyspys55@gmail.com", "mrdkyspys55")
                                isSimulatingLogin = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(BorderStroke(1.dp, Color(0xFF333852)), RoundedCornerShape(8.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B1E2E)),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSimulatingLogin
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Custom vector drawings for the letter "G" in Google Multi-Colors
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "G",
                                    color = Color(0xFF4285F4),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = getTxt("google_sign_in", lang),
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GancodeStudioApp(viewModel: ProjectViewModel) {
    val selectedProject by viewModel.selectedProject.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()
    val lang by viewModel.language.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    
    // Application state navigation
    var currentTab by remember { mutableStateOf(0) }
    var showProfileDropdown by remember { mutableStateOf(false) }

    // Enforce Google security auth gate initially
    if (!isUserLoggedIn) {
        GancodeAuthScreen(viewModel = viewModel)
        return;
    }

    Scaffold(
        bottomBar = {
            if (!isGenerating) {
                GancodeBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it },
                    lang = lang
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
            // High fidelity Cybertech Title Header with user dropdown details & language switches
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBackground)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
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
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = getTxt("app_title", lang),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 14.sp
                        )
                        Text(
                            text = getTxt("app_subtitle", lang).uppercase(),
                            color = ElectricCyan,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Header Control Tools: Language Switcher and Profile Sign-Out
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Bilingual Switch Button
                    TextButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier.padding(end = 4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (lang == "en") "עב" else "EN",
                            color = ElectricCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    // User Profile Button with SignOut Dropdown Menu
                    Box {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF282C40))
                                .border(BorderStroke(1.dp, ElectricCyan), RoundedCornerShape(14.dp))
                                .clickable { showProfileDropdown = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        DropdownMenu(
                            expanded = showProfileDropdown,
                            onDismissRequest = { showProfileDropdown = false },
                            modifier = Modifier.background(CardBackground).border(BorderStroke(1.dp, Color(0xFF2E334D)))
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(text = getTxt("connected_as", lang), color = SoftText, fontSize = 9.sp)
                                        Text(text = userEmail, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {},
                                enabled = false
                            )
                            Divider(color = Color(0xFF24283A))
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Exit", tint = LaserRed, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = getTxt("sign_out", lang), color = LaserRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    showProfileDropdown = false
                                    viewModel.logoutUser()
                                }
                            )
                        }
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
fun GancodeBottomBar(currentTab: Int, onTabSelected: (Int) -> Unit, lang: String) {
    NavigationBar(
        containerColor = CardBackground,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.navigationBarsPadding()
    ) {
        val tabs = listOf(
            TabItem(getTxt("nav_forge", lang), Icons.Default.Add, "Prompt & build"),
            TabItem(getTxt("nav_memory", lang), Icons.Default.List, "Saved Systems"),
            TabItem(getTxt("nav_sandbox", lang), Icons.Default.PlayArrow, "Simulated Runtime"),
            TabItem(getTxt("nav_router", lang), Icons.Default.Build, "Global DNS")
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
    val lang by viewModel.language.collectAsStateWithLifecycle()
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
                    text = getTxt("forge_title", lang),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = getTxt("forge_desc", lang),
                    color = SoftText,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Project text idea box
        Text(
            text = getTxt("step1", lang),
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
                    text = getTxt("placeholder_desc", lang),
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
            text = getTxt("step2", lang),
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
                    text = getTxt("forge_button", lang).uppercase(),
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
    val lang by viewModel.language.collectAsStateWithLifecycle()
    val projects by viewModel.filteredProjects.collectAsStateWithLifecycle()
    val selected by viewModel.selectedProject.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Search filter bar
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text(text = getTxt("search_label", lang), color = SoftText, fontSize = 13.sp) },
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
                            text = getTxt("mem_empty", lang),
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
                                        text = if (proj.status == "ACTIVE") getTxt("status_active", lang) else getTxt("status_stopped", lang),
                                        color = if (proj.status == "ACTIVE") NeonGreen else LaserRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            val localPort = 3000 + (proj.subdomain.hashCode().coerceAtLeast(0) % 5000)
                            Text(
                                text = "localhost/127.0.0.1 → port :$localPort",
                                color = ElectricCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${if (lang == "he") "קונספט והגדרת מערכת" else "Concept"}: ${proj.idea}",
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
    val lang by viewModel.language.collectAsStateWithLifecycle()
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
                text = if (lang == "he") "אין פרויקט פעיל" else "No System Selected",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (lang == "he") 
                    "צור מערכת חדשה בלשונית 'יצירה' או בחר אחת ממערכות הזיכרון כדי להתחיל להריץ שאילתות, לבדוק נקודות קצה (API) ולצפות בממשק המשתמש הפעיל."
                    else "Launch a system in 'Forge' or select an active database graph in 'Memory' to inspect running services, compile web previews, or dispatch requests.",
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
        // Quick project overview banner with local ports
        val currentLocalPort = 3000 + (proj.subdomain.hashCode().coerceAtLeast(0) % 5000)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1F31))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "DOCKER POD: ${proj.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(text = "LOCAL BINDING: http://127.0.0.1:$currentLocalPort", color = ElectricCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
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
                        text = if (proj.status == "ACTIVE") (if (lang == "he") "עצור מיכל" else "STOP POD") else (if (lang == "he") "הפעל מיכל" else "START POD"),
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
                text = { Text(if (lang == "he") "בדיקת API" else "API CLIENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) }
            )
            Tab(
                selected = currentSubTab == 1,
                onClick = { currentSubTab = 1 },
                text = { Text(if (lang == "he") "תוצאת ממשק" else "WEB PREVIEW", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) }
            )
            Tab(
                selected = currentSubTab == 2,
                onClick = { currentSubTab = 2 },
                text = { Text(if (lang == "he") "לוגים של הקונטיינר" else "POD LOGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) }
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

    val lang by viewModel.language.collectAsStateWithLifecycle()
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
            text = if (lang == "he") "בדיקת כתובות ומיקרו-שירותים" else "DISPATCH SYNTHETIC ENDPOINTS",
            color = ElectricCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = if (lang == "he") "בדוק נתיבי מיקרו-שירותים מקומיים ותגובות JSON מקוד השידור פנימית." else "Test live virtual microservices and route databases instantly.",
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
                    text = if (lang == "he") "ערוך גוף בקשת API (JSON):" else "EDIT JSON REQUEST BODY:",
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
                        Text(
                            text = if (lang == "he") "שלח בקשת $method לשרת פנימי" else "DISPATCH $method TO RUNTIME",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // REST Client response view
            if (sandboxResponse != null || isLoading) {
                Text(
                    text = if (lang == "he") "תגובת שרת שהתקבלה:" else "RESPONSE STACK:",
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
                        Text(
                            text = if (lang == "he") "HTTP/1.1 100 Continue...\nבהמתנה למענה ממיכל הריצה המאובטח..." else "HTTP/1.1 100 Continue...\nWaiting for isolated secure pod sandbox resolution...",
                            color = SoftText,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
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

        // PostgreSQL Relational Console Shell section
        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = Color(0xFF282C40), thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = getTxt("sql_terminal", lang),
            color = OrangeRouter,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = if (lang == "he") "שאילתות SQL בזמן אמת מול בסיס הנתונים PostgreSQL של ה-Pod המקומי." else "Execute real-time SQL statements directly against your isolated dev PostgreSQL container instance.",
            color = SoftText,
            fontSize = 10.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val consoleOut by viewModel.dbConsoleOutput.collectAsStateWithLifecycle()
        var sqlText by remember { mutableStateOf("SELECT * FROM tables") }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = sqlText,
                onValueChange = { sqlText = it },
                placeholder = { Text(text = getTxt("sql_placeholder", lang), color = SoftText, fontSize = 11.sp) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("sql_command_input"),
                textStyle = TextStyle(color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeRouter,
                    unfocusedBorderColor = Color(0xFF282C40),
                    focusedContainerColor = InsetConsole,
                    unfocusedContainerColor = InsetConsole,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (sqlText.trim().isNotEmpty()) {
                        viewModel.executeSql(sqlText)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangeRouter),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(52.dp)
            ) {
                Text(
                    text = getTxt("run_sql", lang),
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(InsetConsole)
                .border(BorderStroke(1.dp, Color(0xFF2E334D)), RoundedCornerShape(6.dp))
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = consoleOut,
                    color = Color(0xFFC5CBED),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 15.sp
                )
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

    val lang by viewModel.language.collectAsStateWithLifecycle()
    val pages = remember(proj) { viewModel.parsePages(proj.frontendPagesJson) }
    var selectedScreenIndex by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text(
            text = if (lang == "he") "תצוגה מקדימה של דפי האתר" else "LIVE COMPILED REACT WEB VIEW",
            color = ElectricCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        val currentLocalPort = 3000 + (proj.subdomain.hashCode().coerceAtLeast(0) % 5000)
        Text(
            text = if (lang == "he") 
                "מיפוי וירטואלי של ממשק משתמש בפורט מקומי: http://127.0.0.1:$currentLocalPort"
                else "Direct deployment preview for local port: http://127.0.0.1:$currentLocalPort",
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
    val lang by viewModel.language.collectAsStateWithLifecycle()
    val projects by viewModel.allProjects.collectAsStateWithLifecycle()
    var portTesterInput by remember { mutableStateOf("3000") }
    var testingPortStatus by remember { mutableStateOf("") }
    var isTestingPort by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = getTxt("local_network", lang).uppercase(),
            color = ElectricCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = if (lang == "he") "כלל קונטיינרי המערכות מנותבים מקומית למפתחי לולאה חוזרת (Localhost)." else "All virtual development containers are bound directly onto static loopback maps and local socket ports.",
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
                    text = if (lang == "he") "מיפויי כתובות רשת מקומיים" else "LOCAL LOOPOUT INTERFACE BINDINGS",
                    color = OrangeRouter,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Master Domain
                DnsMappingLine(label = "localhost:8080", resolvesTo = if (lang == "he") "ממשק המערכת הראשי" else "Master Portal (gancode_gui)")
                Spacer(modifier = Modifier.height(8.dp))
                DnsMappingLine(label = "127.0.0.1:9000", resolvesTo = if (lang == "he") "מנוע הליבה של גנקוד" else "Engine Core (gancode_daemon)")
                
                // Active container Subdomains mapped to local port allocations
                projects.filter { it.status == "ACTIVE" }.forEach { proj ->
                    val localPort = 3000 + (proj.subdomain.hashCode().coerceAtLeast(0) % 5000)
                    Spacer(modifier = Modifier.height(8.dp))
                    DnsMappingLine(
                        label = "127.0.0.1:$localPort",
                        resolvesTo = "Pod Container ID-docker_${proj.subdomain.take(6)}"
                    )
                }
            }
        }

        // Port firewall testing extension (Premium Extension Block!)
        Text(
            text = getTxt("port_tester", lang).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, Color(0xFF22283E)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (lang == "he") "בדוק וחבור פורטים באופן דינמי במיקרוספייר המקומי" else "Verify listening status and security boundaries of system socket connections.",
                    color = SoftText,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = portTesterInput,
                        onValueChange = { portTesterInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("port_tester_input"),
                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCyan,
                            unfocusedBorderColor = Color(0xFF2E334D),
                            focusedContainerColor = InsetConsole,
                            unfocusedContainerColor = InsetConsole
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (portTesterInput.trim().isNotEmpty()) {
                                isTestingPort = true
                                testingPortStatus = ""
                                scope.launch {
                                    val portInt = portTesterInput.trim().toIntOrNull() ?: 3000
                                    val isOnline = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                        try {
                                            val testSocket = java.net.Socket()
                                            testSocket.connect(java.net.InetSocketAddress("127.0.0.1", portInt), 500)
                                            testSocket.close()
                                            true
                                        } catch (e: Exception) {
                                            false
                                        }
                                    }
                                    testingPortStatus = if (isOnline) {
                                        if (lang == "he") {
                                            "פורט $portInt: פעיל ומגיב! (127.0.0.1 Connection Successful)"
                                        } else {
                                            "Port $portInt: ONLINE - TCP socket listening successfully (127.0.0.1 Binding OK)"
                                        }
                                    } else {
                                        if (lang == "he") {
                                            "פורט $portInt: סגור או לא פעיל בקונטיינר. (Connection Refused)"
                                        } else {
                                            "Port $portInt: OFFLINE - Connection Refused / No active socket listening."
                                        }
                                    }
                                    isTestingPort = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(50.dp),
                        enabled = !isTestingPort
                    ) {
                        if (isTestingPort) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp), strokeWidth = 1.5.dp)
                        } else {
                            Text(
                                text = getTxt("check_port", lang),
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (testingPortStatus.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF1B3D28))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = testingPortStatus,
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // DNS request analytics chart
        Text(
            text = if (lang == "he") "ניקוד בקשות לקונטיינרים פנימיים:" else "POD PORT PACKET LATENCY SCANNER:",
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
                    Text(text = if (lang == "he") "עומס ערוץ קלט/פלט" else "I/O Socket Packet Rate", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "8.2 MB/Sec", color = NeonGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
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
                    Text(text = "Proxy loopback delay: <1.1ms", color = SoftText, fontSize = 10.sp)
                    Text(text = "Local SSL Bind: ON (TLS 1.3 loopback)", color = ElectricCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
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
