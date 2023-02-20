package com.undermark5.graphql.server.ktor.pages.util

import kotlinx.html.Unsafe
import org.intellij.lang.annotations.Language

fun Unsafe.css(@Language("CSS") css: String) = raw(css)