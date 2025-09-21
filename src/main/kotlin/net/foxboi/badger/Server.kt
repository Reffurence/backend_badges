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
import java.io.PrintWriter
import java.io.StringWriter

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

    private inline fun writeMessage(out: PrintWriter.() -> Unit): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)

        pw.println("-- Badger --")
        pw.out()
        pw.close()

        return sw.toString()
    }

    private suspend fun ApplicationCall.handleGet(path: String, route: Route) {
        try {
            Log.info { "GET ${request.uri}  (${route.type.desc} route from ${route.from})" }

            if ("-help" in request.queryParameters) {
                respondText(writeMessage {
                    println(route.writeHelp(path))
                })
            } else when (route.type) {
                RouteType.TEMPLATE -> {
                    val stack = ScopeStack()
                    stack.pushBack(matchVarsFromQuery(route))

                    val yml = Badger.assets.text(route.from)

                    val serial = Badger.yaml.decodeFromString<SerialTemplate>(yml)
                    val template = serial.instantiate()

                    respondExported(template, getProperTemplateExporter(), stack)
                }

                RouteType.BATCH -> {
                    val stack = ScopeStack()
                    stack.pushBack(matchVarsFromQuery(route))

                    val yml = Badger.assets.text(route.from)

                    val serial = Badger.yaml.decodeFromString<SerialBatch>(yml)
                    val batch = serial.instantiate()

                    respondExported(batch, getProperBatchExporter(), stack)
                }

                RouteType.RAW -> {
                    respondSource(Badger.assets.open(route.from), contentType = route.contentType)
                }
            }
        } catch (e: BadRequestException) {
            val msg = writeMessage {
                println("400 Bad Request")
                println(e.message)
                println()
                println(route.writeHelp(path))
            }
            respondText(msg, status = HttpStatusCode.BadRequest)
        }
    }

    private suspend fun getRouter(): Router? {
        // TODO do something with caching to not have to reload the router all the time?
        return withContext(Dispatchers.IO) {
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
    }

    private suspend inline fun ApplicationCall.handle500(action: suspend ApplicationCall.() -> Unit) {
        try {
            action()
        } catch (e: Exception) {
            val msg = writeMessage {
                println("500 Internal Server Error")
                println("This may be caused by wrong input, currently Badger can't fully trace back")
                println("whether evaluation errors come from query parameters or internal configuration")
                println("errors. Attached below is the stack trace, in the hope that it's useful.")
                println()
                e.printStackTrace(this)
            }
            respondText(msg, status = HttpStatusCode.InternalServerError)
        } catch (_: StackOverflowError) {
            // Stack-overflows are a common vulnerability; let's catch them, idk why they aren't an Exception
            val msg = writeMessage {
                println("500 Internal Server Error")
                println("A stack overflow happened during evaluation.")
            }
            respondText(msg, status = HttpStatusCode.InternalServerError)
        }
    }

    private fun Routing.configRouting() {
        get("/{route...}") {
            call.handle500 {
                val routePath = call.parameters.getAll("route")

                if (routePath == null || routePath.isEmpty()) {
                    if ("-help" in call.request.queryParameters) {
                        val router = getRouter()

                        call.respondText(writeMessage {
                            println("Route overview")
                            println()
                            println(router?.writeAllHelp() ?: "No routing was configured")
                        })
                    } else {
                        call.respondText(writeMessage {
                            println("Hi I am Badger server")
                        })
                    }
                } else {
                    val routeString = routePath.joinToString("") { "/$it" }

                    val router = getRouter()
                    val route = router?.route(routeString)

                    when {
                        router == null -> {
                            val msg = writeMessage {
                                println("404 Not Found")
                                println()
                                println("No router was configured")
                            }
                            call.respondText(msg, status = HttpStatusCode.NotFound)
                        }

                        route == null -> {
                            val msg = writeMessage {
                                println("404 Not Found")
                                println()
                                println(router.writeAvailableRoutes())
                            }
                            call.respondText(msg, status = HttpStatusCode.NotFound)
                        }

                        else -> {
                            call.handleGet(routeString, route)
                        }
                    }
                }
            }
        }
    }
}