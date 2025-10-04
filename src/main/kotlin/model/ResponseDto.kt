package com.iponomarev.model

import kotlinx.serialization.Serializable

@Serializable
data class ResponseDto(
    val url: String?
)