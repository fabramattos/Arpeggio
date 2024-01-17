package br.com.primusicos.api.Infra.busca

import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@Component
@RequestScope
class BuscaRequest(
    var busca: String = "",
    var regiao: BuscaRegiao = BuscaRegiao.BR,
    var tipos: List<BuscaTipo> = listOf(BuscaTipo.ALBUM),
)