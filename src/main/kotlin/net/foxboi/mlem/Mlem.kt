package net.foxboi.mlem

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import kotlinx.io.files.Path
import net.foxboi.mlem.asset.AssetManager
import net.foxboi.mlem.export.pdf.PdfManager
import net.foxboi.mlem.export.zip.ZipManager
import java.util.*

object Mlem {
    val yaml = Yaml(
        configuration = YamlConfiguration(
            strictMode = false
        )
    )

    val config = MlemConfig.load()

    val server = MlemServer(
        port = config.port,
        host = config.host
    )

    val assets = AssetManager(
        assetDir = config.assetsDir,
        tmpDir = Path(config.tempDir, "downloads")
    )

    val pdf = PdfManager(
        tmpDir = Path(config.tempDir, "pdf")
    )

    val zip = ZipManager(
        tmpDir = Path(config.tempDir, "zip")
    )

    suspend fun run() {
        Log.info { "Starting MLEM server" }
        server.start()
    }

    fun time(): Long {
        return System.currentTimeMillis()
    }

    fun uuid(): String {
        return UUID.randomUUID().toString()
    }

    fun env(name: String): String? {
        return System.getenv(name)
    }

    fun property(name: String): String? {
        return System.getProperty(name)
    }
}