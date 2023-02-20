package com.undermark5.graphql.server.ktor.execution

import com.expediagroup.graphql.generator.execution.FunctionDataFetcher
import graphql.schema.DataFetchingEnvironment
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.named
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation


@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Inject

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Qualifier(val value: String)


open class KtorDataFetcher(
    target: Any?,
    fn: KFunction<*>,
) : FunctionDataFetcher(target, fn), KoinComponent {

    override fun mapParameterToValue(param: KParameter, environment: DataFetchingEnvironment): Pair<KParameter, Any?>? =
        if (param.hasAnnotation<Inject>()) {
            val qualifier = param.findAnnotation<Qualifier>()?.value
            param.type.classifier?.let {
                (it as? KClass<*>)?.let { klass ->

                    param to getKoin().get(klass, qualifier = qualifier?.let { name -> named(name) })
                }
            }
        } else {
            super.mapParameterToValue(param, environment)
        }
}
