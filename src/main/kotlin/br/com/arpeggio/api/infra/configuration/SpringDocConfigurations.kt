package br.com.arpeggio.api.infra.configuration

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
                        DESCRIÇÃO:
                        API para consulta simultanea nos seguintes  streamings de música:
                            Spotify, Deezer, Tidal, YouTube Music (desativado temporariamente).
                        
                        Por enquanto, não busca por Podcasts.
                        
                        
                        COMO USAR:
                        Pesquise por um artista, informando o país para consulta e o tipo de conteúdo desejado e retornaremos
                        a quantidade presente em cada serviço!
                        
                        Tipo de conteúdo buscado: ALBUM, SINGLE, EP
                        
                        
                        OBS:
                        Devido a diferença na forma que cada streaming cataloga seus dados, para uma comparação justa,
                        esta API agrupa SINGLES e EPs (buscando por SINGLE retornamos SINGLE e EP).
                    """
            contact = Contact().apply {
                name = "Felipe Mattos"
//                email = "email@email.com"
//                url = "urlFrontEnd.com.br"
            }
        })


//    @Bean
//    fun corsWebFilter(): CorsWebFilter {
//        val source = UrlBasedCorsConfigurationSource()
//        val config = CorsConfiguration()
//
//        config.allowCredentials = true
//
//        // por  padrão swagger faz requisição sem o "s" do http
//        config.addAllowedOrigin("http://arpeggio.up.railway.app/**")
//        config.addAllowedOrigin("*")
//        config.addAllowedHeader("*")
//        config.addAllowedMethod("*")
//        source.registerCorsConfiguration("/**", config)
////        source.registerCorsConfiguration("/v1/artista/**", config)
////        source.registerCorsConfiguration("/doc/swagger-ui/**", config)
//        return CorsWebFilter(source)
//    }

}