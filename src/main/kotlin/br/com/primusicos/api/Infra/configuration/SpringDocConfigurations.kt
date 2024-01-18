package br.com.primusicos.api.Infra.configuration

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SpringDocConfigurations {

    @Bean
    fun customOpenAPI() = OpenAPI()
        .info(Info().apply {
            version = "1.0"
            title = "Arpeggio API"
            description = """
                        API para consulta simultanea em streamings de música.
                        Dado um artista, banda ou podcast, juntamente com os filtros para a busca (país, tipo de album desejado)
                        retornamos um comparativo com os dados obtidos.
                    """
            contact = Contact().apply {
                name = "Felipe Mattos"
//                email = "email@email.com"
//                url = "urlFrontEnd.com.br"
            }
        })

}