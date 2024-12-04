package br.com.arpeggio.api.dto.request

import java.util.concurrent.atomic.AtomicInteger

data class RequestParams(
    val busca: String,
    val regiao: RequestRegiao,
    val tipos: List<RequestTipo>
){
    val id: Int = generateId()

    companion object {
        private val counter = AtomicInteger(0)
        private const val MAX_ID = 1000

        private fun generateId(): Int {
            return counter.updateAndGet { current ->
                if (current >= MAX_ID) 1 else current + 1
            }
        }
    }
}