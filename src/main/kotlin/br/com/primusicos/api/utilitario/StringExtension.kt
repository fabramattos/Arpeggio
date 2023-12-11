package br.com.primusicos.api.utilitario

/**
 * Remove espaços em branco demasiados da string. Retorna com apenas um " " entre as palavras, quando aplicável.
 */
fun String.tratarBuscaArtista() : String {
    println("antes do filtro: + $this")
    val split: List<String> = this.split(" ")

    println("Lista antes de filtro:")
    for (i in split.indices){
        println("$i: ${split[i]}")
    }

    val filtrado = split.filterNot { it.isEmpty() }
    for (i in filtrado.indices){
        println("$i: ${filtrado[i]}")
    }


    var resposta = ""
    filtrado.forEach{resposta = resposta.plus(it) + " "}

    println("tamanho resposta inicial: ${resposta.length}")

    resposta = resposta.removeSuffix(" ")
    println("tamanho resposta final: ${resposta.length}")
    print(resposta)
    return resposta
}
