package br.com.primusicos.api.Infra.security

import br.com.primusicos.api.utilitario.toBase64

class AuthEncoders {
    fun encodeToBase64(clientId: String, clientSecret: String): String {
        val credentials = "$clientId:$clientSecret"
        val encodedCredentials = credentials.toBase64()
        return "Basic $encodedCredentials"
    }
}