package net.foxboi.badger.model

import io.ktor.server.plugins.*
import kotlinx.serialization.decodeFromString
import net.foxboi.badger.Badger
import net.foxboi.badger.asset.Asset
import net.foxboi.badger.asset.AssetManager
import net.foxboi.badger.expr.Expr
import net.foxboi.badger.expr.Value
import net.foxboi.badger.model.dyn.Dyn
import net.foxboi.badger.model.dyn.LocalScope
import net.foxboi.badger.model.dyn.Scope
import net.foxboi.badger.route.QueryParam
import net.foxboi.badger.serial.SerialDyn
import net.foxboi.badger.serial.batch.SerialBatch
import net.foxboi.badger.serial.bulk.SerialEntry

data class BulkOption(
    val name: String,
    val batch: Dyn<Asset>,
    val desc: String? = null
) {
    val params = mutableMapOf<String, QueryParam>()
    val scope = LocalScope()

    suspend fun batch(scope: Scope, assets: AssetManager): Batch {
        val batchAsset = batch via scope
        val batchText = assets.text(batchAsset)
        val serial = Badger.yaml.decodeFromString<SerialBatch>(batchText)
        return serial.instantiate()
    }

    fun param(name: String, param: QueryParam) {
        params[name] = param
    }

    fun set(name: String, value: Value<*>) {
        scope.set(name, value)
    }

    fun set(name: String, value: Expr) {
        scope.set(name, value)
    }

    fun unset(name: String) {
        scope.unset(name)
    }

    fun instantiateEntry(index: Int, entry: SerialEntry): BulkEntry {
        val q = entry.params
        val scope = LocalScope()

        for ((name, param) in params) {
            val qValue = q[name]

            val serial = if (qValue == null) {
                param.fallback
                    ?: throw BadRequestException("Missing required input parameter '$name' on bulk element index $index")
            } else {
                SerialDyn(qValue.content)
            }

            val expr = try {
                serial.instantiateAsExpr(param.type)
            } catch (e: Exception) {
                throw BadRequestException("Malformed input parameter '$name'", e)
            }

            scope.set(name, expr)
        }

        return BulkEntry(this, scope)
    }

    fun writeHelp(): String {
        return buildString {
            if (desc != null) {
                append("# $desc\n")
            }
            append(name)
            if (params.isNotEmpty()) {
                append(':')
            }
            append('\n')
            for ((name, param) in params) {
                append(" - ${param.writeHelpLine(name)}\n")
            }
            append('\n')
        }
    }
}