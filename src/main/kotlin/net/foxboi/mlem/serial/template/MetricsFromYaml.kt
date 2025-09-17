package net.foxboi.mlem.serial.template

import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlScalar
import kotlinx.serialization.Serializable
import net.foxboi.mlem.Mlem
import net.foxboi.mlem.model.Metrics
import net.foxboi.mlem.serial.SerialException

fun metricsFromYaml(yaml: YamlNode) = when (yaml) {
    is YamlList -> metricsFromArray(yaml)
    is YamlMap -> metricsFromObject(yaml)
    else -> throw SerialException("size: must be array or object")
}

private fun metricsFromArray(array: YamlList): Metrics {
    val values = Mlem.yaml.decodeFromYamlNode<List<Int>>(array)
    if (values.size != 2) {
        throw SerialException("size: must be two numbers: [width, height]")
    }

    return Metrics.digital(values[0], values[1])
}

private fun YamlMap.hasKeys(vararg keys: String) = keys.all {
    getKey(it) != null
}

private fun metricsFromObject(obj: YamlMap) = when {
    obj.hasKeys("px", "dpi", "in") -> throw SerialException("size: ambiguous size format")
    obj.hasKeys("px", "dpi") -> metricsFromObjectPxDpi(obj)
    obj.hasKeys("in", "dpi") -> metricsFromObjectInchDpi(obj)
    obj.hasKeys("px", "in") -> metricsFromObjectPxInch(obj)
    obj.hasKeys("px") -> metricsFromObjectPx(obj)
    else -> throw SerialException("size: invalid size format")
}

private fun metricsFromObjectPxDpi(obj: YamlMap) = when (obj["dpi"] as YamlNode?) {
    is YamlScalar -> metricsFromObjectPxSingleDpi(obj)
    is YamlList -> metricsFromObjectPxDoubleDpi(obj)
    else -> throw SerialException("size: 'dpi' must either be a number or an array of 2 numbers")
}

private fun metricsFromObjectInchDpi(obj: YamlMap) = when (obj["dpi"] as YamlNode?) {
    is YamlScalar -> metricsFromObjectInchSingleDpi(obj)
    is YamlList -> metricsFromObjectInchDoubleDpi(obj)
    else -> throw SerialException("size: 'dpi' must either be a number or an array of 2 numbers")
}

private fun metricsFromObjectPxSingleDpi(array: YamlMap): Metrics {
    @Serializable
    class Model(
        val px: List<Int>,
        val dpi: Double
    )

    val model = Mlem.yaml.decodeFromYamlNode<Model>(array)
    if (model.px.size != 2) {
        throw SerialException("size: 'px' array must be two numbers: [width, height]")
    }

    return Metrics.fromPxDpi(model.px[0], model.px[1], model.dpi)
}

private fun metricsFromObjectPxDoubleDpi(array: YamlMap): Metrics {
    @Serializable
    class Model(
        val px: List<Int>,
        val dpi: List<Double>
    )

    val model = Mlem.yaml.decodeFromYamlNode<Model>(array)
    if (model.px.size != 2) {
        throw SerialException("size: 'px' array must be two numbers: [width, height]")
    }
    if (model.dpi.size != 2) {
        throw SerialException("size: 'dpi' array must be two numbers: [x, y]")
    }

    return Metrics.fromPxDpi(model.px[0], model.px[1], model.dpi[0], model.dpi[1])
}

private fun metricsFromObjectInchSingleDpi(array: YamlMap): Metrics {
    @Serializable
    class Model(
        val `in`: List<Double>,
        val dpi: Double
    )

    val model = Mlem.yaml.decodeFromYamlNode<Model>(array)
    if (model.`in`.size != 2) {
        throw SerialException("size: 'in' array must be two numbers: [width, height]")
    }

    return Metrics.fromInDpi(model.`in`[0], model.`in`[1], model.dpi)
}

private fun metricsFromObjectInchDoubleDpi(array: YamlMap): Metrics {
    @Serializable
    class Model(
        val `in`: List<Double>,
        val dpi: List<Double>
    )

    val model = Mlem.yaml.decodeFromYamlNode<Model>(array)
    if (model.`in`.size != 2) {
        throw SerialException("size: 'in' array must be two numbers: [width, height]")
    }
    if (model.dpi.size != 2) {
        throw SerialException("size: 'dpi' array must be two numbers: [x, y]")
    }

    return Metrics.fromInDpi(model.`in`[0], model.`in`[1], model.dpi[0], model.dpi[1])
}

private fun metricsFromObjectPxInch(array: YamlMap): Metrics {
    @Serializable
    class Model(
        val px: List<Int>,
        val `in`: List<Double>
    )

    val model = Mlem.yaml.decodeFromYamlNode<Model>(array)
    if (model.px.size != 2) {
        throw SerialException("size: 'px' array must be two numbers: [width, height]")
    }
    if (model.`in`.size != 2) {
        throw SerialException("size: 'in' array must be two numbers: [width, height]")
    }

    return Metrics.fromPxIn(model.px[0], model.px[1], model.`in`[0], model.`in`[1])
}

private fun metricsFromObjectPx(array: YamlMap): Metrics {
    @Serializable
    class Model(
        val px: List<Int>
    )

    val model = Mlem.yaml.decodeFromYamlNode<Model>(array)
    if (model.px.size != 2) {
        throw SerialException("size: 'px' array must be two numbers: [width, height]")
    }

    return Metrics.digital(model.px[0], model.px[1])
}