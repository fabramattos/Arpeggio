package br.com.primusicos.api.domain.resultado

data class Resultado(
    val artista: String,
    val resultados: List<ResultadoBuscaOk>,
    val erros: List<ResultadoBuscaErros>,
)