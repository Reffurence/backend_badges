@file:UseSerializers(PathSerializer::class)

package net.foxboi.badger

import io.ktor.utils.io.*
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.decodeFromString
import net.foxboi.badger.asset.Asset
import net.foxboi.badger.util.PathSerializer
import kotlin.system.exitProcess

@Serializable
data class Config(
    val port: Int = 80,
    val host: String = "0.0.0.0",

    @SerialName("assets_dir")
    val assetsDir: Path = Path("./assets"),

    @SerialName("temp_dir")
    val tempDir: Path = Path(SystemTemporaryDirectory, "mlem"),

    val router: Asset? = null
) {
    init {
        if (port !in 0..0xFFFF)
            throw ConfigException("Invalid port $port")
    }

    private fun withContextPath(context: Path) = copy(
        assetsDir = resolveParent(context, assetsDir),
        tempDir = resolveParent(context, tempDir)
    )

    private fun resolveParent(parent: Path, path: Path): Path {
        if (path.isAbsolute) return path // Don't change absolute paths
        return Path(parent, "$path")
    }

    companion object {
        val default = Config()

        private fun String.toPort() = toIntOrNull() ?: throw ConfigException("Not a port number: $this")
        private fun String.toPath() = kotlinx.io.files.Path(this)
        private fun String.toAsset() = Asset.fromStringOrNull(this) ?: throw ConfigException("Invalid asset: $this")

        fun loadFromEnv(default: Config = Config.default) = Config(
            Badger.env("PORT")?.toPort() ?: default.port,
            Badger.env("HOST") ?: default.host,

            Badger.env("ASSETS_DIR")?.toPath() ?: default.assetsDir,
            Badger.env("TEMP_DIR")?.toPath() ?: default.tempDir,

            Badger.env("ROUTER")?.toAsset() ?: default.router
        )

        fun loadFromProperties(default: Config = Config.default) = Config(
            Badger.property("mlem.port")?.toPort() ?: default.port,
            Badger.property("mlem.host") ?: default.host,

            Badger.property("mlem.assets_dir")?.toPath() ?: default.assetsDir,
            Badger.property("mlem.temp_dir")?.toPath() ?: default.tempDir,

            Badger.property("mlem.router")?.toAsset() ?: default.router
        )

        fun loadFromYaml(toml: String, contextPath: Path? = null) = try {
            val result = Badger.yaml.decodeFromString<Config>(toml)
            if (contextPath != null) {
                result.withContextPath(contextPath)
            } else {
                result
            }
        } catch (e: Exception) {
            throw ConfigException("Failed to parse config YAML", e)
        }

        fun load(): Config = try {
            val configPath = Badger.env("CONFIG_PATH") ?: Badger.property("mlem.config_path") ?: "mlem_config.yml"

            var config = try {
                val path = Path(configPath)
                val parent = path.parent

                if (SystemFileSystem.exists(path)) {
                    val json = SystemFileSystem.source(path).buffered().use { it.readText() }
                    loadFromYaml(json, parent)
                } else {
                    Log.info { "Config file at $configPath not found, using default config" }
                    default
                }
            } catch (e: Exception) {
                throw ConfigException("Failed to load config file", e)
            }

            config = loadFromProperties(config)
            config = loadFromEnv(config)

            Log.info { "Config loaded from $configPath" }
            config
        } catch (e: ConfigException) {
            Log.fatal(e) { e.message ?: "Config failed to load" }
            exitProcess(1)
        }
    }
}