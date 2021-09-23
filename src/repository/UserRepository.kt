package com.todo.repository

import com.todo.data.User
import com.todo.data.dao.UserDoa
import com.todo.data.table.UserTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement

class UserRepository : UserDoa {
    override suspend fun createUser(name: String, email: String, password: String): User? =
        DataBaseFactory.dbQuery {
            var statement: InsertStatement<Number>? = null
            statement = UserTable.insert { user ->
                user[UserTable.name] = name
                user[UserTable.email] = email
                user[UserTable.password] = password

            }
            rowToUser(statement?.resultedValues?.get(0))
        }

    override suspend fun findUserById(userId: Int): User? =
        DataBaseFactory.dbQuery {
            UserTable.select {
                UserTable.userId.eq(userId)
            }.map {
                rowToUser(it)
            }.singleOrNull()
        }


    override suspend fun findUserByEmail(email: String): User? =
        DataBaseFactory.dbQuery {
            UserTable.select {
                UserTable.email.eq(email)
            }.map {
                rowToUser(it)
            }.singleOrNull()
        }

    override suspend fun deleteUssr(userId: Int): Int =
        DataBaseFactory.dbQuery {
            UserTable.deleteWhere {
                UserTable.userId.eq(userId)
            }
        }

    override suspend fun updateUser(userId: Int, name: String, email: String, password: String): Int =
        DataBaseFactory.dbQuery {
            UserTable.update({
                UserTable.userId.eq(userId)
            }) { user ->
                user[UserTable.name] = name
                user[UserTable.email] = email
                user[UserTable.password] = password
            }
        }

    private fun rowToUser(row: ResultRow?): User? =
        row?.let {
            User(
                userId = row[UserTable.userId],
                name = row[UserTable.name],
                emailId = row[UserTable.email],
                password = row[UserTable.password]
            )
        } ?: kotlin.run {
            null
        }

}