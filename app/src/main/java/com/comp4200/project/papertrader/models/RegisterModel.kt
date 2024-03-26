package com.comp4200.project.papertrader.models

data class RegisterModel(
    val email: String,
    val username: String,
    val password: String,
    val confirmPassword: String
)
