package net.foxboi.badger.asset.src

import io.minio.BucketExistsArgs
import io.minio.GetObjectArgs
import io.minio.MinioClient
import io.minio.StatObjectArgs
import io.minio.errors.ErrorResponseException
import kotlinx.io.Source
import kotlinx.io.asSource
import kotlinx.io.buffered

class MinioAssetSrc private constructor(
    val client: MinioClient,
    val bucket: String,
    val assetPrefix: String
) : AssetSrc {
    override fun exists(path: String): Boolean {
        return try {
            client.statObject(
                StatObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(assetPrefix + path)
                    .build()
            )
            true
        } catch (_: ErrorResponseException) {
            false
        } catch (e: Exception) {
            throw AssetSrcException("Failed STAT", e)
        }
    }

    override fun read(path: String): Source {
        return try {
            val response = client.getObject(
                GetObjectArgs.builder()
                    .bucket(bucket)
                    .`object`(assetPrefix + path)
                    .build()
            )

            response.asSource().buffered()
        } catch (e: Exception) {
            throw AssetSrcException("Failed READ", e)
        }
    }

    override fun close() {
        client.close()
    }

    companion object {
        private fun connect(
            client: MinioClient,
            url: String,
            bucket: String,
            assetPrefix: String
        ): MinioAssetSrc {
            val exists = client.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucket)
                    .build()
            )

            if (!exists) {
                throw AssetSrcException("Cannot connect to bucket $bucket at $url, as the bucket does not exist")
            }

            return MinioAssetSrc(client, bucket, assetPrefix)
        }

        fun connect(
            url: String,
            bucket: String,
            assetPrefix: String = ""
        ): MinioAssetSrc {
            val client = MinioClient.builder()
                .endpoint(url)
                .build()

            return connect(client, url, bucket, assetPrefix)
        }

        fun connect(
            url: String,
            bucket: String,
            accessKey: String,
            secretKey: String,
            assetPrefix: String = ""
        ): MinioAssetSrc {
            val client = MinioClient.builder()
                .endpoint(url)
                .credentials(accessKey, secretKey)
                .build()

            return connect(client, url, bucket, assetPrefix)
        }
    }
}