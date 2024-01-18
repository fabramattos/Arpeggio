package br.com.primusicos.api.Infra.exception

import java.lang.RuntimeException

class FalhaInformacoesImprecisasDireitosAutoraisException
    : RuntimeException("Informações imprecisas devido restrição por detentores de direito")