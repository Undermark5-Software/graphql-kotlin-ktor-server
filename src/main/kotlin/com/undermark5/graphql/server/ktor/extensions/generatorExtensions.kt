/*
 * Copyright 2022 Expedia, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Slight modifications to work for Ktor rather than Spring by undermark5 2023
 */
package com.undermark5.graphql.server.ktor.extensions

import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.server.Schema
import com.expediagroup.graphql.server.operations.Mutation
import com.expediagroup.graphql.server.operations.Query
import com.expediagroup.graphql.server.operations.Subscription


/**
 * Converts given object to a [TopLevelObject] wrapper.
 */
internal fun Query.toTopLevelObject(): TopLevelObject = this.let {
    TopLevelObject(it, it::class)
}

/**
 * Converts given object to a [TopLevelObject] wrapper.
 */
internal fun Mutation.toTopLevelObject(): TopLevelObject = this.let {
    TopLevelObject(it, it::class)
}

/**
 * Converts given object to a [TopLevelObject] wrapper.
 */
internal fun Subscription.toTopLevelObject(): TopLevelObject = this.let {
    TopLevelObject(it, it::class)
}

/**
 * Converts given object to a [TopLevelObject] wrapper.
 */
internal fun Schema.toTopLevelObject(): TopLevelObject = this.let {
    TopLevelObject(it, it::class)
}
