package br.com.primusicos.api.utilitario

/**
 * Remove espaços em branco demasiados da string. Retorna com apenas um " " entre as palavras, quando aplicável.
 */
fun String.tratarBuscaArtista() : String {
    val split: List<String> = this.split(" ")
    val filtrado = split.filterNot { it.isEmpty() }
    var resposta = ""

    filtrado.forEach{resposta = resposta.plus(it) + " "}

    resposta = resposta.removeSuffix(" ")

    return resposta
}
