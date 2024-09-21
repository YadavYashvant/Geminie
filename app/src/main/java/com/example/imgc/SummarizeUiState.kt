package com.example.imgc

sealed interface SummarizeUiState {

    object Initial : SummarizeUiState


    object Loading : SummarizeUiState


    data class Success(
            val outputText: String
    ) : SummarizeUiState

    data class Error(
            val errorMessage: String
    ) : SummarizeUiState
}