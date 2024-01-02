package com.pokepage.model

import kotlinx.serialization.Serializable

@Serializable
class Pokemon(
    val id: Int,
    val name: String,
    val url: String
)

@Serializable
data class PostPokemonBody(
    val name: String,
    val url: String
)
@Serializable
class PutPokemonBody(
    val name: String,
    val url: String
)



