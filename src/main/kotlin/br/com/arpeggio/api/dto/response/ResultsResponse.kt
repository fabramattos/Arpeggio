package br.com.arpeggio.api.dto.response

data class ResultsResponse(
    val busca: String,
    val resultados: List<ItemResponse>,
)