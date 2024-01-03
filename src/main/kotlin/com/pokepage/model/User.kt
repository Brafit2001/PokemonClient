package com.pokepage.model

import kotlinx.serialization.Serializable

@Serializable
class User(
    val id: Int,
    val name: String,
    val password: String,
    val email: String,
)


@Serializable
class PostUserBody(
    val name: String,
    val password: String,
    val email: String,
)

@Serializable
class PutUserBody(
    val name: String,
    val password: String,
    val email: String,
)

