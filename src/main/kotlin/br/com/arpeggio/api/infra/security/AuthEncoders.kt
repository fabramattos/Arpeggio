package br.com.arpeggio.api.infra.security

import br.com.arpeggio.api.utilitario.toBase64

class AuthEncoders {
    fun encodeToBase64(clientId: String, clientSecret: String): String {
        val credentials = "$clientId:$clientSecret"
        val encodedCredentials = credentials.toBase64()
        return "Basic $encodedCredentials"
    }
}