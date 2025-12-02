package br.com.arpeggio.api.infra.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebClientConfig: WebMvcConfigurer  {

    @Bean
    fun webClient(): WebClient {
        return WebClient.builder().build()
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("*") // Permitir acesso de qualquer origem
            .allowedMethods("GET") // Métodos permitidos
            .allowedHeaders("*") // Headers permitidos
    }
}
