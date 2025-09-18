package net.foxboi.badger

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.io.files.Path
import net.foxboi.badger.asset.AssetManager
import net.foxboi.badger.export.pdf.PdfManager
import net.foxboi.badger.export.zip.ZipManager
import java.util.*
import kotlin.concurrent.thread

/**
 * The main object of the Badger application.
 */
object Badger {
    /**
     * The [Yaml] configuration used by all YAML parsing in Badger.
     */
    val yaml = Yaml(
        configuration = YamlConfiguration(
            strictMode = false
        )
    )

    /**
     * The [Config].
     */
    val config = Config.load()

    /**
     * The [Server].
     */
    val server = Server()

    /**
     * The [AssetManager].
     */
    val assets = AssetManager(
        assetDir = config.assetsDir,
        tmpDir = Path(config.tempDir, "downloads")
    )

    /**
     * A [PdfManager] configured to use the configured temporary directory.
     */
    val pdf = PdfManager(
        tmpDir = Path(config.tempDir, "pdf")
    )

    /**
     * A [ZipManager] configured to use the configured temporary directory.
     */
    val zip = ZipManager(
        tmpDir = Path(config.tempDir, "zip")
    )

    private val shutdownThread = thread(start = false) {
        stop()
    }

    /**
     * Runs the application.
     */
    suspend fun run() {
        Log.info { "Starting server" }

        Runtime.getRuntime().addShutdownHook(shutdownThread)

        server.start()
    }

    private fun stop() {
        Log.info { "Terminated, kthxbye" }
        server.stop()
        assets.close()

        Thread.sleep(300) // Do nothing for a while idk why
        Log.shutdown()
    }

    /**
     * Gets the current time, as a timestamp in milliseconds since Epoch.
     */
    fun time(): Long {
        return System.currentTimeMillis()
    }

    /**
     * Get a random UUID string.
     */
    fun uuid(): String {
        return UUID.randomUUID().toString()
    }

    /**
     * Get an environment variable.
     */
    fun env(name: String): String? {
        return System.getenv(name)
    }

    /**
     * Get a JVM property.
     */
    fun property(name: String): String? {
        return System.getProperty(name)
    }
}