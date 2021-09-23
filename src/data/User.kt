package com.todo.data

import io.ktor.auth.*

data class User(
    val userId:Int,
    val emailId:String,
    val name:String,
    val password:String,
) : Principal
