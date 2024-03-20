package br.com.arpeggio.api.domain.resultado

data class ResultadoView(
    val busca: String,
    val resultados: List<ResultadoBusca>,
)