package com.pokepage

import com.pokepage.plugins.*
import io.ktor.client.*
import io.ktor.client.engine.apache5.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0", module = Application::module, watchPaths = listOf("classes"))
        .start(wait = true)
}

fun Application.module() {

    configureTemplating()
    configureRouting()
}
