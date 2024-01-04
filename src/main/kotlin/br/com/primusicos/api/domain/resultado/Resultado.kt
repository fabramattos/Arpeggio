package br.com.primusicos.api.domain.resultado

data class Resultado(
    val artista: String,
    val resultados: List<ResultadoBusca>,
    val erroApi: String?,
)