package com.example.imgc

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GetImgContextViewmodel(
        private val generativeModel: GenerativeModel
) : ViewModel() {

    private val _uiState: MutableStateFlow<SummarizeUiState> =
            MutableStateFlow(SummarizeUiState.Initial)
    val uiState: StateFlow<SummarizeUiState> =
            _uiState.asStateFlow()

    fun findContextOfImage(uri: Bitmap, prompt: String?, isAdv: Boolean, ) {
        _uiState.value = SummarizeUiState.Loading

        viewModelScope.launch {
            try {
                val response = generativeModel.generateContent(
                    content { image(uri ?: return@content)
                        if(prompt!=null && isAdv) {
                            text(prompt)
                        }
                        else{
                            text("Find the context of the following image")
                        }
                    }
                        //"Find the context of the following image: $uri"
                )
                response.text?.let { outputContent ->
                    _uiState.value = SummarizeUiState.Success(outputContent)
                }
            } catch (e: Exception) {
                _uiState.value = SummarizeUiState.Error(e.localizedMessage ?: "")
            }
        }
    }
}