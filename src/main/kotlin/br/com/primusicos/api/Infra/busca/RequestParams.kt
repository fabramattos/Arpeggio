package br.com.primusicos.api.Infra.busca

class RequestParams(
    val busca: String,
    val regiao: RequestRegiao,
    val tipos: List<RequestTipo>
)