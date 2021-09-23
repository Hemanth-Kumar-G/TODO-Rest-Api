package com.todo.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.todo.data.User
import java.util.*

class JwtService {

    private val issuer = "Sever"
    private val jwtSecret = System.getenv("JWT_SECRET")
    private val algorithm = Algorithm.HMAC512(jwtSecret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    fun generateToken(user: User) = JWT
        .create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("userId", user.userId)
        .withExpiresAt(expireToken())
        .sign(algorithm)

    fun expireToken() = Date(System.currentTimeMillis() + 36_00_000 * 24)
}