package com.undermark5.graphql.server.ktor.pages.util

import io.ktor.server.routing.*

typealias Router = Route.() -> Route
typealias RouterType = (Route) -> Route