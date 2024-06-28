package com.example.myapplication.data.response

data class LoginResponse(val status: String, val message: String, val user: User? = null) {
    data class User(val id: Int, val name: String, val email: String) // Customize user data fields
}
