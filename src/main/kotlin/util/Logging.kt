package com.iponomarev.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface Logging {
    val log: Logger
        get() = LoggerFactory.getLogger(this::class.java)
}

object AppLogger : Logging