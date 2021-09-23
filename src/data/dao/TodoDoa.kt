package com.todo.data.dao

import com.todo.data.Todo

interface TodoDoa {

    suspend fun createTodo(
        userId: Int,
        todo: String,
        done: Boolean
    ): Todo?

    suspend fun getAllTodo(
        userId: Int
    ): List<Todo>

    suspend fun getTodo(
        id: Int
    ): Todo?

    suspend fun deleteTodo(
        id: Int
    ): Int

    suspend fun deleteAllTodo(userId: Int): Int

    suspend fun updateTodo(
        id: Int,
        todo: String,
        done: Boolean
    ): Int
}