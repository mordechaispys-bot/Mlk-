package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.net.ServerSocket
import java.net.Socket
import java.net.InetSocketAddress
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class ProjectViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = ProjectRepository(database.projectDao())

    val allProjects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val activeServers = java.util.concurrent.ConcurrentHashMap<Int, kotlinx.coroutines.Job>()

    init {
        viewModelScope.launch {
            allProjects.collect { projects ->
                val activePorts = projects.filter { it.status == "ACTIVE" }.map { 
                    3000 + (it.subdomain.hashCode().coerceAtLeast(0) % 5000)
                }.toSet()
                
                val currentServers = activeServers.keys.toSet()
                currentServers.forEach { port ->
                    if (!activePorts.contains(port)) {
                        stopLocalSocketServer(port)
                    }
                }
                
                projects.forEach { proj ->
                    if (proj.status == "ACTIVE") {
                        val port = 3000 + (proj.subdomain.hashCode().coerceAtLeast(0) % 5000)
                        startLocalSocketServer(port, proj)
                    }
                }
            }
        }
    }

    private fun startLocalSocketServer(port: Int, project: Project) {
        if (activeServers.containsKey(port)) return
        
        val job = viewModelScope.launch(Dispatchers.IO) {
            var serverSocket: ServerSocket? = null
            try {
                serverSocket = ServerSocket(port)
                while (coroutineContext[kotlinx.coroutines.Job]?.isActive == true) {
                    val socket = try {
                        serverSocket.accept()
                    } catch (e: Exception) {
                        break
                    }
                    
                    launch(Dispatchers.IO) {
                        try {
                            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                            val writer = PrintWriter(socket.getOutputStream(), true)
                            
                            val reqLines = mutableListOf<String>()
                            var line: String? = reader.readLine()
                            while (line != null && line.trim().isNotEmpty()) {
                                reqLines.add(line)
                                line = reader.readLine()
                            }
                            
                            if (reqLines.isEmpty()) {
                                socket.close()
                                return@launch
                            }
                            
                            val firstLine = reqLines[0]
                            val parts = firstLine.split(" ")
                            val method = parts.getOrNull(0) ?: "GET"
                            val fullPath = parts.getOrNull(1) ?: "/"
                            
                            val endpoints = parseEndpoints(project.backendEndpointsJson)
                            val path = fullPath.substringBefore("?")
                            
                            val matched = endpoints.firstOrNull { 
                                it["path"] == path || it["path"] == "/$path" || "/${it["path"]}" == path 
                            } ?: endpoints.firstOrNull { path.startsWith(it["path"] ?: "---") }
                            
                            val responseBody = if (matched != null) {
                                matched["sampleResponse"] ?: "{}"
                            } else {
                                val err = JSONObject()
                                err.put("status", "error")
                                err.put("message", "404 Route Not Found")
                                err.put("requested_path", path)
                                val available = JSONArray()
                                endpoints.forEach { available.put(it["path"]) }
                                err.put("available_routes", available)
                                err.toString()
                            }
                            
                            writer.println("HTTP/1.1 200 OK")
                            writer.println("Content-Type: application/json; charset=utf-8")
                            writer.println("Access-Control-Allow-Origin: *")
                            writer.println("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS")
                            writer.println("Access-Control-Allow-Headers: *")
                            writer.println("Content-Length: ${responseBody.toByteArray(StandardCharsets.UTF_8).size}")
                            writer.println("Connection: close")
                            writer.println()
                            writer.println(responseBody)
                            writer.flush()
                        } catch (e: java.io.IOException) {
                            // Suppressed socket error
                        } finally {
                            try { socket.close() } catch (e: Exception) {}
                        }
                    }
                }
            } catch (e: Exception) {
                // Suppressed server socket error
            } finally {
                try { serverSocket?.close() } catch (e: Exception) {}
            }
        }
        activeServers[port] = job
    }

    private fun stopLocalSocketServer(port: Int) {
        activeServers.remove(port)?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        activeServers.keys.toSet().forEach { stopLocalSocketServer(it) }
    }

    private val _selectedProject = MutableStateFlow<Project?>(null)
    val selectedProject: StateFlow<Project?> = _selectedProject.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationLogs = MutableStateFlow<List<String>>(emptyList())
    val generationLogs: StateFlow<List<String>> = _generationLogs.asStateFlow()

    private val _currentLogsDisplay = MutableStateFlow<List<String>>(emptyList())
    val currentLogsDisplay: StateFlow<List<String>> = _currentLogsDisplay.asStateFlow()

    // Interactive Sandbox testing states
    private val _sandboxSelectedEndpoint = MutableStateFlow<String?>(null)
    val sandboxSelectedEndpoint: StateFlow<String?> = _sandboxSelectedEndpoint.asStateFlow()

    private val _sandboxRequestBody = MutableStateFlow("")
    val sandboxRequestBody: StateFlow<String> = _sandboxRequestBody.asStateFlow()

    private val _sandboxResponse = MutableStateFlow<String?>(null)
    val sandboxResponse: StateFlow<String?> = _sandboxResponse.asStateFlow()

    private val _sandboxLoading = MutableStateFlow(false)
    val sandboxLoading: StateFlow<Boolean> = _sandboxLoading.asStateFlow()

    // Search & filter
    val searchQuery = MutableStateFlow("")

    // Bilingual Support
    private val _language = MutableStateFlow("en") // "en" or "he"
    val language: StateFlow<String> = _language.asStateFlow()

    fun toggleLanguage() {
        _language.value = if (_language.value == "en") "he" else "en"
    }

    // Google / Email Sign-In simulation states
    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _userEmail = MutableStateFlow("mrdkyspys55@gmail.com")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userName = MutableStateFlow("mrdkyspys55")
    val userName: StateFlow<String> = _userName.asStateFlow()

    fun loginUser(email: String, name: String = "") {
        _userEmail.value = email.trim().ifEmpty { "mrdkyspys55@gmail.com" }
        _userName.value = if (name.trim().isNotEmpty()) name else email.split("@").first()
        _isUserLoggedIn.value = true
    }

    fun logoutUser() {
        _isUserLoggedIn.value = false
    }

    // PostgreSQL Console Shell Simulator
    private val _dbConsoleOutput = MutableStateFlow("-- PostgreSQL database terminal ready.\n-- Running queries locally in container context.\n-- Try: SELECT * FROM tables\n-- Or: SELECT * FROM members")
    val dbConsoleOutput: StateFlow<String> = _dbConsoleOutput.asStateFlow()

    fun executeSql(query: String) {
        viewModelScope.launch {
            if (query.trim().isEmpty()) return@launch
            _dbConsoleOutput.value = "Executing: $query on local postgres instance..."
            delay(500)
            val lowercase = query.lowercase(Locale.ROOT)
            if (lowercase.contains("select * from tables") || lowercase.contains("show tables")) {
                val tables = selectedProject.value?.let { parseSchema(it.databaseSchemaJson) } ?: emptyList()
                if (tables.isEmpty()) {
                    _dbConsoleOutput.value = "postgres=# SELECT * FROM tables;\n(0 rows) - No local database tables configured yet."
                } else {
                    val sb = StringBuilder("postgres=# SELECT * FROM tables;\n")
                    sb.append(String.format(Locale.ROOT, "%-16s | %-32s\n", "TABLE NAME", "COLUMN DEFINITIONS"))
                    sb.append("-----------------+----------------------------------------------------\n")
                    tables.forEach { tableMap ->
                        val tableName = tableMap["table"] as? String ?: "unknown"
                        @Suppress("UNCHECKED_CAST")
                        val cols = tableMap["columns"] as? List<Map<String, String>> ?: emptyList()
                        val colStr = cols.joinToString(", ") { "${it["name"]}:${it["type"]}" }
                        sb.append(String.format(Locale.ROOT, "%-16s | %-32s\n", tableName, colStr))
                    }
                    _dbConsoleOutput.value = sb.toString()
                }
            } else if (lowercase.contains("select") && (lowercase.contains("members") || lowercase.contains("workouts") || lowercase.contains("products") || lowercase.contains("orders") || lowercase.contains("slots") || lowercase.contains("appointments") || lowercase.contains("resources") || lowercase.contains("tasks"))) {
                val records = when {
                    lowercase.contains("members") -> """
                     id |      email      |        created_at       
                    ----+-----------------+-------------------------
                      1 | user@gancode.ai | 2026-06-11 09:25:12.784
                      2 | dem@example.com | 2026-06-11 09:41:03.112
                    (2 rows)
                    """.trimIndent()
                    lowercase.contains("workouts") -> """
                     id | member_id |      activity       | duration_mins 
                    ----+-----------+---------------------+---------------
                    101 |         1 | Morning Cardio Burn |            30
                    102 |         2 | Hypertrophy Push    |            45
                    (2 rows)
                    """.trimIndent()
                    lowercase.contains("products") -> """
                     id |         title         | price_cents | stock_qty 
                    ----+-----------------------+-------------+-----------
                      1 | Minimalist Work Desk  |       24900 |        12
                      2 | Ergonomic Mesh Chair  |       18900 |        34
                    (2 rows)
                    """.trimIndent()
                    lowercase.contains("orders") -> """
                     id |    invoice_ref    | total_cents |  status   
                    ----+-------------------+-------------+-----------
                    542 | inv_99812_checkout|       43800 | COMPLETED
                    (1 row)
                    """.trimIndent()
                    else -> """
                     id |       name       |  status  |  created_by   
                    ----+------------------+----------+---------------
                      1 | Asset Alpha Pro  | ACTIVE   | console_agent
                      2 | Backup Sync-Daemon| COMPLETED| mrdkyspys55
                    (2 rows)
                    """.trimIndent()
                }
                _dbConsoleOutput.value = "postgres=# $query\n$records"
            } else if (lowercase.contains("insert") || lowercase.contains("update") || lowercase.contains("delete")) {
                _dbConsoleOutput.value = "postgres=# $query\nQuery executed successfully. Row affected: 1."
            } else {
                _dbConsoleOutput.value = "postgres=# $query\nERROR: relation does not exist or syntax error near \"${query.trim().split(" ").firstOrNull() ?: ""}\"\nTry running: 'SELECT * FROM tables' or select from one of the custom project tables listed above."
            }
        }
    }

    val filteredProjects: StateFlow<List<Project>> = combine(allProjects, searchQuery) { list, query ->
        if (query.trim().isEmpty()) {
            list
        } else {
            list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.idea.contains(query, ignoreCase = true) ||
                it.subdomain.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectProject(project: Project?) {
        _selectedProject.value = project
        _sandboxResponse.value = null
        if (project != null) {
            // Pick first endpoint as default test endpoint
            val endpoints = parseEndpoints(project.backendEndpointsJson)
            val firstEndpoint = endpoints.firstOrNull()?.get("path") ?: ""
            _sandboxSelectedEndpoint.value = firstEndpoint
            _sandboxRequestBody.value = endpoints.firstOrNull()?.get("inputDesc") ?: "{}"
        } else {
            _sandboxSelectedEndpoint.value = null
        }
    }

    fun updateSelectedEndpoint(path: String, inputDesc: String) {
        _sandboxSelectedEndpoint.value = path
        _sandboxRequestBody.value = inputDesc
        _sandboxResponse.value = null
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repository.deleteProject(project)
            if (selectedProject.value?.id == project.id) {
                _selectedProject.value = null
            }
        }
    }

    fun startGeneratingProject(idea: String, selectedIntegrations: List<String>) {
        if (idea.trim().isEmpty()) return

        viewModelScope.launch {
            _isGenerating.value = true
            _currentLogsDisplay.value = emptyList()
            val logs = mutableListOf<String>()

            fun addLog(msg: String) {
                logs.add(msg)
                _currentLogsDisplay.value = logs.toList()
            }

            // Phase 1: Booting Orchestrator
            addLog("[GANCODE ENGINE] Booting local orchestrator node...")
            delay(300)
            addLog("[AI-ORCHESTRATOR] Analyzing project criteria and structural bounds...")
            delay(400)
            addLog("[AI-ORCHESTRATOR] Selected targeted cloud cluster: fly-io-isolated-k8s")
            delay(300)

            // Phase 2: Calling API / Or Fallback Solver
            addLog("[CODEGEN] Invoking AI intelligence model (gemini-3.5-flash)...")
            val geminiResponse = withContext(Dispatchers.IO) {
                GeminiClient.parseAndCallGemini(idea)
            }

            val projectDetails: LocalProjectParsed = if (geminiResponse != null) {
                try {
                    addLog("[CODEGEN] AI response received! Successfully extracted architectural code matrix.")
                    parseGeminiResponse(geminiResponse, idea, selectedIntegrations)
                } catch (e: Exception) {
                    addLog("[CODEGEN] Parse failed on AI payload, initializing smart local matrix compiler...")
                    buildLocalFallbackDetails(idea, selectedIntegrations)
                }
            } else {
                addLog("[CODEGEN] (No custom Gemini key found in platform secrets or key limits hit).")
                addLog("[CODEGEN] Running Gancode's pre-compiled architecture and local blueprints synthesis...")
                delay(1200)
                buildLocalFallbackDetails(idea, selectedIntegrations)
            }

            // Phase 3: Simulated Container Provisioning
            val localPort = 3000 + (projectDetails.subdomain.hashCode().coerceAtLeast(0) % 5000)
            addLog("[CONTAINER] Allocating local container slot, binding local interface socket at port :$localPort")
            delay(400)
            addLog("[DOCKER] pulling base system runtime for ${projectDetails.framework}...")
            delay(500)
            addLog("[DOCKER] base layer pulled. Status: sha256:88ad7fc911b")
            delay(400)
            addLog("[CODEGEN] Initializing environment mapping template and service layers...")
            delay(450)

            // Dynamic setup depending on selected integrations
            selectedIntegrations.forEach { service ->
                addLog("[API] Auto-weaving modular dependency integration: $service Webhook setup...")
                delay(300)
                if (service == "Stripe") {
                    addLog("[API] Injected client dependencies: @stripe/stripe-js, config stripe_api_key")
                } else if (service == "Supabase") {
                    addLog("[API] Injected service layer: @supabase/supabase-js initialized")
                }
                delay(200)
            }

            addLog("[BUILD] Compiling React static components into high-performance web previews...")
            delay(600)
            addLog("[DB] Provisioning PostgreSQL relational cluster database instance...")
            delay(500)
            addLog("[DB] Running default Schema scripts on Database connection...")
            delay(400)
            addLog("[ROUTER] Local mapping established: http://127.0.0.1:$localPort and local network IP http://192.168.1.150:$localPort")
            delay(400)
            addLog("[SYSTEM] Booting application services inside container...")
            delay(500)
            addLog("[SYSTEM] Container boot up succeeded! Live Preview dashboard and Backend APIs are running.")
            addLog("[HEALTHCHECK] 200 OK received for /health endpoint. App status: ACTIVE")

            val logsCompiled = logs.joinToString("\n")

            val project = Project(
                name = projectDetails.name,
                idea = idea,
                timestamp = System.currentTimeMillis(),
                status = "ACTIVE",
                subdomain = projectDetails.subdomain,
                backendFramework = projectDetails.framework,
                backendEndpointsJson = projectDetails.endpointsJson,
                databaseSchemaJson = projectDetails.schemaJson,
                frontendPagesJson = projectDetails.pagesJson,
                envVariablesJson = projectDetails.envJson,
                apiIntegrationsJson = compileIntegrationsJson(selectedIntegrations),
                terminalLogsJson = logsCompiled
            )

            val projectId = repository.insertProject(project)
            val savedProject = project.copy(id = projectId.toInt())
            _selectedProject.value = savedProject
            _isGenerating.value = false

            // Default sandbox selection
            val endpoints = parseEndpoints(savedProject.backendEndpointsJson)
            val firstPath = endpoints.firstOrNull()?.get("path") ?: ""
            _sandboxSelectedEndpoint.value = firstPath
            _sandboxRequestBody.value = endpoints.firstOrNull()?.get("inputDesc") ?: "{}"
            _sandboxResponse.value = null
        }
    }

    // Trigger virtual testing endpoint simulation
    fun triggerVirtualRequest(method: String, endpointName: String, body: String) {
        viewModelScope.launch {
            _sandboxLoading.value = true
            _sandboxResponse.value = null
            delay(500) // Small delay for visual response

            val selected = selectedProject.value
            if (selected == null) {
                _sandboxResponse.value = "{ \"error\": \"No project active\" }"
                _sandboxLoading.value = false
                return@launch
            }

            if (selected.status != "ACTIVE") {
                _sandboxResponse.value = """
                    {
                      "error": "Connection Refused",
                      "host": "127.0.0.1",
                      "port": ${3000 + (selected.subdomain.hashCode().coerceAtLeast(0) % 5000)},
                      "details": "Local developer container is STOPPED. Please click 'START POD' to boot the service socket."
                    }
                """.trimIndent()
                _sandboxLoading.value = false
                return@launch
            }

            val localPort = 3000 + (selected.subdomain.hashCode().coerceAtLeast(0) % 5000)
            
            // Execute actual network request to the real unprivileged local port!
            val responseText = withContext(Dispatchers.IO) {
                try {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(2, java.util.concurrent.TimeUnit.SECONDS)
                        .build()

                    val url = "http://127.0.0.1:$localPort$endpointName"
                    val requestBuilder = Request.Builder().url(url)
                    
                    if (method == "POST" || method == "PUT") {
                        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                        val reqBody = body.toRequestBody(mediaType)
                        if (method == "POST") requestBuilder.post(reqBody) else requestBuilder.put(reqBody)
                    } else if (method == "DELETE") {
                        requestBuilder.delete()
                    } else {
                        requestBuilder.get()
                    }

                    client.newCall(requestBuilder.build()).execute().use { response ->
                        if (response.isSuccessful) {
                            response.body?.string() ?: "{}"
                        } else {
                            JSONObject().apply {
                                put("status", "error")
                                put("code", response.code)
                                put("message", response.message)
                            }.toString(2)
                        }
                    }
                } catch (e: Exception) {
                    JSONObject().apply {
                        put("error", "Connection Refused")
                        put("host", "127.0.0.1")
                        put("port", localPort)
                        put("exception", e.javaClass.simpleName)
                        put("message", e.localizedMessage ?: "Connection reset by peer")
                        put("troubleshoot", "Verify that Gancode unprivileged container daemon has started successfully.")
                    }.toString(2)
                }
            }

            _sandboxResponse.value = responseText
            _sandboxLoading.value = false
        }
    }

    // Toggle container running state
    fun toggleProjectState() {
        val current = _selectedProject.value ?: return
        viewModelScope.launch {
            val nextStatus = if (current.status == "ACTIVE") "STOPPED" else "ACTIVE"
            val updated = current.copy(status = nextStatus)
            repository.updateProject(updated)
            _selectedProject.value = updated
        }
    }

    // Parse JSON lists safely
    fun parseEndpoints(jsonStr: String): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val map = mutableMapOf<String, String>()
                map["method"] = obj.optString("method", "GET")
                map["path"] = obj.optString("path", "/")
                map["desc"] = obj.optString("desc", "")
                map["sampleResponse"] = obj.optString("sampleResponse", "{}")
                
                // Ingest custom payload schemas
                val inputDesc = if (map["method"] == "POST" || map["method"] == "PUT") {
                    val defaultBody = JSONObject()
                    if (map["path"]?.contains("register") == true || map["path"]?.contains("auth") == true) {
                        defaultBody.put("email", "test@gancode.ai")
                        defaultBody.put("password", "gancode_secure_pass123")
                    } else if (map["path"]?.contains("task") == true || map["path"]?.contains("todo") == true) {
                        defaultBody.put("title", "Finish dynamic testing")
                        defaultBody.put("priority", "high")
                    } else {
                        defaultBody.put("name", "Sandbox Parameter")
                        defaultBody.put("value", "custom-input-101")
                    }
                    defaultBody.toString(2)
                } else {
                    "{}"
                }
                map["inputDesc"] = inputDesc
                list.add(map)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun parseSchema(jsonStr: String): List<Map<String, Any>> {
        val list = mutableListOf<Map<String, Any>>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val tableMap = mutableMapOf<String, Any>()
                tableMap["table"] = obj.optString("table", "unknown_table")
                
                val columnsArr = obj.optJSONArray("columns")
                val colsList = mutableListOf<Map<String, String>>()
                if (columnsArr != null) {
                    for (j in 0 until columnsArr.length()) {
                        val colObj = columnsArr.getJSONObject(j)
                        colsList.add(mapOf(
                            "name" to colObj.optString("name", "col"),
                            "type" to colObj.optString("type", "VARCHAR")
                        ))
                    }
                }
                tableMap["columns"] = colsList
                list.add(tableMap)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun parsePages(jsonStr: String): List<Map<String, String>> {
        val list = mutableListOf<Map<String, String>>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val map = mutableMapOf<String, String>()
                map["name"] = obj.optString("name", "Screen")
                map["route"] = obj.optString("route", "/")
                map["components"] = obj.optString("components", "")
                map["desc"] = obj.optString("desc", "")
                list.add(map)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun parseEnv(jsonStr: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val obj = JSONObject(jsonStr)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.optString(key, "")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return map
    }

    private fun compileIntegrationsJson(list: List<String>): String {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        return arr.toString()
    }

    // Helper classes
    private data class LocalProjectParsed(
        val name: String,
        val subdomain: String,
        val framework: String,
        val endpointsJson: String,
        val schemaJson: String,
        val pagesJson: String,
        val envJson: String
    )

    // Parse the actual raw JSON from Gemini
    private fun parseGeminiResponse(rawJson: String, idea: String, integrations: List<String>): LocalProjectParsed {
        // Standardize the raw text: remove any markdown wrapping in case Gemini sent ```json ... ```
        var cleanJson = rawJson.trim()
        if (cleanJson.startsWith("```json")) {
            cleanJson = cleanJson.substringAfter("```json").substringBeforeLast("```").trim()
        } else if (cleanJson.startsWith("```")) {
            cleanJson = cleanJson.substringAfter("```").substringBeforeLast("```").trim()
        }

        val json = JSONObject(cleanJson)
        val name = json.optString("name", buildDefaultName(idea))
        val subdomain = json.optString("subdomain", generateSubdomain(name))
        val framework = json.optString("framework", "Node.js / Express")
        val endpoints = json.optJSONArray("endpoints")?.toString() ?: "[]"
        val schema = json.optJSONArray("schema")?.toString() ?: "[]"
        val pages = json.optJSONArray("pages")?.toString() ?: "[]"
        
        // Formulate env variable merges
        val envObj = json.optJSONObject("env") ?: JSONObject()
        integrations.forEach {
            if (it == "Stripe") {
                envObj.put("STRIPE_SECRET_KEY", "sk_test_51Ng...")
                envObj.put("STRIPE_WEBHOOK_SECRET", "whsec_...")
            } else if (it == "Supabase") {
                envObj.put("SUPABASE_URL", "https://gancode-proj.supabase.co")
                envObj.put("SUPABASE_ANON_KEY", "eyJhbGciOiJIUz...")
            } else if (it == "Google Maps") {
                envObj.put("GOOGLE_MAPS_API_KEY", "AIzaSy...")
            } else if (it == "OpenAI / LLM API") {
                envObj.put("OPENAI_API_KEY", "sk-proj-...")
            }
        }
        val envJson = envObj.toString()

        return LocalProjectParsed(
            name = name,
            subdomain = subdomain,
            framework = framework,
            endpointsJson = endpoints,
            schemaJson = schema,
            pagesJson = pages,
            envJson = envJson
        )
    }

    private fun generateSubdomain(name: String): String {
        return name.lowercase(Locale.ROOT)
            .replace("[^a-z0-9\\s]".toRegex(), "")
            .replace("\\s+".toRegex(), "-")
            .take(15) + "-" + (100..999).random()
    }

    private fun buildDefaultName(idea: String): String {
        val words = idea.split(" ").filter { it.length > 2 }.take(2)
        return if (words.isNotEmpty()) {
            words.joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } } + " Live API"
        } else {
            "Gancode Engine App"
        }
    }

    // Precise Local Blueprints generator for fallback
    private fun buildLocalFallbackDetails(idea: String, integrations: List<String>): LocalProjectParsed {
        val lowercaseIdea = idea.lowercase(Locale.ROOT)
        
        val name: String
        val subdomain: String
        val framework: String
        val endpointsJson: String
        val schemaJson: String
        val pagesJson: String
        val envObj = JSONObject()

        if (lowercaseIdea.contains("fit") || lowercaseIdea.contains("gym") || lowercaseIdea.contains("sport")) {
            name = "FitPulse Engine Core"
            subdomain = "fitpulse-" + (1000..9999).random()
            framework = "Python / FastAPI"
            
            endpointsJson = """
            [
              {"method": "POST", "path": "/api/auth/register", "desc": "Registers gym member and configures credentials", "sampleResponse": "{\"status\":\"success\",\"uid\":\"usr_8321\",\"email\":\"test@gancode.ai\"}"},
              {"method": "GET", "path": "/api/workouts", "desc": "Fetches tailored workout activities and routine sheets", "sampleResponse": "[{\"id\":101,\"name\":\"Morning Cardio Burn\",\"duration_mins\":30},{\"id\":102,\"name\":\"Hypertrophy Push Lift\",\"duration_mins\":45}]"},
              {"method": "POST", "path": "/api/workouts/track", "desc": "Logs active performance calories and heartrate parameters", "sampleResponse": "{\"status\":\"tracked\",\"logged_id\":9814,\"calories\":385}"}
            ]
            """.trimIndent()

            schemaJson = """
            [
              {"table": "members", "columns": [{"name": "id", "type": "SERIAL PRIMARY KEY"}, {"name": "email", "type": "VARCHAR(255) UNIQUE"}, {"name": "created_at", "type": "TIMESTAMP"}]},
              {"table": "workouts", "columns": [{"name": "id", "type": "SERIAL PRIMARY KEY"}, {"name": "member_id", "type": "INT REFERENCES members(id)"}, {"name": "activity", "type": "VARCHAR"}, {"name": "duration_mins", "type": "INT"}]}
            ]
            """.trimIndent()

            pagesJson = """
            [
              {"name": "Client Dashboard", "route": "/", "components": "Header, StatsCardsSummary, ActivityChart, WorkoutsFeed", "desc": "Displays live metrics summaries, streaks, and user activities feed"},
              {"name": "Workout Tracker", "route": "/track", "components": "TrackerForm, ActiveTimerRow, SaveWorkoutButton", "desc": "Allows members to log active workouts and set dynamic targets"}
            ]
            """.trimIndent()

            envObj.put("DATABASE_URL", "postgresql://gancode_fitpulse:secure_secret@pg-instance:5432/db")
            envObj.put("FASTAPI_ENV", "production")
            envObj.put("PORT", "8000")
        } 
        else if (lowercaseIdea.contains("shop") || lowercaseIdea.contains("store") || lowercaseIdea.contains("order") || lowercaseIdea.contains("retail")) {
            name = "Vendora Cloud Retail"
            subdomain = "vendora-" + (1000..9999).random()
            framework = "Node.js / Express"

            endpointsJson = """
            [
              {"method": "GET", "path": "/api/products", "desc": "Retrieves digital catalog and database stocking levels", "sampleResponse": "[{\"id\":1,\"title\":\"Minimalist Work Desk\",\"price\":249,\"stock\":12},{\"id\":2,\"title\":\"Ergonomic Mesh Chair\",\"price\":189,\"stock\":34}]"},
              {"method": "POST", "path": "/api/orders/checkout", "desc": "Initializes transactional ordering and processes stripe sessions", "sampleResponse": "{\"status\":\"order_created\",\"invoice_id\":\"inv_99812\",\"total_cents\":43800}"},
              {"method": "GET", "path": "/api/orders/history", "desc": "Extracts past transactions for billing audit reviews", "sampleResponse": "[{\"id\":542,\"total_cents\":12900,\"date\":\"2026-06-10\"}]"}
            ]
            """.trimIndent()

            schemaJson = """
            [
              {"table": "products", "columns": [{"name": "id", "type": "SERIAL PRIMARY KEY"}, {"name": "title", "type": "VARCHAR(500)"}, {"name": "price_cents", "type": "INT"}, {"name": "stock_qty", "type": "INT"}]},
              {"table": "orders", "columns": [{"name": "id", "type": "SERIAL PRIMARY KEY"}, {"name": "invoice_ref", "type": "VARCHAR"}, {"name": "total_cents", "type": "INT"}, {"name": "status", "type": "VARCHAR"}]}
            ]
            """.trimIndent()

            pagesJson = """
            [
              {"name": "Retail Storefront", "route": "/", "components": "NavBar, HeroBanner, ProductsGrid, CartDrawer", "desc": "Beautiful e-commerce view supporting items search, dynamic carting, and checkout"},
              {"name": "Merchant Analytics", "route": "/admin", "components": "BillingStatsPanel, OrdersTable, InventoryManager", "desc": "Admin hub displaying real time sales curves, inventory charts, and invoicing"}
            ]
            """.trimIndent()

            envObj.put("DATABASE_URL", "postgresql://vendora_db_admin:root_p_990@host-pg-secure:5432/retail")
            envObj.put("NODE_ENV", "production")
            envObj.put("PORT", "3080")
        }
        else if (lowercaseIdea.contains("booking") || lowercaseIdea.contains("reserve") || lowercaseIdea.contains("appointment") || lowercaseIdea.contains("schedule")) {
            name = "Bookify Live Orchestrator"
            subdomain = "bookify-" + (1000..9999).random()
            framework = "Python / FastAPI"

            endpointsJson = """
            [
              {"method": "GET", "path": "/api/slots", "desc": "Fetches available timeslots and reservation windows", "sampleResponse": "[{\"time\":\"10:00AM\",\"available\":true},{\"time\":\"11:30AM\",\"available\":false}]"},
              {"method": "POST", "path": "/api/reserve", "desc": "Locks selected calendar spot and registers clients profile", "sampleResponse": "{\"status\":\"confirmed\",\"appointment_id\":\"apt_5541\",\"time\":\"10:00AM\"}"},
              {"method": "PUT", "path": "/api/reserve/cancel", "desc": "Releases specific slots to open queues", "sampleResponse": "{\"status\":\"cancelled\",\"refund_initiated\":true}"}
            ]
            """.trimIndent()

            schemaJson = """
            [
              {"table": "slots", "columns": [{"name": "id", "type": "SERIAL PRIMARY KEY"}, {"name": "time_slot", "type": "VARCHAR"}, {"name": "is_booked", "type": "BOOLEAN"}]},
              {"table": "appointments", "columns": [{"name": "id", "type": "SERIAL PRIMARY KEY"}, {"name": "client_name", "type": "VARCHAR"}, {"name": "slot_id", "type": "INT REFERENCES slots(id)"}]}
            ]
            """.trimIndent()

            pagesJson = """
            [
              {"name": "Client Booking Calendar", "route": "/", "components": "Header, DateSelector, SlotsGrid, BookingModal", "desc": "Client calendar selection for allocating available appointments"},
              {"name": "Organizer Dashboard", "route": "/dashboard", "components": "ActiveSchedulesList, ClientListTable, SlotConfig", "desc": "Corporate overview to schedule staff time blocks, manage conflicts, and download reports"}
            ]
            """.trimIndent()

            envObj.put("DATABASE_URL", "postgresql://bookify_user:test_p@localhost:5432/scheduling")
            envObj.put("PORT", "8000")
        }
        else {
            // General customized fallback
            val baseName = idea.trim().split(" ").take(2).joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
            name = if (baseName.length > 3) "$baseName Core Engine" else "Gancode System Pro"
            val baseSlug = if (baseName.length > 3) baseName.lowercase(Locale.ROOT).replace(" ", "-") else "system-app"
            subdomain = "$baseSlug-" + (100..999).random()
            framework = "Node.js / Express"

            endpointsJson = """
            [
              {"method": "GET", "path": "/api/dashboard/summary", "desc": "Aggregates key metrics for visual presentation on the frontend", "sampleResponse": "{\"active_users\":3412,\"system_load\":\"12%\",\"monthly_growth\":\"+24%\"}"},
              {"method": "POST", "path": "/api/resource/create", "desc": "Generates data records and updates memory layer", "sampleResponse": "{\"status\":\"success\",\"resource_id\":12502,\"payload_processed\":true}"},
              {"method": "GET", "path": "/api/resource/list", "desc": "Retrieves paginated dynamic logs", "sampleResponse": "[{\"id\":1,\"name\":\"Item Asset Alpha\",\"value\":\"active\"},{\"id\":2,\"name\":\"Item Asset Beta\",\"value\":\"pending\"}]"}
            ]
            """.trimIndent()

            schemaJson = """
            [
              {"table": "resources", "columns": [{"name": "id", "type": "SERIAL PRIMARY KEY"}, {"name": "name", "type": "VARCHAR(255)"}, {"name": "status", "type": "VARCHAR"}, {"name": "created_by", "type": "VARCHAR"}]}
            ]
            """.trimIndent()

            pagesJson = """
            [
              {"name": "Performance Dashboard", "route": "/", "components": "NavBar, SummaryCards, ResourceTable, CreationDialog", "desc": "Primary user hub for analyzing live items list, creating records, and generating telemetry logs"}
            ]
            """.trimIndent()

            envObj.put("DATABASE_URL", "postgresql://cloud_user:v_1_secret_99@pg-cluster-host:5432/main_db")
            envObj.put("PORT", "3000")
            envObj.put("NODE_ENV", "production")
        }

        integrations.forEach {
            if (it == "Stripe") {
                envObj.put("STRIPE_API_KEY", "sk_test_mock_stripe_key_0091")
            } else if (it == "Supabase") {
                envObj.put("SUPABASE_URL", "https://gancode-proj.supabase.co")
                envObj.put("SUPABASE_ANON_KEY", "eyJhbGciOiJIUz...")
            } else if (it == "Google Maps") {
                envObj.put("GOOGLE_MAPS_API_KEY", "AIzaSyMockKeyForMaps")
            } else if (it == "OpenAI / LLM API") {
                envObj.put("OPENAI_API_KEY", "sk-proj-mock-openai-key")
            }
        }

        return LocalProjectParsed(
            name = name,
            subdomain = subdomain,
            framework = framework,
            endpointsJson = endpointsJson,
            schemaJson = schemaJson,
            pagesJson = pagesJson,
            envJson = envObj.toString()
        )
    }
}
