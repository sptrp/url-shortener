package com.iponomarev.service

class UrlProcessorService {
    fun shorten(url: String): String {
        return "$url test shortener"
    }

    fun unshorten(id: String): String {
        return "$id test unshortener"
    }
}