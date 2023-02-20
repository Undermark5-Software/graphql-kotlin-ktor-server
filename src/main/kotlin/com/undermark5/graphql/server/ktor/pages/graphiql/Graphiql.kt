package com.undermark5.graphql.server.ktor.pages.graphiql

import com.undermark5.graphql.server.ktor.GraphqlConfigurationProperties
import com.undermark5.graphql.server.ktor.pages.util.Router
import com.undermark5.graphql.server.ktor.pages.util.RouterType
import com.undermark5.graphql.server.ktor.pages.util.css
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.html.*
import org.intellij.lang.annotations.Language
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

/* Original Html
<!DOCTYPE html>
<html lang="en_US">

<head>
    <meta charset=utf-8 />
    <meta name="viewport" content="user-scalable=no, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, minimal-ui">
    <meta name="robots" content="noindex, nofollow, noimageindex, nosnippet">
    <title>GraphiQL</title>
    <style>
        body {
            height: 100%;
            margin: 0;
            width: 100%;
            overflow: hidden;
        }

        #graphiql {
            height: 100vh;
        }
    </style>

    <link rel="stylesheet" href="https://unpkg.com/graphiql/graphiql.min.css" />
    <link rel="stylesheet" href="https://unpkg.com/@graphiql/plugin-explorer/dist/style.css" />
    <link rel="stylesheet" href="https://unpkg.com/@graphiql/plugin-code-exporter/dist/style.css" />
</head>

<body>
<div id="graphiql">Loading...</div>

<script src="https://unpkg.com/react@17/umd/react.development.js"
        integrity="sha512-Vf2xGDzpqUOEIKO+X2rgTLWPY+65++WPwCHkX2nFMu9IcstumPsf/uKKRd5prX3wOu8Q0GBylRpsDB26R6ExOg=="
        crossorigin="anonymous"></script>
<script src="https://unpkg.com/react-dom@17/umd/react-dom.development.js"
        integrity="sha512-Wr9OKCTtq1anK0hq5bY3X/AvDI5EflDSAh0mE9gma+4hl+kXdTJPKZ3TwLMBcrgUeoY0s3dq9JjhCQc7vddtFg=="
        crossorigin="anonymous"></script>
<script src="https://unpkg.com/graphiql@2.2.0/graphiql.min.js"
        integrity="sha512-FVCV2//UVo1qJ3Kg6kkHLe0Hg+IJhjrGa+aYHh8xD4KmwbbjthIzvaAcCJsQgA43+k+6u7HqORKXMyMt82Srfw=="
        crossorigin="anonymous"></script>
<script src="https://unpkg.com/@graphiql/plugin-explorer@0.1.12/dist/graphiql-plugin-explorer.umd.js"
        integrity="sha512-Fjas/uSkzvsFjbv4jqU9nt4ulU7LDjiMAXW2YFTYD96NgKS1fhhAsGR4b2k2VaVLsE29aia3vyobAq9TNzusvA=="
        crossorigin="anonymous"></script>
<script src="https://unpkg.com/@graphiql/plugin-code-exporter/dist/graphiql-plugin-code-exporter.umd.js"
        integrity="sha512-NwP+k36ExLYeIqp2lniZCblbz/FLJ/lQlBV55B6vafZWIYppwHUp1gCdvlaaUjV95RWPInQy4z/sIa56psJy/g=="
        crossorigin="anonymous"></script>

<script>
    var serverUrl = '/${graphQLEndpoint}';
    var subscriptionUrl = '/${subscriptionsEndpoint}';
    var fetcher = GraphiQL.createFetcher({
        url: serverUrl,
        subscriptionUrl
    });

    var fetchSnippet = {
        language: 'JavaScript',
        name: 'Fetch',
        codeMirrorMode: 'jsx',
        options: [],
        generate: (generateOptions) => {
            const operation = generateOptions.operationDataList[0];
            const context = generateOptions.context;
            const headers = context.headers.length !== 0 ? `headers: ${context.headers},\n  ` : '';
            const variables = JSON.stringify(operation.variables);
            return `
const res = await fetch("${context.serverUrl}", {
  method: 'POST',
  ${headers}body: JSON.stringify({
    operationName: "${operation.name}",
    query: \`${operation.query.replaceAll("\n", "\\n")}\`,
    variables: ${variables}
  }),
});

const { errors, data } = await res.json();

// Do something with the response
console.log(data, errors);
`;
        }
    };

    var curlSnippet = {
        language: 'Bash',
        name: 'Curl',
        codeMirrorMode: 'jsx',
        options: [],
        generate: (generateOptions) => {
            const operation = generateOptions.operationDataList[0];
            const context = generateOptions.context;
            let headersObject;
            try {
                headersObject = JSON.parse(context.headers);
            } catch (e) {
                headersObject = {};
            }
            const headers = Object.entries(headersObject)
                .reduce((acc, [headerName, headerValue]) => `${acc} -H '${headerName}: ${headerValue}' \\\n`, '');
            const payload = JSON.stringify({
                operationName: operation.name,
                query: `${operation.query.replaceAll("\n", "\\n")}`,
                variables: operation.variables
            })
            return `curl '${context.serverUrl}' \\\n${headers}--data-raw $'${payload}' --compressed`;
        }
    }

    function GraphiQLWithPlugins() {
        var [query, setQuery] = React.useState('');
        var [variables, setVariables] = React.useState('');
        var [headers, setHeaders] = React.useState('');
        var defaultHeaders = `{\n  "content-type": "application/json"\n}`;

        var explorerPlugin = GraphiQLPluginExplorer.useExplorerPlugin({
            query,
            onEdit: setQuery,
        });
        var exporterPlugin = GraphiQLPluginCodeExporter.useExporterPlugin({
            query,
            variables,
            context: { serverUrl, headers },
            snippets: [curlSnippet, fetchSnippet]
        });

        return React.createElement(GraphiQL, {
            fetcher,
            query,
            onEditQuery: setQuery,
            variables,
            onEditVariables: setVariables,
            headers: '',
            onEditHeaders: setHeaders,
            defaultHeaders,
            defaultEditorToolsVisibility: true,
            plugins: [explorerPlugin, exporterPlugin]
        });
    }

    ReactDOM.render(
        React.createElement(GraphiQLWithPlugins),
        document.getElementById('graphiql')
    );
</script>
</body>

</html>

*/

