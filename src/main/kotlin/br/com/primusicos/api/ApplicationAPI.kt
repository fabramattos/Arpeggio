package br.com.primusicos.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class ApplicationAPI

fun main(args: Array<String>) {
    runApplication<ApplicationAPI>(*args)
}