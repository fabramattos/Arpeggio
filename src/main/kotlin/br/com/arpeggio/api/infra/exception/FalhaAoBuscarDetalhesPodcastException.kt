package br.com.arpeggio.api.infra.exception

import java.lang.RuntimeException

class FalhaAoBuscarDetalhesPodcastException :
    RuntimeException("Falha ao buscar detalhes do podcast encontrado!")