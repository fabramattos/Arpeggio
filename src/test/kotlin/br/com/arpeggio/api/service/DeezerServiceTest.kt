package br.com.arpeggio.api.service

import br.com.arpeggio.api.domain.resultado.ResultadoBuscaConcluidaAlbuns
import br.com.arpeggio.api.infra.busca.RequestParams
import br.com.arpeggio.api.infra.busca.RequestRegiao
import br.com.arpeggio.api.infra.busca.RequestTipo
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.random.Random

class DeezerServiceTest {

    private val deezerService = mockk<DeezerService>()
    private val listaDisparos = mutableListOf<RequestParams>()

    init {
        //cria lista de requests com id's unicos
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
            ResultadoBuscaConcluidaAlbuns("Deezer", Random.nextInt())
        }

        // executa as chamadas ao service de forma assincrona
        runBlocking {
            coroutineScope {
                listaDisparos.forEach {
                    launch {
                        deezerService.buscaPorArtista(it)
                        listaProcessada.add(it)
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
        Assertions.assertFalse(existeDiferenca,"Não deveria existir diferença na ordem de requests recebidos e processados"
        )
    }


    @Test
    fun `Dado varios requests assincronos, Quando o tempo de resposta variar, Deve retornar os resultados em ordem diferente da recebida`() {
        val listaProcessada = mutableListOf<RequestParams>()

        // configura mock com delay aleatorio.
        coEvery { deezerService.buscaPorArtista(any()) } coAnswers {
            delay(Random.nextLong(500))
            ResultadoBuscaConcluidaAlbuns("Deezer", Random.nextInt())
        }

        // executa as chamadas ao service de forma assincrona
        runBlocking {
            coroutineScope {
                listaDisparos.forEach {
                    launch {
                        deezerService.buscaPorArtista(it)
                        listaProcessada.add(it)
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

        Assertions.assertTrue(existeDiferenca,"Deveria existir diferença na ordem de requests recebidos e processados")
    }
}
