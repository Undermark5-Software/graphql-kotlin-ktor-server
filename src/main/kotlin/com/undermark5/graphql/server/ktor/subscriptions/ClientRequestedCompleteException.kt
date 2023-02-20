package com.undermark5.graphql.server.ktor.subscriptions

import kotlinx.coroutines.CancellationException

class ClientRequestedCompleteException: CancellationException("client requested completion")
class KeepAliveViolatedException: CancellationException("client failed to respond to ping in time")