@Language("js")
private fun graphiqlScript(graphqlEndpoint: String, subscriptionsEndpoint: String) = """
    |    
    |        var serverUrl = '/${graphqlEndpoint}';
    |        var subscriptionUrl = `ws://${'$'}{location.host}/${subscriptionsEndpoint}`;
    |        var fetcher = GraphiQL.createFetcher({
    |            url: serverUrl,
    |            subscriptionUrl
    |        });
    |        
    |        var fetchSnippet = {
    |            language: 'JavaScript',
    |            name: 'Fetch',
    |            codeMirrorMode: 'jsx',
    |            options: [],
    |            generate: (generateOptions) => {
    |                const operation = generateOptions.operationDataList[0];
    |                const context = generateOptions.context;
    |                const headers = context.headers.length !== 0 ? `headers: ${'$'}{context.headers},\n  ` : '';
    |                const variables = JSON.stringify(operation.variables);
    |                return `
    |        const res = await fetch("${'$'}{context.serverUrl}", {
    |          method: 'POST',
    |          ${'$'}{headers}body: JSON.stringify({
    |            operationName: "${'$'}{operation.name}",
    |            query: \`${'$'}{operation.query.replaceAll("\n", "\\n")}\`,
    |            variables: ${'$'}{variables}
    |          }),
    |        });
    |        
    |        const { errors, data } = await res.json();
    |        
    |        // Do something with the response
    |        console.log(data, errors);
    |        `;
    |            }
    |        };
    |        
    |        var curlSnippet = {
    |            language: 'Bash',
    |            name: 'Curl',
    |            codeMirrorMode: 'jsx',
    |            options: [],
    |            generate: (generateOptions) => {
    |                const operation = generateOptions.operationDataList[0];
    |                const context = generateOptions.context;
    |                let headersObject;
    |                try {
    |                    headersObject = JSON.parse(context.headers);
    |                } catch (e) {
    |                    headersObject = {};
    |                }
    |                const headers = Object.entries(headersObject)
    |                    .reduce((acc, [headerName, headerValue]) => `${'$'}{acc} -H '${'$'}{headerName}: ${'$'}{headerValue}' \\\n`, '');
    |                const payload = JSON.stringify({
    |                    operationName: operation.name,
    |                    query: `${'$'}{operation.query.replaceAll("\n", "\\n")}`,
    |                    variables: operation.variables
    |                })
    |                return `curl '${'$'}{context.serverUrl}' \\\n${'$'}{headers}--data-raw ${'$'}'${'$'}{payload}' --compressed`;
    |            }
    |        }
    |        
    |        function GraphiQLWithPlugins() {
    |            var [query, setQuery] = React.useState('');
    |            var [variables, setVariables] = React.useState('');
    |            var [headers, setHeaders] = React.useState('');
    |            var defaultHeaders = `{\n  "content-type": "application/json"\n}`;
    |        
    |            var explorerPlugin = GraphiQLPluginExplorer.useExplorerPlugin({
    |                query,
    |                onEdit: setQuery,
    |            });
    |            var exporterPlugin = GraphiQLPluginCodeExporter.useExporterPlugin({
    |                query,
    |                variables,
    |                context: { serverUrl, headers },
    |                snippets: [curlSnippet, fetchSnippet]
    |            });
    |        
    |            return React.createElement(GraphiQL, {
    |                fetcher,
    |                query,
    |                onEditQuery: setQuery,
    |                variables,
    |                onEditVariables: setVariables,
    |                headers: '',
    |                onEditHeaders: setHeaders,
    |                defaultHeaders,
    |                defaultEditorToolsVisibility: true,
    |                plugins: [explorerPlugin, exporterPlugin]
    |            });
    |        }
    |        
    |        ReactDOM.render(
    |            React.createElement(GraphiQLWithPlugins),
    |            document.getElementById('graphiql')
    |        );
""".trimMargin()

