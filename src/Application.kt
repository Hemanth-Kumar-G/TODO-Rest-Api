package com.todo

import com.todo.auth.JwtService
import com.todo.auth.MySession
import com.todo.repository.DataBaseFactory
import com.todo.repository.TodoRepository
import com.todo.repository.UserRepository
import com.todo.routes.todoRoute
import com.todo.routes.userRoute
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*
import org.slf4j.event.Level
import kotlin.collections.set

const val API_VERSION = "/v1"
fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    DataBaseFactory.init()
    val userDb = UserRepository()
    val todoDb = TodoRepository()
    val jwt = JwtService()
    val hash = { s: String ->
        s
    }

    install(Locations) {
    }

    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(Authentication) {
        jwt("jwt") {
            verifier(jwt.verifier)
            realm = "Todo Server"
            validate {
                val payload = it.payload
                val claim = payload.getClaim("userId")
                val claimString = claim.asInt()
                val user = userDb.findUserById(claimString)
                user
            }
        }
    }

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }


    routing {

        get("/"){
            call.respondText { "Hello Hemanth" }
        }
        userRoute(
            userDb,
            todoDb,
            jwt,
            hash
        )

        todoRoute(
            userDb,
            todoDb
        )
    }
}

