package br.com.arpeggio.api.infra.configuration

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class StartupLogger(private val env: Environment) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(StartupLogger::class.java)

    override fun run(args: ApplicationArguments?) {
        // Logando os perfis ativos
        val activeProfiles = env.activeProfiles.joinToString(", ")
        logger.info("Perfis ativos: $activeProfiles")
    }
}