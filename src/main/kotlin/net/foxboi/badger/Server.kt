package net.foxboi.badger

import io.ktor.http.*
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
        server = embeddedServer(CIO, port = Badger.config.port, host = Badger.config.host) {
            routing { configRouting() }
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
        val fmt = q["-format"] ?: "png"
        return when (fmt.lowercase()) {
            "pdf" -> Pdf
            "png" -> Png
            "jpg", "jpeg" -> Jpeg
            "webp" -> Webp
            else -> throw BadRequestException("Unsupported format: '$fmt'")
        }
    }

    private fun ApplicationCall.getProperBatchExporter(): Exporter<Batch> {
        val q = request.queryParameters
        val fmt = q["-format"] ?: "hugepdf"
        return when (fmt.lowercase()) {
            "hugepdf" -> HugePdf
            "pngzip" -> PngZip
            "jpgzip", "jpegzip" -> JpegZip
            "webpzip" -> WebpZip
            "pdfzip" -> PdfZip
            else -> throw BadRequestException("Unsupported format: '$fmt'")
        }
    }

    private suspend fun ApplicationCall.handleRoute(path: String, route: Route) {
        if (route.type == RouteType.TEMPLATE) {
            Log.info { "GET $path" }

            val stack = ScopeStack()
            stack.pushBack(matchVarsFromQuery(route))

            val yml = Badger.assets.text(route.from)

            val serial = Badger.yaml.decodeFromString<SerialTemplate>(yml)
            val template = serial.instantiate()

            respondExported(template, getProperTemplateExporter(), stack)
        }

        if (route.type == RouteType.BATCH) {
            Log.info { "GET $path" }

            val stack = ScopeStack()
            stack.pushBack(matchVarsFromQuery(route))

            val yml = Badger.assets.text(route.from)

            val serial = Badger.yaml.decodeFromString<SerialBatch>(yml)
            val batch = serial.instantiate()

            respondExported(batch, getProperBatchExporter(), stack)
        }
    }

    private fun Routing.configRouting() {
        get("/{route...}") {
            try {
                val routePath = call.parameters.getAll("route")

                if (routePath == null || routePath.isEmpty()) {
                    call.respondText("-- Badger --\nHi I am Badger server")
                } else {
                    val routeString = routePath.joinToString("") { "/$it" }

                    // TODO do something with caching to not have to reload the router all the time?
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

                    val route = router?.route(routeString)

                    when {
                        router == null || route == null -> {
                            call.respondText("-- Badger --\n404 Not Found", status = HttpStatusCode.NotFound)
                        }

                        else -> {
                            call.handleRoute(routeString, route)
                        }
                    }
                }
            } catch (e: BadRequestException) {
                call.respondText("-- Badger --\n400 Bad Request\n${e.message}", status = HttpStatusCode.BadRequest)
            }
        }
    }
}