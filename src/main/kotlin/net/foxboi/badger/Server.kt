package net.foxboi.badger

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonPrimitive
import net.foxboi.badger.export.*
import net.foxboi.badger.model.Batch
import net.foxboi.badger.model.Template
import net.foxboi.badger.model.dyn.LocalScope
import net.foxboi.badger.model.dyn.Scope
import net.foxboi.badger.model.dyn.ScopeStack
import net.foxboi.badger.route.Route
import net.foxboi.badger.route.RouteType
import net.foxboi.badger.route.Router
import net.foxboi.badger.serial.SerialDyn
import net.foxboi.badger.serial.batch.SerialBatch
import net.foxboi.badger.serial.template.SerialTemplate

/**
 * The Badger HTTP server.
 */
class Server {
    private lateinit var server: EmbeddedServer<*, *>

    /**
     * Starts the server.
     */
    suspend fun start() {
        val router = withContext(Dispatchers.IO) {
            val asset = Badger.config.router

            if (asset == null) {
                Log.warn { "No routing was set in config" }
                null
            } else {
                val text = Badger.assets.text(asset)
                try {
                    Badger.yaml.decodeFromString<Router>(text)
                } catch (e: Exception) {
                    throw ConfigException("Failed to load router", e)
                }
            }
        }

        server = embeddedServer(CIO, port = Badger.config.port, host = Badger.config.host) {
            routing { configRouting(router) }
        }

        server.startSuspend(wait = true)
    }

    /**
     * Stops the server.
     */
    fun stop() {
        if (!::server.isInitialized) {
            Log.warn { "Server stopped before it was started" }
        }
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

    private fun ApplicationCall.getProperTemplateExporter(): Exporter<Template> {
        val q = request.queryParameters
        val fmt = q["format"] ?: "png"
        return when (fmt) {
            "pdf" -> Pdf
            "png" -> Png
            else -> throw BadRequestException("Unsupported format: '$fmt'")
        }
    }

    private fun ApplicationCall.getProperBatchExporter(): Exporter<Batch> {
        val q = request.queryParameters
        val fmt = q["format"] ?: "hugepdf"
        return when (fmt) {
            "hugepdf" -> HugePdf
            "pngzip" -> PngZip
            "pdfzip" -> PdfZip
            else -> throw BadRequestException("Unsupported format: '$fmt'")
        }
    }

    private fun Routing.createEndpoint(path: String, route: Route) {
        if (route.type == RouteType.TEMPLATE) {
            get(path) {
                Log.info { "GET $path" }

                val stack = ScopeStack()
                stack.pushBack(call.matchVarsFromQuery(route))

                val yml = Badger.assets.text(route.from)

                val serial = Badger.yaml.decodeFromString<SerialTemplate>(yml)
                val template = serial.instantiate()

                call.respondExported(template, call.getProperTemplateExporter(), stack)
            }
        }

        if (route.type == RouteType.BATCH) {
            get(path) {
                Log.info { "GET $path" }

                val stack = ScopeStack()
                stack.pushBack(call.matchVarsFromQuery(route))

                val yml = Badger.assets.text(route.from)

                val serial = Badger.yaml.decodeFromString<SerialBatch>(yml)
                val batch = serial.instantiate()

                call.respondExported(batch, call.getProperBatchExporter(), stack)
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