package br.com.primusicos.api.domain

data class RespostaPorStreaming(
    val streaming: String,
    val artista: String,
    val totalDeAlbuns: Int)