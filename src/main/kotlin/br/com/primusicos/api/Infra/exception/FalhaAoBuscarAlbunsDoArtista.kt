package br.com.primusicos.api.Infra.exception

import java.lang.RuntimeException

class FalhaAoBuscarAlbunsDoArtista(msg: String = "Falha ao buscar albuns do artista!") : RuntimeException(msg)