package com.iponomarev.model

import kotlinx.serialization.Serializable

@Serializable
data class RequestDto(
    val url: String
)
