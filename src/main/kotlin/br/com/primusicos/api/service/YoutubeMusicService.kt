package br.com.primusicos.api.service

import br.com.primusicos.api.domain.resultado.ResultadoBusca
import br.com.primusicos.api.domain.resultado.ResultadoBuscaErros
import br.com.primusicos.api.domain.resultado.ResultadoBuscaOk
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Wait
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.stereotype.Service


@Service
class YoutubeMusicService(
    private val NOME_STREAMING: String = "YouTube Music",
    private val EDGE_LOCATION: String = "\"C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe\"",
    private val SELETOR_CSS_BOTAO_ARTISTA: String = "#chips > ytmusic-chip-cloud-chip-renderer:nth-child(6) > div > a",
    private val SELETOR_CSS_ALBUNS_DO_ARTISTA: String = "#details > yt-formatted-string",
    private val SELETOR_CSS_LISTA_ARTISTAS: String = "#contents > ytmusic-grid-renderer",
) : CommandStreamingAudio {


    override fun buscaPorArtista(nome: String): ResultadoBusca {
        var totalDeAlbuns = 0
        val busca = runCatching {
            totalDeAlbuns = executaSelenium(nome)
        }

        busca.onFailure {return ResultadoBuscaErros(NOME_STREAMING, busca.exceptionOrNull()!!.localizedMessage) }

        return ResultadoBuscaOk(NOME_STREAMING, totalDeAlbuns)
    }

    private fun executaSelenium(nome: String) : Int {
        val options = EdgeOptions()
        //options.addArguments("--headless")  // Execute sem abrir uma janela do navegador
        options.setBinary(EDGE_LOCATION)

        val driver = EdgeDriver(options)

        val url = "https://music.youtube.com/search?q=$nome"

        // Acesse a URL
        driver.get(url)

        val wait: Wait<WebDriver> = WebDriverWait(driver, java.time.Duration.ofSeconds(5))

        cliqueBotaoArtista(wait)
        cliqueNoArtistaDaLista(wait, nome, driver)
        cliqueBotaoAlbumDoArtista(wait)
        val qtdeAlbuns = calculaTotalDeAlbuns(wait, driver)

        // Feche o driver
        driver.quit()
        return qtdeAlbuns

    }

    private fun calculaTotalDeAlbuns(
        wait: Wait<WebDriver>,
        driver: EdgeDriver,
    ) : Int {

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
        // Imprimir o resultado
        println("Número de álbuns no YouTube: $result")

        return result.toInt()
    }

    private fun cliqueBotaoAlbumDoArtista(wait: Wait<WebDriver>) = wait
        .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(SELETOR_CSS_ALBUNS_DO_ARTISTA)))
        .click()

    private fun cliqueNoArtistaDaLista(
        wait: Wait<WebDriver>,
        nome: String,
        driver: EdgeDriver,
    ) {

        val artistLink = wait
            .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("a[aria-label='$nome' i]")))
            .getAttribute("href")

        driver.get(artistLink)
    }

    private fun cliqueBotaoArtista(wait: Wait<WebDriver>) = wait
        .until(ExpectedConditions.elementToBeClickable(By.cssSelector(SELETOR_CSS_BOTAO_ARTISTA)))
        .click()

}