package br.com.arpeggio.api.infra.exception

import java.lang.RuntimeException

class FalhaAoBuscarAlbunsDoArtista : RuntimeException("Falha ao buscar albuns do artista!")