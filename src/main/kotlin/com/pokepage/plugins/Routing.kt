package com.pokepage.plugins

import com.pokepage.model.PokeApiResponse
import com.pokepage.model.Pokemon
import com.pokepage.model.PostPokemonBody
import com.pokepage.model.PutPokemonBody
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.apache5.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.thymeleaf.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.json.Json
import java.lang.Double.parseDouble

val client = HttpClient(Apache5) {
    engine {
        // this: Apache5EngineConfig
        followRedirects = true
        socketTimeout = 10_000
        connectTimeout = 10_000
        connectionRequestTimeout = 20_000
        customizeRequest {
            // this: RequestConfig.Builder
        }

    }
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.NONE //You can only request/response headers, include bodies or none
        filter { request ->
            request.url.host.contains("ktor.io")
        }
        sanitizeHeader {
                header -> header == HttpHeaders.Authorization
        }
    }
    install(ContentNegotiation){
        json()
    }
}
const val apiUrl = "http://127.0.0.1:8080/v1/api/pokemons"

fun Application.configureRouting() {

    val homePaths = arrayOf("/", "/search")
    routing {
        staticResources("/static", "static")
        homePaths.forEach { path -> get(path, home()) }

        get("/new"){
            val pokemon = Pokemon(0, "", "")
            call.respond(ThymeleafContent("newPokemon", mapOf("pokemon" to pokemon)))
        }
        post("/new"){
            val formParameters = call.receiveParameters()
            val name: String = formParameters["name"].toString()
            val url: String = formParameters["pokemonUrl"].toString()
            val postPokemon = PostPokemonBody(name, url)
            client.post(apiUrl) {
                contentType(ContentType.Application.Json)
                setBody(postPokemon)
            }
            call.respondRedirect("/", false)
        }

        post("/edit"){
            val formParameters = call.receiveParameters()
            val id: String = formParameters["id"].toString()
            val name: String = formParameters["name"].toString()
            val url: String = formParameters["pokemonUrl"].toString()
            val putPokemon = PutPokemonBody(name, url)
            client.put("$apiUrl/$id") {
                contentType(ContentType.Application.Json)
                setBody(putPokemon)
            }
            call.respondRedirect("/", false)
        }

        get("/edit/{id}"){
            val id = call.parameters["id"]
            val pokemon: Pokemon = client.get("$apiUrl/id/$id").body()
            call.respond(ThymeleafContent("editPokemon", mapOf("pokemon" to pokemon)))
        }

        get("/delete/{id}"){
            val id = call.parameters["id"]
            client.delete("$apiUrl/$id")
            call.respondRedirect("/", false)
        }
    }
}

private fun home(): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit =
    {
        val getAllRequest: HttpResponse = client.get(apiUrl)
        if (getAllRequest.status == HttpStatusCode.NoContent){
            //If database is empty it dumps all poke api data
            //Get Full Pokemons from PokeApi list
            val pokeApiResponse: String = client.get("https://pokeapi.co/api/v2/pokemon?limit=1018&offset=0").body()
            val convertResponse = Json.decodeFromString<PokeApiResponse>(pokeApiResponse)
            val pokeApiList: List<PostPokemonBody> = convertResponse.results
            //Create new Pokemon in database
            pokeApiList.forEach {
                client.post(apiUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(it)
                }

            }
        }else{
            val keyword = call.request.queryParameters["keyword"]
            var numeric = true
            if (keyword == null || keyword == ""){
                val pokemonList: List<Pokemon> = getAllRequest.body()
                call.respond(ThymeleafContent("index", mapOf("pokemonList" to pokemonList)))

            }else{
                try {
                    parseDouble(keyword)
                } catch (e: NumberFormatException) {
                    numeric = false
                }
                var getUrl = ""
                if (numeric){
                    getUrl = "id"
                }else{
                    getUrl = "name"
                }
                val getRequest: HttpResponse = client.get("$apiUrl/$getUrl/$keyword")
                when(getRequest.status){
                    HttpStatusCode.NotFound -> call.respondText("Pokemon Not Found")
                    else -> {
                        val pokemon: Pokemon = getRequest.body()
                        val pokemonList: List<Pokemon> = listOf(pokemon)
                        call.respond(ThymeleafContent("index", mapOf("pokemonList" to pokemonList)))
                    }
                }

            }

        }

    }
