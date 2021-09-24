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
            call.respondText { document }
        }

        get("/document"){
            call.respondText { document }
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

val document ="""
    This Api is used for Creating , Update,Delete User.
    For a Particular user , Todo can be created, updated, deleted ,delete ALL todo's of particular User
 
 /**
 * =====================================
 * ============USER====================
 * ====================================
 */
    
    POST "/v1/create"   PARAM= name,email,password
    POST "/v1/login"   PARAM= email,password
    POST "/v1/user"     need to send JWT token which you would be getting during create /login
    PUT "/v1/user"   PARAM= name,email,password    need to send JWT token which you would be getting during create /login
    PATCH "/v1/user"   ANY PARAM= name,email,password    need to send JWT token which you would be getting during create /login
   
     /**
     * =====================================
     * ============TODO====================
     * ====================================
     */
     JWT is must for authentication
     
      POST "/v1/todos"   PARAM= todo,done:Boolean
      GET "/v1/todos" 
      DELETE "/v1/todos/{id}"   deleted particular todo row
      DELETE "/v1/todos"    deletes all todo of user
      PUT "/v1/todos/{id}"  modify particular todo row
       
  
""".trimIndent()

