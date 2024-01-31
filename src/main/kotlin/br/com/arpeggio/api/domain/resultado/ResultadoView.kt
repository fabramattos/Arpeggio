package br.com.arpeggio.api.domain.resultado

data class ResultadoView(
    val artista: String,
    val resultados: List<ResultadoBusca>,
)