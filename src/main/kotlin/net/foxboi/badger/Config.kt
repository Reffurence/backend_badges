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
import net.foxboi.badger.asset.src.AssetSrc
import net.foxboi.badger.asset.src.EmptyAssetSrc
import net.foxboi.badger.asset.src.LocalAssetSrc
import net.foxboi.badger.asset.src.MinioAssetSrc
import net.foxboi.badger.util.PathSerializer
import kotlin.system.exitProcess

/**
 * Badger's configuration instance.
 *
 * @param port        The port to open a server at.
 * @param host        The server host.
 * @param assetSource The assets directory, where local assets are loaded from.
 * @param tempDir     The temporary directory, where temporary files are stored.
 * @param router      The router asset. When unspecified, the server will not serve anything.
 */
@Serializable
data class Config(
    val port: Int = 80,
    val host: String = "0.0.0.0",

    @SerialName("asset_source")
    val assetSource: AssetSrcConfig = EmptyAssetSrcConfig,

    @SerialName("temp_dir")
    val tempDir: Path = Path(SystemTemporaryDirectory, "mlem"),

    val router: Asset? = null
) {
    constructor(
        port: Int = 80,
        host: String = "0.0.0.0",

        assetSourceType: AssetSrcType = AssetSrcType.EMPTY,
        assetsDir: Path?,
        minioEndpoint: String? = null,
        minioBucket: String? = null,
        minioAccessKey: String? = null,
        minioSecretKey: String? = null,
        minioNoCredentials: Boolean = false,
        minioAssetPrefix: String = "",

        tempDir: Path = Path(SystemTemporaryDirectory, "mlem"),

        router: Asset? = null
    ) : this(
        port,
        host,

        when (assetSourceType) {
            AssetSrcType.EMPTY -> EmptyAssetSrcConfig

            AssetSrcType.LOCAL -> LocalAssetSrcConfig(
                assetsDir ?: Path("./assets")
            )

            AssetSrcType.MINIO -> MinioAssetSrcConfig(
                minioEndpoint,
                minioAccessKey,
                minioSecretKey,
                minioBucket,
                minioNoCredentials,
                minioAssetPrefix
            )
        },

        tempDir,
        router
    )

    val assetSourceType get() = assetSource.type
    val assetsDir get() = (assetSource as? LocalAssetSrcConfig)?.path
    val minioEndpoint get() = (assetSource as? MinioAssetSrcConfig)?.endpoint
    val minioBucket get() = (assetSource as? MinioAssetSrcConfig)?.bucket
    val minioAccessKey get() = (assetSource as? MinioAssetSrcConfig)?.accessKey
    val minioSecretKey get() = (assetSource as? MinioAssetSrcConfig)?.secretKey
    val minioNoCredentials get() = (assetSource as? MinioAssetSrcConfig)?.noCredentials ?: false
    val minioAssetPrefix get() = (assetSource as? MinioAssetSrcConfig)?.assetPrefix ?: ""

    init {
        if (port !in 0..0xFFFF)
            throw ConfigException("Invalid port $port")
    }

    private fun withContextPath(context: Path) = copy(
        assetSource = resolveParent(context, assetSource),
        tempDir = resolveParent(context, tempDir)
    )

    private fun resolveParent(parent: Path, path: Path): Path {
        if (path.isAbsolute) return path // Don't change absolute paths
        return Path(parent, "$path")
    }

    private fun resolveParent(parent: Path, config: AssetSrcConfig): AssetSrcConfig {
        return when (config) {
            is LocalAssetSrcConfig -> if (config.path != null) {
                LocalAssetSrcConfig(resolveParent(parent, config.path))
            } else {
                config
            }

            else -> config
        }
    }

    companion object {
        val default = Config()

        private fun String.toPort() = toIntOrNull() ?: throw ConfigException("Not a port number: $this")
        private fun String.toPath() = kotlinx.io.files.Path(this)
        private fun String.toAsset() = Asset.fromStringOrNull(this) ?: throw ConfigException("Invalid asset: $this")
        private fun String.toAssetSrcType() = when (this) {
            "empty" -> AssetSrcType.EMPTY
            "local" -> AssetSrcType.LOCAL
            "minio" -> AssetSrcType.MINIO
            else -> throw ConfigException("Unknown asset source type: $this")
        }

        private fun String.toBoolean() = when (this) {
            "true" -> true
            "false" -> false
            else -> throw ConfigException("Not a boolean: $this")
        }

        fun loadFromEnv(default: Config = Config.default) = Config(
            Badger.env("PORT")?.toPort() ?: default.port,
            Badger.env("HOST") ?: default.host,

            Badger.env("ASSET_SOURCE")?.toAssetSrcType() ?: default.assetSourceType,
            Badger.env("ASSETS_DIR")?.toPath() ?: default.assetsDir,
            Badger.env("MINIO_ENDPOINT") ?: default.minioEndpoint,
            Badger.env("MINIO_BUCKET") ?: default.minioBucket,
            Badger.env("MINIO_ACCESS_KEY") ?: default.minioAccessKey,
            Badger.env("MINIO_SECRET_KEY") ?: default.minioSecretKey,
            Badger.env("MINIO_NO_CREDENTIALS")?.toBoolean() ?: default.minioNoCredentials,
            Badger.env("MINIO_ASSET_PREFIX") ?: default.minioAssetPrefix,

            Badger.env("TEMP_DIR")?.toPath() ?: default.tempDir,

            Badger.env("ROUTER")?.toAsset() ?: default.router
        )

        fun loadFromProperties(default: Config = Config.default) = Config(
            Badger.property("net.foxboi.badger.port")?.toPort() ?: default.port,
            Badger.property("net.foxboi.badger.host") ?: default.host,

            Badger.property("net.foxboi.badger.asset_source")?.toAssetSrcType() ?: default.assetSourceType,
            Badger.property("net.foxboi.badger.assets_dir")?.toPath() ?: default.assetsDir,
            Badger.property("net.foxboi.badger.minio_endpoint") ?: default.minioEndpoint,
            Badger.property("net.foxboi.badger.minio_bucket") ?: default.minioBucket,
            Badger.property("net.foxboi.badger.minio_access_key") ?: default.minioAccessKey,
            Badger.property("net.foxboi.badger.minio_secret_key") ?: default.minioSecretKey,
            Badger.property("net.foxboi.badger.minio_no_credentials")?.toBoolean() ?: default.minioNoCredentials,
            Badger.property("net.foxboi.badger.minio_asset_prefix") ?: default.minioAssetPrefix,

            Badger.property("net.foxboi.badger.temp_dir")?.toPath() ?: default.tempDir,

            Badger.property("net.foxboi.badger.router")?.toAsset() ?: default.router
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
            val configPath =
                Badger.env("CONFIG_PATH") ?: Badger.property("net.foxboi.badger.config_path") ?: "badger.yml"

            var config = try {
                val path = SystemFileSystem.resolve(Path(configPath))
                val parent = path.parent

                if (SystemFileSystem.exists(path)) {
                    val json = SystemFileSystem.source(path).buffered().use { it.readText() }
                    loadFromYaml(json, parent)
                } else {
                    Log.info { "Config file at '$path' not found, using default config" }
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


enum class AssetSrcType {
    @SerialName("empty")
    EMPTY,

    @SerialName("local")
    LOCAL,

    @SerialName("minio")
    MINIO,
}

@Serializable
sealed interface AssetSrcConfig {
    val type: AssetSrcType
    fun open(): AssetSrc
}

@Serializable
@SerialName("empty")
object EmptyAssetSrcConfig : AssetSrcConfig {
    override val type = AssetSrcType.EMPTY

    override fun open(): EmptyAssetSrc {
        Log.info { "Asset source was not specified, no local assets are present" }

        return EmptyAssetSrc
    }
}

@Serializable
@SerialName("local")
class LocalAssetSrcConfig(
    @SerialName("directory")
    val path: Path?
) : AssetSrcConfig {
    override val type = AssetSrcType.LOCAL

    override fun open() = if (path != null) {
        val glob = SystemFileSystem.resolve(path)
        Log.info { "Loading assets from '$glob'" }

        LocalAssetSrc(glob)
    } else {
        Log.error { "Assets directory was not defined in config, asset source will be empty." }
        Log.error { "To set an assets directory, provide 'directory' under 'asset_source' or set the 'ASSETS_DIR' environment variable to the desired directory." }
        EmptyAssetSrc
    }
}

@Serializable
@SerialName("minio")
class MinioAssetSrcConfig(
    val endpoint: String? = null,

    @SerialName("access_key")
    val accessKey: String? = null,

    @SerialName("secret_key")
    val secretKey: String? = null,

    val bucket: String? = null,

    @SerialName("no_credentials")
    val noCredentials: Boolean = false,

    @SerialName("asset_prefix")
    val assetPrefix: String = "",
) : AssetSrcConfig {
    override val type = AssetSrcType.MINIO

    override fun open(): AssetSrc {
        return if (bucket != null && endpoint != null) {
            Log.info { "Connecting to MinIO endpoint '$endpoint', bucket '$bucket'" }

            if (accessKey != null && secretKey != null && !noCredentials) {
                MinioAssetSrc.connect(endpoint, bucket, accessKey, secretKey, assetPrefix)
            } else {
                if (!noCredentials) {
                    Log.warn { "Minio credentials have not been defined in config, attempting access without credentials." }
                    Log.warn { "To set credentials, provide 'access_key' and 'secret_key' under 'asset_source' in config or specify the 'MINIO_ACCESS_KEY' and 'MINIO_SECRET_KEY' environment variables." }
                    Log.warn { "To disable this warning, set 'no_credentials' under 'asset_source' in config or the 'MINIO_NO_CREDENTIALS' environment variable to 'true'." }
                }

                MinioAssetSrc.connect(endpoint, bucket, assetPrefix)
            }
        } else {
            Log.error { "Minio endpoint and bucket have not been defined in config, asset source will be empty." }
            Log.error { "To set an endpoint and bucket, provide 'endpoint' and 'bucket' under 'asset_source' in config or specify the 'MINIO_ENDPOINT' and 'MINIO_BUCKET' environment variables." }
            EmptyAssetSrc
        }
    }
}