package net.foxboi.badger.serial.template

import com.charleskorn.kaml.YamlNode
import kotlinx.serialization.Serializable
import net.foxboi.badger.model.Template
import net.foxboi.badger.serial.Serial
import net.foxboi.badger.serial.SerialExpr

@Serializable
class SerialTemplate(
    val size: YamlNode,
    val layers: List<SerialLayer<*>>,
    val vars: Map<String, SerialExpr> = mapOf()
) : Serial<Template> {
    override fun instantiate(): Template {
        val t = Template(metricsFromYaml(size))
        for (layer in layers) {
            t.addLayer(layer.instantiate())
        }
        for ((key, expr) in vars) {
            t.set(key, expr.instantiate())
        }
        return t
    }
}