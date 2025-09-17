package net.foxboi.mlem

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonPrimitive
import net.foxboi.mlem.export.*
import net.foxboi.mlem.model.dyn.LocalScope
import net.foxboi.mlem.model.dyn.Scope
import net.foxboi.mlem.model.dyn.ScopeStack
import net.foxboi.mlem.route.Route
import net.foxboi.mlem.route.RouteType
import net.foxboi.mlem.route.Router
import net.foxboi.mlem.serial.SerialDyn
import net.foxboi.mlem.serial.batch.SerialBatch
import net.foxboi.mlem.serial.template.SerialTemplate

class MlemServer(
    val port: Int = 80,
    val host: String = "0.0.0.0"
) {
    private lateinit var server: EmbeddedServer<*, *>

    suspend fun start() {
        val asset = Mlem.config.router
        val router = if (asset == null) {
            Log.warn { "No routing was set in config" }
            null
        } else {
            val text = Mlem.assets.text(asset)
            try {
                Mlem.yaml.decodeFromString<Router>(text)
            } catch (e: Exception) {
                throw ConfigException("Failed to load router", e)
            }
        }

        server = embeddedServer(CIO, port = port, host = host) {
            routing { configRouting(router) }
        }
        server.startSuspend(wait = true)
    }

    fun stop() {
        server.stop()
    }


    private suspend fun ApplicationCall.matchVarsFromBody(route: Route): Scope {
        val data = receive<Map<String, JsonPrimitive>>()

        val scope = LocalScope()

        for ((name, param) in route.params) {
            val qValue = data[name]

            val serial = if (qValue == null) {
                param.fallback ?: throw BadRequestException("Missing required body parameter '$name'")
            } else {
                SerialDyn(qValue.content)
            }

            val expr = try {
                serial.instantiateAsExpr(param.type)
            } catch (e: Exception) {
                throw BadRequestException("Malformed body parameter '$name'", e)
            }

            scope.set(name, expr)
        }

        return scope
    }

    private fun ApplicationCall.matchVarsFromQuery(route: Route): Scope {
        val q = request.queryParameters
        val scope = LocalScope()

        for ((name, param) in route.params) {
            val qValue = q[name]

            val serial = if (qValue == null) {
                param.fallback ?: throw BadRequestException("Missing required input parameter '$name'")
            } else {
                SerialDyn(qValue)
            }

            val expr = try {
                serial.instantiateAsExpr(param.type)
            } catch (e: Exception) {
                throw BadRequestException("Malformed input parameter '$name'", e)
            }

            scope.set(name, expr)
        }

        return scope
    }

    private fun ApplicationCall.getProperTemplateExporter(): TemplateExporter {
        val q = request.queryParameters
        val fmt = q["format"] ?: "png"
        return when (fmt) {
            "pdf" -> PdfTemplateExporter
            "png" -> PngTemplateExporter
            else -> throw BadRequestException("Unsupported format: '$fmt'")
        }
    }

    private fun ApplicationCall.getProperBatchExporter(): BatchExporter {
        val q = request.queryParameters
        val fmt = q["format"] ?: "hugepdf"
        return when (fmt) {
            "hugepdf" -> HugePdfBatchExporter
            "pngzip" -> PngZipBatchExporter
            "pdfzip" -> PdfZipBatchExporter
            else -> throw BadRequestException("Unsupported format: '$fmt'")
        }
    }

    private fun Routing.createEndpoint(path: String, route: Route) {
        if (route.type == RouteType.TEMPLATE) {
            get(path) {
                Log.info { "GET $path" }

                val stack = ScopeStack()
                stack.pushBack(call.matchVarsFromQuery(route))

                val yml = Mlem.assets.text(route.from)

                val serial = Mlem.yaml.decodeFromString<SerialTemplate>(yml)
                val template = serial.instantiate()

                call.getProperTemplateExporter().respondTo(call, template, stack, Mlem.assets)
            }
        }

        if (route.type == RouteType.BATCH) {
            get(path) {
                Log.info { "GET $path" }

                val stack = ScopeStack()
                stack.pushBack(call.matchVarsFromQuery(route))

                val yml = Mlem.assets.text(route.from)

                val serial = Mlem.yaml.decodeFromString<SerialBatch>(yml)
                val batch = serial.instantiate()

                call.getProperBatchExporter().respondTo(call, batch, stack, Mlem.assets)
            }
        }
    }

    private fun Routing.configRouting(router: Router?) {
        get("/") {
            call.respondText("Hi I am MLEM server")
        }

        if (router != null) {
            for ((path, route) in router.routes) {
                createEndpoint(path, route)
            }
        }
    }
}