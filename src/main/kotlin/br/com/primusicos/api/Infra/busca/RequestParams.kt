package br.com.primusicos.api.Infra.busca

import java.util.*

class RequestParams(
    val busca: String,
    val regiao: RequestRegiao,
    val tipos: List<RequestTipo>
){
    val id: UUID? = UUID.randomUUID()
}