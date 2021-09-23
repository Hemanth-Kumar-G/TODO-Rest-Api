package com.todo.data.dao

import com.todo.data.User

interface UserDoa {

    suspend fun createUser(
        name: String,
        email: String,
        password: String
    ): User?

    suspend fun findUserById(userId: Int): User?

    suspend fun findUserByEmail(email: String): User?

    suspend fun deleteUssr(userId: Int): Int

    suspend fun updateUser(
        userId: Int,
        name: String,
        email: String,
        password: String
    ): Int

}