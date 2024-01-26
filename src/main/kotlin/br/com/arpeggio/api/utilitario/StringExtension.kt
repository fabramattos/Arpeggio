package br.com.arpeggio.api.utilitario

import java.util.Base64

/**
 * Remove espaços em branco demasiados da string. Retorna com apenas um " " entre as palavras, quando aplicável.
 */
fun String.tratarBuscaArtista() : String {
    val split: List<String> = this.split(" ")
    val filtrado = split.filter { it.isNotEmpty() }
    var resposta = ""

    filtrado.forEach{resposta = resposta.plus(it) + " "}

    resposta = resposta.removeSuffix(" ")

    return resposta
}

fun String.toBase64(): String {
    return Base64.getEncoder().encodeToString(toByteArray())
}
