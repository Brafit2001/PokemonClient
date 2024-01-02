package com.pokepage.model

import kotlinx.serialization.Serializable

@Serializable
data class PokeApiResponse(
    val count: Int = 0,
    val next: String? = null,
    val previous: String? = null,
    val results: List<PostPokemonBody> = listOf()
)


