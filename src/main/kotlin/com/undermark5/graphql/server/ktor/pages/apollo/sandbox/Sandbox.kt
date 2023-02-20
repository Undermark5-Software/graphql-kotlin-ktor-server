package com.undermark5.graphql.server.ktor.pages.apollo.sandbox

import com.undermark5.graphql.server.ktor.GraphqlConfigurationProperties
import com.undermark5.graphql.server.ktor.pages.util.Router
import com.undermark5.graphql.server.ktor.pages.util.RouterType
import com.undermark5.graphql.server.ktor.pages.util.css
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.*
import org.intellij.lang.annotations.Language
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Language("js")
private fun sandboxScript(initialEndpoint: String) = """
    |  new window.EmbeddedSandbox({
    |    target: '#embedded-sandbox',
    |    initialEndpoint: `${'$'}{location.protocol}//${'$'}{location.host}/$initialEndpoint`,
    |    includeCookies: false,
    |  });
""".trimMargin()

suspend fun ApplicationCall.apolloSandbox(endpoint: String) = respondHtml {
    head {
        charset("utf-8")
        meta(name = "robots", content = "noindex, nofollow, noimageindex, nosnippet")
        title("Apollo Sandbox")
        style {
            unsafe {
                css(
                    """
                    |body {
                    |    height: 100vh;
                    |    margin: 0;
                    |    width: 100%;
                    |    overflow: hidden;
                    |}
                """.trimMargin()
                )
            }
        }
    }
    body {
        div(style = "width: 100%; height: 100vh", id = "embedded-sandbox")
        script(src = "https://embeddable-sandbox.cdn.apollographql.com/_latest/embeddable-sandbox.umd.production.min.js") {}
        script {
            unsafe {
                +sandboxScript(endpoint)
            }
        }
    }
}

@HtmlTagMarker
private inline fun FlowOrMetaDataOrPhrasingContent.div(
    style: String,
    id: String,
    crossinline block: DIV.() -> Unit = {}
) = DIV(
    attributesMapOf("style", style, "id", id),
    consumer,
).visit(block)

@Module
internal class ApolloSandboxRoute {
    @Single
    @Named("sandbox")
    fun sandboxRoute(configProperties: GraphqlConfigurationProperties): Router = _ApolloSandboxRoute(configProperties)

}

@Suppress("ClassName")
private class _ApolloSandboxRoute(private val configProperties: GraphqlConfigurationProperties) : RouterType {
    override fun invoke(route: Route): Route {
        return route.get(configProperties.apolloSandbox.endpoint) {
            call.apolloSandbox(configProperties.endpoint)
        }
    }

}