suspend fun ApplicationCall.graphiql(graphqlEndpoint: String, subscriptionsEndpoint: String) = respondHtml {
    lang = "en_US"

    head {
        charset("utf-8")
        meta(
            name = "viewport",
            content = "user-scalable=no, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, minimal-ui"
        )
        meta(name = "robots", content = "noindex, nofollow, noimageindex, nosnippet")
        title("GraphiQL")
        style {
            unsafe {
                css(
                    """
                    |body {
                    |    height: 100%;
                    |    margin: 0;
                    |    width: 100%;
                    |    overflow: hidden;
                    |}
                    |        
                    |#graphiql {
                    |    height: 100vh;
                    |}
                """.trimMargin()
                )
            }
        }
        link(rel = "stylesheet", href = "https://unpkg.com/graphiql/graphiql.min.css")
        link(rel = "stylesheet", href = "https://unpkg.com/@graphiql/plugin-explorer/dist/style.css")
        link(rel = "stylesheet", href = "https://unpkg.com/@graphiql/plugin-code-exporter/dist/style.css")
    }
    body {
        div {
            id = "graphiql"
            +"Loading..."
        }
        script(
            src = "https://unpkg.com/react@17/umd/react.development.js",
            integrity = "sha512-Vf2xGDzpqUOEIKO+X2rgTLWPY+65++WPwCHkX2nFMu9IcstumPsf/uKKRd5prX3wOu8Q0GBylRpsDB26R6ExOg==",
            crossorigin = "anonymous"
        )
        script(
            src = "https://unpkg.com/react-dom@17/umd/react-dom.development.js",
            integrity = "sha512-Wr9OKCTtq1anK0hq5bY3X/AvDI5EflDSAh0mE9gma+4hl+kXdTJPKZ3TwLMBcrgUeoY0s3dq9JjhCQc7vddtFg==",
            crossorigin = "anonymous"
        )
        script(
            src = "https://unpkg.com/graphiql@2.2.0/graphiql.min.js",
            integrity = "sha512-FVCV2//UVo1qJ3Kg6kkHLe0Hg+IJhjrGa+aYHh8xD4KmwbbjthIzvaAcCJsQgA43+k+6u7HqORKXMyMt82Srfw==",
            crossorigin = "anonymous"
        )
        script(
            src = "https://unpkg.com/@graphiql/plugin-explorer@0.1.12/dist/graphiql-plugin-explorer.umd.js",
            integrity = "sha512-Fjas/uSkzvsFjbv4jqU9nt4ulU7LDjiMAXW2YFTYD96NgKS1fhhAsGR4b2k2VaVLsE29aia3vyobAq9TNzusvA==",
            crossorigin = "anonymous"
        )
        script(
            src = "https://unpkg.com/@graphiql/plugin-code-exporter/dist/graphiql-plugin-code-exporter.umd.js",
            integrity = "sha512-NwP+k36ExLYeIqp2lniZCblbz/FLJ/lQlBV55B6vafZWIYppwHUp1gCdvlaaUjV95RWPInQy4z/sIa56psJy/g==",
            crossorigin = "anonymous"
        )
        script {
            unsafe {
                +graphiqlScript(graphqlEndpoint, subscriptionsEndpoint)
            }
        }
    }
}

@HtmlTagMarker
private inline fun FlowOrMetaDataOrPhrasingContent.script(
    type: String? = null,
    src: String,
    integrity: String,
    crossorigin: String,
    crossinline block: SCRIPT.() -> Unit = {}
) = SCRIPT(
    attributesMapOf("type", type, "src", src, "integrity", integrity, "crossorigin", crossorigin),
    consumer
).visit(block)


@Module
internal class GraphiqlRoute {
    @Single
    @Named("graphiql")
    fun graphiqlRoute(configProperties: GraphqlConfigurationProperties): Router =
        _GraphiqlRoute(configProperties)
}

@Suppress("ClassName")
private class _GraphiqlRoute(private val configProperties: GraphqlConfigurationProperties): RouterType {
    override fun invoke(route: Route): Route {
        return route.get(configProperties.graphiql.endpoint) {
            call.graphiql(
                graphqlEndpoint = configProperties.endpoint,
                subscriptionsEndpoint = configProperties.subscriptions.endpoint,
            )
        }
    }
}
