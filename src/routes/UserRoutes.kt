package com.todo.routes

import com.todo.API_VERSION
import com.todo.auth.JwtService
import com.todo.auth.MySession
import com.todo.repository.TodoRepository
import com.todo.repository.UserRepository
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*

const val USER = "$API_VERSION/user"
const val LOGIN = "$API_VERSION/login"
const val CREATE = "$API_VERSION/create"

@OptIn(KtorExperimentalLocationsAPI::class)
@Location(LOGIN)
class UserLoginRoute

@OptIn(KtorExperimentalLocationsAPI::class)
@Location(CREATE)
class UserCreateRoute

@OptIn(KtorExperimentalLocationsAPI::class)
@Location(USER)
class UserRoute

@KtorExperimentalLocationsAPI
fun Route.userRoute(
    userDb: UserRepository,
    todoDb: TodoRepository,
    jwtService: JwtService,
    hash: (String) -> String
) {
    post<UserCreateRoute> {
        val parameters = call.receive<Parameters>()

        val name = parameters["name"] ?: return@post call.respondText(
            text = "User name missing",
            status = HttpStatusCode.Unauthorized
        )
        val email = parameters["email"] ?: return@post call.respondText(
            text = "emailId missing",
            status = HttpStatusCode.Unauthorized
        )
        val password = parameters["password"] ?: return@post call.respondText(
            text = "password missing",
            status = HttpStatusCode.Unauthorized
        )
        val hashPassword = hash(password)
        val currentUser = userDb.createUser(name, email, password)

        try {
            currentUser?.userId?.let {
                call.sessions.set(MySession(it))
                call.respondText(
                    jwtService.generateToken(currentUser),
                    status = HttpStatusCode.Created
                )
            }
        } catch (e: Exception) {
            call.respondText("Problem creating user... ", status = HttpStatusCode.InternalServerError)
        }
    }


    post<UserLoginRoute> {
        val parameters = call.receive<Parameters>()
        val email = parameters["email"] ?: return@post call.respondText(
            text = "emailId missing",
            status = HttpStatusCode.Unauthorized
        )
        val password = parameters["password"] ?: return@post call.respondText(
            text = "password missing",
            status = HttpStatusCode.Unauthorized
        )

        val hashPassword = hash(password)

        try {
            val currentUser = userDb.findUserByEmail(email)
            currentUser?.userId?.let {
                if (currentUser.password == hashPassword) {
                    call.sessions.set(MySession(it))
                    call.respondText(jwtService.generateToken(currentUser))
                } else {
                    return@post call.respondText(
                        "invalid password",
                        status = HttpStatusCode.Unauthorized
                    )
                }
            }
        } catch (e: Exception) {
            call.respondText("Problem creating user... ", status = HttpStatusCode.InternalServerError)
        }
    }


    delete<UserRoute> {
        val user = call.sessions.get<MySession>()?.let {
            userDb.findUserById(it.userId)
        }
        try {
            user?.let {
                todoDb.deleteAllTodo(it.userId)
                val status = userDb.deleteUssr(it.userId)
                if (status == 1) {
                    call.respondText("User Deleted")
                } else {
                    call.respondText("problem in deleting ")
                }
            } ?: kotlin.run {
                call.respondText("Problem getting user... ", status = HttpStatusCode.BadRequest)

            }
        } catch (e: Exception) {
            call.respondText("Problem deleting user... ", status = HttpStatusCode.InternalServerError)
        }
    }

    put<UserRoute> {
        val parameters = call.receive<Parameters>()

        val name = parameters["name"] ?: return@put call.respondText(
            text = "User name missing",
            status = HttpStatusCode.BadRequest
        )
        val email = parameters["email"] ?: return@put call.respondText(
            text = "emailId missing",
            status = HttpStatusCode.BadRequest
        )
        val password = parameters["password"] ?: return@put call.respondText(
            text = "password missing",
            status = HttpStatusCode.BadRequest
        )

        val user = call.sessions.get<MySession>()?.let {
            userDb.findUserById(it.userId)
        } ?: return@put call.respondText(
            "Problem getting user",
            status = HttpStatusCode.BadRequest
        )

        val hashPassword = hash(password)

        try {
            val currentUser = userDb.updateUser(
                user.userId,
                name,
                email,
                password
            )
            if (currentUser == 1) {
                call.respondText("Updated Successfully")
            } else {
                call.respondText("problem in deleting ")
            }

        } catch (e: Exception) {
            call.respondText("Problem Updating  user... ", status = HttpStatusCode.InternalServerError)

        }
    }

    patch<UserRoute> {
        val parameters = call.receive<Parameters>()
        val user = call.sessions.get<MySession>()?.let {
            userDb.findUserById(it.userId)
        }

        val name = parameters["name"] ?: "${user?.name}"

        val email = parameters["email"] ?: "${user?.emailId}"

        val password = parameters["password"] ?: "${user?.password}"

        val hash = hash(password)

        try {
            val isUpdated = user?.userId?.let { it1 -> userDb.updateUser(it1, name, email, hash) }

            if (isUpdated == 1) {
                val updated = userDb.findUserById(user.userId)
                updated?.userId?.let {
                    call.respond(updated)
                }
            } else {
                call.respond("something went wrong..")
            }
        } catch (e: Throwable) {
            application.log.error("Failed to register user", e)
            call.respond(HttpStatusCode.BadRequest, "Problems deleting User")
        }
    }


}