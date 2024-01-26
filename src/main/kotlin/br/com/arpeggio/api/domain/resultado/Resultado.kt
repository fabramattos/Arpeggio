package br.com.arpeggio.api.domain.resultado

data class Resultado(
    val artista: String,
    val resultados: List<ResultadoBusca>,
    val erroApi: String?,
)