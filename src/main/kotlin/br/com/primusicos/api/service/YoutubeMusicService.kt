package br.com.primusicos.api.service

import br.com.primusicos.api.domain.resultado.ResultadoBusca
import br.com.primusicos.api.domain.resultado.ResultadoBuscaErros
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


@Service
class YoutubeMusicService(
    private var nomeArtista: String?,

    @Value("\${chrome.url}")
    private val CHROME_URL: String,

    @Value("\${chrome.port}")
    private val CHROME_PORT: String,

    private val NOME_STREAMING: String = "YouTube Music",
    private val SELETOR_XPATH_BOTAO_ARTISTA: String = "//yt-formatted-string[text()='Artistas']",
    private val SELETOR_CSS_ALBUNS_DO_ARTISTA: String = "#details > yt-formatted-string",
    private val SELETOR_CSS_LISTA_ARTISTAS: String = "#contents > ytmusic-grid-renderer",
    private var driver: RemoteWebDriver?,
) : CommandStreamingAudio {


    override fun buscaPorArtista(): ResultadoBusca {
        println("Consultando YouTube")
        return ResultadoBuscaErros(NOME_STREAMING,"Desativado na API temporariamente")
//        nomeArtista = nome
//        var totalDeAlbuns = 0
//        val busca = runCatching {
//            totalDeAlbuns = executaSelenium()
//        }
//
//        busca.onFailure { return ResultadoBuscaErros(NOME_STREAMING, busca.exceptionOrNull()!!.localizedMessage) }
//
//        return ResultadoBuscaOk(NOME_STREAMING, totalDeAlbuns)
    }

    private fun executaSelenium(): Int {
        val options = ChromeOptions()
        // funcionamento headless com problema ao rodar via Remote Web Driver. Com drivers locais funciona.

        //options.addArguments("headless=new")
        options.addArguments("--ignore-ssl-errors=yes")
        options.addArguments("--ignore-certificate-errors")
        options.addArguments("--lang=pt-BR") // garantindo que o navegador use pt-br independente de onde é instanciado.


        var qtdeAlbuns = 0

        val navegacao = runCatching {
            println("url do driver: ${CHROME_URL}:${CHROME_PORT}")
            driver = RemoteWebDriver(URL("${CHROME_URL}:${CHROME_PORT}"), options)

            driver?.let {
                val wait: Wait<WebDriver> = WebDriverWait(it, java.time.Duration.ofSeconds(10))
                val url = "https://music.youtube.com/search?q=$nomeArtista"

                // Acesse a URL
                it.get(url)
                cliqueBotaoArtista(wait)
                cliqueNoArtistaDaLista(wait)
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

    private fun cliqueNoArtistaDaLista(wait: Wait<WebDriver>) {

        val artistLink = wait
            .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[aria-label='$nomeArtista' i]")))
            .getAttribute("href")

        driver
            ?.get(artistLink)
            ?: throw RuntimeException("Erro ao recuperar link da página do artista")
    }

    private fun cliqueBotaoArtista(wait: Wait<WebDriver>) = wait
        .until(ExpectedConditions.presenceOfElementLocated(By.xpath(SELETOR_XPATH_BOTAO_ARTISTA)))
        .click()

}