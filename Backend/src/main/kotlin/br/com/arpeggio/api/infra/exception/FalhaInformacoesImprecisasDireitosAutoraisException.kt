package br.com.arpeggio.api.infra.exception

import java.lang.RuntimeException

class FalhaInformacoesImprecisasDireitosAutoraisException
    : RuntimeException("Informações imprecisas devido restrição por detentores de direito")