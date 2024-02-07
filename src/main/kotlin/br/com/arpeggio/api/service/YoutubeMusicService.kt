package br.com.arpeggio.api.service

import br.com.arpeggio.api.domain.resultado.ResultadoBusca
import br.com.arpeggio.api.domain.resultado.ResultadoBuscaConcluida
import br.com.arpeggio.api.domain.resultado.ResultadoBuscaErros
import br.com.arpeggio.api.infra.busca.RequestParams
import jakarta.annotation.PostConstruct
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Wait
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URL
import java.time.Duration

private const val SELETOR_XPATH_BOTAO_ARTISTA: String = "//yt-formatted-string[text()='Artistas']"
private const val SELETOR_CSS_ALBUNS_DO_ARTISTA: String = "#details > yt-formatted-string"
private const val SELETOR_CSS_LISTA_ARTISTAS: String = "#contents > ytmusic-grid-renderer"

@Service
class YoutubeMusicService(
    override val NOME_STREAMING: String = "Youtube Music",

    @Value("\${CHROME_HOST}")
    private val CHROME_HOST: String,

    @Value("\${CHROME_PORT}")
    private val CHROME_PORT: String,

    private var driver: RemoteWebDriver?,
) : CommandStreamingAudio {

    @PostConstruct
    fun logaUrlDoChrome() {
        println("Selenium: url do driver: http://${CHROME_HOST}:${CHROME_PORT}")
    }

    override suspend fun buscaPorArtista(requestParams: RequestParams): ResultadoBusca {
        //return ResultadoBuscaErros(NOME_STREAMING, "Desativado na API temporariamente")
        var totalDeAlbuns = 0
        val busca = runCatching {
            totalDeAlbuns = executaSelenium(requestParams)
        }

        busca.onFailure { return ResultadoBuscaErros(NOME_STREAMING, busca.exceptionOrNull()!!.localizedMessage) }

        return ResultadoBuscaConcluida(NOME_STREAMING, totalDeAlbuns)
    }

    private fun executaSelenium(requestParams: RequestParams): Int {
        val options = ChromeOptions()
        // funcionamento headless com problema ao rodar em container. Com drivers locais funciona.

        //options.addArguments("headless=new")
        options.addArguments("--ignore-ssl-errors=yes")
        options.addArguments("--ignore-certificate-errors")
        options.addArguments("--lang=pt-BR") // garantindo que o navegador use pt-br independente de onde é instanciado.


        var qtdeAlbuns = 0

        val navegacao = runCatching {
            driver = RemoteWebDriver(URL("http://${CHROME_HOST}:${CHROME_PORT}"), options)

            driver?.let {
                val wait: Wait<WebDriver> = WebDriverWait(it, Duration.ofSeconds(10))
                val url = "https://music.youtube.com/search?q=${requestParams.busca}"

                // Acesse a URL
                it.get(url)
                cliqueBotaoArtista(wait)
                cliqueNoArtistaDaLista(requestParams, wait)
                cliqueBotaoAlbumDoArtista(wait)
                qtdeAlbuns = calculaTotalDeAlbuns(wait)
            } ?: throw RuntimeException("Erro ao criar o Selenium Remote WebDriver")
        }

        driver?.let {
            println("fechou driver")
            it.quit()
        }

        navegacao.onFailure {
            throw RuntimeException(navegacao.exceptionOrNull()!!.localizedMessage)
        }

        return qtdeAlbuns
    }

    private fun calculaTotalDeAlbuns(wait: Wait<WebDriver>): Int {
        wait
            .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(SELETOR_CSS_LISTA_ARTISTAS)))

        val script = """
                var itemsContainer = document.querySelector('#contents > ytmusic-grid-renderer > #items');
    
                // Se o contêiner de itens for encontrado
                if (itemsContainer) {
                    // Obtenha todos os itens dentro do contêiner
                    var items = itemsContainer.children;
                    return items.length;
                    
                } else{
                    return -1;
                }
            """.trimIndent()

        val result = (driver as JavascriptExecutor).executeScript(script) as Long

        return result.toInt()
    }

    private fun cliqueBotaoAlbumDoArtista(wait: Wait<WebDriver>) = wait
        .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(SELETOR_CSS_ALBUNS_DO_ARTISTA)))
        .click()

    private fun cliqueNoArtistaDaLista(requestParams: RequestParams, wait: Wait<WebDriver>) {

        val artistLink = wait
            .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[aria-label='${requestParams.busca}' i]")))
            .getAttribute("href")

        driver
            ?.get(artistLink)
            ?: throw RuntimeException("Erro ao recuperar link da página do artista")
    }

    private fun cliqueBotaoArtista(wait: Wait<WebDriver>) = wait
        .until(ExpectedConditions.presenceOfElementLocated(By.xpath(SELETOR_XPATH_BOTAO_ARTISTA)))
        .click()

}