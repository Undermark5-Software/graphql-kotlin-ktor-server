package com.undermark5.graphql.server.ktor.util

import io.github.oshai.KotlinLogging
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE

private val logger = KotlinLogging.logger {}
object KoinLogbackLogger : Logger(Level.DEBUG) {
    override fun display(level: Level, msg: MESSAGE) {
        when (level) {
            Level.DEBUG -> logger.debug(msg)
            Level.INFO -> logger.info(msg)
            Level.WARNING -> logger.warn(msg)
            Level.ERROR -> logger.error(msg)
            Level.NONE -> { /*NOOP*/
            }
        }
    }
}