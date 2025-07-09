
package io.shubham0204.smollmandroid.ui.screens.model_download

import android.app.Application
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.shubham0204.hf_model_hub_api.HFModelsAPI
import io.shubham0204.hf_model_hub_api.HFModelInfo
import io.shubham0204.hf_model_hub_api.HFModelTree
import io.shubham0204.smollm.GGUFReader
import io.shubham0204.smollm.SmolLM
import io.shubham0204.smollmandroid.R
import io.shubham0204.smollmandroid.data.AppDB
import io.shubham0204.smollmandroid.data.LLMModel
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class DownloadModelsViewModel(
    private val app: Application,
    private val appDB: AppDB
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(DownloadModelsUIState())
    val uiState: StateFlow<DownloadModelsUIState> = _uiState

    private val _filePickerUIState = MutableStateFlow(FilePickerUIState())
    val filePickerUIState: StateFlow<FilePickerUIState> = _filePickerUIState

    private val hfModelsAPI = HFModelsAPI()

    fun downloadGemmaModel() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDownloading = true,
                downloadProgress = 0f,
                downloadStatus = "Preparing to download Gemma model..."
            )

            try {
                val modelRepo = "unsloth/gemma-3n-E2B-it-GGUF"
                val fileName = "gemma-3n-E2B-it-Q4_K_M.gguf"
                
                // Get model info and tree
                val modelInfo = hfModelsAPI.getModelInfo(modelRepo)
                val modelTree = hfModelsAPI.getModelTree(modelRepo)
                
                val ggufFiles = modelTree.filter { 
                    it.path.endsWith(".gguf") && it.path.contains(fileName)
                }
                
                if (ggufFiles.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isDownloading = false,
                        errorMessage = "GGUF file not found in model repository"
                    )
                    return@launch
                }
                
                val file = ggufFiles.first()
                val downloadUrl = "https://huggingface.co/${modelRepo}/resolve/main/${file.path}"
                
                _uiState.value = _uiState.value.copy(
                    downloadStatus = "Downloading ${file.path}..."
                )
                
                downloadModelFile(downloadUrl, fileName, modelInfo)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDownloading = false,
                    errorMessage = "Download failed: ${e.message}"
                )
            }
        }
    }

    private suspend fun downloadModelFile(url: String, fileName: String, modelInfo: HFModelInfo) {
        withContext(Dispatchers.IO) {
            try {
                val modelsDir = File(app.filesDir, "models")
                if (!modelsDir.exists()) {
                    modelsDir.mkdirs()
                }
                
                val modelFile = File(modelsDir, fileName)
                val connection = URL(url).openConnection()
                val totalSize = connection.contentLength
                
                connection.getInputStream().use { input ->
                    FileOutputStream(modelFile).use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead = 0
                        var totalBytesRead = 0
                        
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalBytesRead += bytesRead
                            
                            val progress = if (totalSize > 0) {
                                totalBytesRead.toFloat() / totalSize.toFloat()
                            } else 0f
                            
                            _uiState.value = _uiState.value.copy(
                                downloadProgress = progress,
                                downloadStatus = "Downloading... ${(progress * 100).toInt()}%"
                            )
                        }
                    }
                }
                
                // Verify and save to database
                val ggufReader = GGUFReader()
                val isValidGGUF = ggufReader.isValidGGUFFile(modelFile.absolutePath)
                
                if (isValidGGUF) {
                    val llmModel = LLMModel(
                        name = fileName.removeSuffix(".gguf"),
                        path = modelFile.absolutePath,
                        size = modelFile.length(),
                        isDownloaded = true,
                        source = "HuggingFace"
                    )
                    
                    appDB.llmModelDAO().insertLLMModel(llmModel)
                    
                    _uiState.value = _uiState.value.copy(
                        isDownloading = false,
                        downloadProgress = 1f,
                        downloadStatus = "Download completed successfully!",
                        errorMessage = null
                    )
                } else {
                    modelFile.delete()
                    _uiState.value = _uiState.value.copy(
                        isDownloading = false,
                        errorMessage = "Downloaded file is not a valid GGUF model"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDownloading = false,
                    errorMessage = "Download failed: ${e.message}"
                )
            }
        }
    }

    fun searchModels(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val searchResults = hfModelsAPI.getModelsList(
                    query = query,
                    filter = "gguf",
                    sort = "downloads",
                    limit = 20
                )
                _uiState.value = _uiState.value.copy(
                    searchResults = searchResults,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Search failed: ${e.message}"
                )
            }
        }
    }

    fun importModelFromFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            _filePickerUIState.value = _filePickerUIState.value.copy(
                isImporting = true,
                errorMessage = null
            )

            try {
                val fileName = getFileName(context, uri) ?: "imported_model.gguf"
                val modelsDir = File(context.filesDir, "models")
                if (!modelsDir.exists()) {
                    modelsDir.mkdirs()
                }

                val modelFile = File(modelsDir, fileName)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(modelFile).use { output ->
                        input.copyTo(output)
                    }
                }

                // Verify GGUF file
                val ggufReader = GGUFReader()
                val isValidGGUF = ggufReader.isValidGGUFFile(modelFile.absolutePath)

                if (isValidGGUF) {
                    val llmModel = LLMModel(
                        name = fileName.removeSuffix(".gguf"),
                        path = modelFile.absolutePath,
                        size = modelFile.length(),
                        isDownloaded = true,
                        source = "Local Import"
                    )

                    appDB.llmModelDAO().insertLLMModel(llmModel)

                    _filePickerUIState.value = _filePickerUIState.value.copy(
                        isImporting = false,
                        importSuccess = true
                    )
                } else {
                    modelFile.delete()
                    _filePickerUIState.value = _filePickerUIState.value.copy(
                        isImporting = false,
                        errorMessage = "Selected file is not a valid GGUF model"
                    )
                }

            } catch (e: Exception) {
                _filePickerUIState.value = _filePickerUIState.value.copy(
                    isImporting = false,
                    errorMessage = "Import failed: ${e.message}"
                )
            }
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var fileName: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName
    }

    fun downloadModel(modelInfo: HFModelInfo) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDownloading = true, errorMessage = null)
            try {
                val modelInfo = hfModelsAPI.getModelInfo(modelInfo.id)
                val modelTree = hfModelsAPI.getModelTree(modelInfo.id)
                
                val ggufFiles = modelTree.filter { file ->
                    file.path.endsWith(".gguf") && file.path.contains("Q4_K_M", ignoreCase = true)
                }
                
                if (ggufFiles.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isDownloading = false,
                        errorMessage = "No suitable GGUF files found in this model"
                    )
                    return@launch
                }
                
                val file = ggufFiles.first()
                val downloadUrl = "https://huggingface.co/${modelInfo.id}/resolve/main/${file.path}"
                val fileName = file.path.substringAfterLast("/")
                
                downloadModelFile(downloadUrl, fileName, modelInfo)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDownloading = false,
                    errorMessage = "Download failed: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
        _filePickerUIState.value = _filePickerUIState.value.copy(errorMessage = null)
    }

    fun clearImportSuccess() {
        _filePickerUIState.value = _filePickerUIState.value.copy(importSuccess = false)
    }
}

data class DownloadModelsUIState(
    val searchResults: List<HFModelInfo> = emptyList(),
    val isLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val downloadStatus: String = "",
    val errorMessage: String? = null
)

data class FilePickerUIState(
    val isImporting: Boolean = false,
    val importSuccess: Boolean = false,
    val errorMessage: String? = null
)
