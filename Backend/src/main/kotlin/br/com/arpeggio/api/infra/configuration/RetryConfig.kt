package br.com.arpeggio.api.infra.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.retry.annotation.EnableRetry

@Configuration
@EnableRetry
class RetryConfig {
}