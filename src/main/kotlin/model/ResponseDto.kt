package com.iponomarev.model

import kotlinx.serialization.Serializable

@Serializable
data class ResponseDto(
    val success: Boolean = true,
    val url: String? = null,
    val error: String? = null
)