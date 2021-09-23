package com.todo.routes

import com.todo.API_VERSION
import com.todo.auth.MySession
import com.todo.repository.TodoRepository
import com.todo.repository.UserRepository
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.sessions.*

const val TODOS = "$API_VERSION/todos"

@OptIn(KtorExperimentalLocationsAPI::class)
@Location(TODOS)
class TodoRoute

fun Route.todoRoute(
    userDb: UserRepository,
    todoDb: TodoRepository,
) {
    authenticate("jwt") {

        post<TodoRoute> {
            val todoParameter = call.receive<Parameters>()

            val todo = todoParameter["todo"] ?: return@post call.respondText(
                "Missing todo..",
                status = HttpStatusCode.Unauthorized
            )
            val done = todoParameter["done"] ?: "false"

            val user = call.sessions.get<MySession>()?.let {
                userDb.findUserById(it.userId)
            }

            if (user == null) {
                call.respondText(
                    "problems getting user..",
                    status = HttpStatusCode.BadRequest
                )
            }
            try {
                val currentTodo = user?.userId?.let { it1 ->
                    todoDb.createTodo(
                        it1,
                        todo,
                        done.toBoolean()
                    )
                }
                currentTodo?.id?.let {
                    call.respond(status = HttpStatusCode.OK, currentTodo)
                }

            } catch (e: Throwable) {
                application.log.error("Failed to add todo", e)
                call.respond(HttpStatusCode.BadRequest, "Problems Saving Todo")
            }
        }
    }

    get<TodoRoute> {
        val user = call.sessions.get<MySession>()?.let {
            userDb.findUserById(it.userId)
        }
        if (user == null) {
            call.respond(
                status = HttpStatusCode.BadRequest,
                "problem getting user"
            )
        }
        try {
            val todos = user?.userId?.let { it1 -> todoDb.getAllTodo(it1) }
            if (todos?.isNotEmpty() == true) {
                call.respond(todos)
            }
        } catch (e: Throwable) {
            application.log.error("Failed to add todo", e)
            call.respond(HttpStatusCode.BadRequest, "Problems Saving Todo")
        }
    }

    delete("$API_VERSION/todos/{id}") {
        val id = call.parameters["id"] ?: return@delete call.respondText(
            "invalid id"
        )

        val user = call.sessions.get<MySession>()?.let {
            userDb.findUserById(it.userId)
        }

        if (user == null) {
            call.respond(
                status = HttpStatusCode.BadRequest,
                "problem getting user"
            )
        }

        try {
            val allTodos = user?.userId?.let { it1 -> todoDb.getAllTodo(it1) }
            allTodos?.forEach {
                if (it.id == id.toInt()) {
                    todoDb.deleteTodo(id.toInt())
                    call.respond(it)
                } else {
                    call.respondText("problem deleting todo..")
                }
            }
        } catch (e: Throwable) {
            application.log.error("Failed to add todo", e)
            call.respond(HttpStatusCode.BadRequest, "Problems Saving Todo")
        }
    }

    delete<TodoRoute> {
        val user = call.sessions.get<MySession>()?.let {
            userDb.findUserById(it.userId)
        }

        if (user == null) {
            call.respond(
                status = HttpStatusCode.BadRequest,
                "problem getting user"
            )
        }

        try {
            val isDeleted = user?.userId?.let { it1 -> todoDb.deleteAllTodo(it1) }

            if (isDeleted != null) {
                if (isDeleted > 0)
                    call.respond("All todos deleted successful.. ")
                else
                    call.respond("something went wrong..")

            }
        } catch (e: Throwable) {
            application.log.error("Failed to add todo", e)
            call.respond(HttpStatusCode.BadRequest, "Problems Saving Todo")
        }
    }

    put("$API_VERSION/todos/{id}") {
        val id = call.parameters["id"]
        val user = call.sessions.get<MySession>()?.let {
            userDb.findUserById(it.userId)
        }

        val parameter = call.receive<Parameters>()

        val todo = parameter["todo"] ?: return@put call.respondText(
            "missing field",
            status = HttpStatusCode.Unauthorized
        )

        val done = parameter["todo"] ?: "false"

        if (user == null) {
            call.respond(
                status = HttpStatusCode.BadRequest,
                "problem getting user"
            )
        }

        try {
            val allTodos = user?.userId?.let { it1 -> todoDb.getAllTodo(it1) }
            allTodos?.forEach {
                if(it.id == id?.toInt()){
                    todoDb.updateTodo(id.toInt(),todo,done.toBoolean())
                    call.respondText("updated successfully...")
                }else{
                    call.respond("something went wrong..")
                }
            }
        } catch (e: Throwable) {
            application.log.error("Failed to add todo", e)
            call.respond(HttpStatusCode.BadRequest, "Problems Saving Todo")
        }
    }
}