package br.com.arpeggio.api.service

import br.com.arpeggio.api.domain.resultado.ResultadoBusca
import br.com.arpeggio.api.domain.resultado.ResultadoBuscaConcluidaAlbuns
import br.com.arpeggio.api.infra.busca.RequestParams
import br.com.arpeggio.api.infra.busca.RequestRegiao
import br.com.arpeggio.api.infra.busca.RequestTipo
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

class DeezerServiceTest {

    private val deezerService = mockk<DeezerService>()
    private val listaDisparos = mutableListOf<RequestParams>()

    init {
        repeat(50) {
            listaDisparos.add(RequestParams("banda", RequestRegiao.BR, listOf(RequestTipo.ALBUM)))
        }
    }

    @Test
    fun `Dado varios requests assincronos, Quando o tempo de resposta nao variar, Deve retornar os resultados na mesma sequencia processada`() {
        val listaProcessada = mutableListOf<RequestParams>()

        // configura mock com delay aleatorio.
        coEvery { deezerService.buscaPorArtista(any()) } coAnswers {
            delay(100) // simula um tempo de resposta da API fixo
            ResultadoBuscaConcluidaAlbuns("Deezer", "artista teste", Random.nextInt())
        }

        // executa as chamadas ao service de forma assincrona
        runBlocking {
            coroutineScope {
                listaDisparos.forEach {
                    launch {
                        deezerService.buscaPorArtista(it)
                        synchronized(listaProcessada) {
                            listaProcessada.add(it)
                        }
                    }
                }
            }
        }

        Assertions.assertEquals(50, listaProcessada.size)
        Assertions.assertTrue(listaProcessada.containsAll(listaDisparos))

        // verifica se a sequencia disparada é a mesma retornada. Não deve ser.
        var existeDiferenca = false
        for (i in 0..49) {
            if (listaDisparos[i] != listaProcessada[i]) {
                existeDiferenca = true
                break
            }
        }
        Assertions.assertFalse(
            existeDiferenca, "Não deveria existir diferença na ordem de requests recebidos e processados"
        )
    }


    @Test
    fun `Dado varios requests assincronos, Quando processados, Deve retornar resultados em ordem diferente da sequencia original`() {
        val contador = AtomicInteger(1)

        coEvery { deezerService.buscaPorArtista(any()) } coAnswers {
            val delayTime = Random.nextLong(500, 1500)
            delay(delayTime)
            ResultadoBuscaConcluidaAlbuns("Deezer", "artista teste", contador.getAndIncrement())
        }

        val resultados = mutableListOf<ResultadoBuscaConcluidaAlbuns>()

        runBlocking {
            listaDisparos.forEach {
                launch {
                    val resultado = deezerService.buscaPorArtista(it) as ResultadoBuscaConcluidaAlbuns
                    resultados.add(resultado)
                }
            }
        }

        // Logs para análise
        println(
            "Resultados: ${
            resultados.map
            { it.albuns }
        }"
        )

        // Verificar se os resultados estão fora de ordem
        val estaForaDeOrdem = resultados
            .zipWithNext { a, b -> a.albuns > b.albuns }
            .any { it }

        Assertions.assertTrue(
            estaForaDeOrdem,
            "Os resultados deveriam estar fora de ordem devido ao processamento paralelo."
        )
    }

}
