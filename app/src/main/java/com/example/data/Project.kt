package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val idea: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String, // "PROVISIONING", "ACTIVE", "STOPPED"
    val subdomain: String,
    val backendFramework: String, // "Node.js / Express" or "Python / FastAPI"
    val backendEndpointsJson: String, // JSON list of endpoints
    val databaseSchemaJson: String, // JSON or descriptive DB structure
    val frontendPagesJson: String, // JSON list of React pages/views
    val envVariablesJson: String, // JSON map of .env parameters
    val apiIntegrationsJson: String, // JSON list of enabled integrations
    val terminalLogsJson: String // String log lines concatenated
)

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY timestamp DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Int): Project?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: Int)
}
