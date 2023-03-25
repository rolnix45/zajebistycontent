package rolnix.zajebistycontent

import org.joml.Vector4f

class Material {
    private val defaultColor = Vector4f(1.0f, 1.0f, 1.0f, 1.0f)

    var ambientColor: Vector4f = defaultColor
    var diffuseColor: Vector4f = defaultColor
    var specularColor: Vector4f = defaultColor

    var reflectance: Float = 0f

    var texture: Texture? = null

    constructor(texture: Texture) {
        this(defaultColor, defaultColor, defaultColor, texture, 0f)
    }

    constructor(color: Vector4f, reflectance: Float) {
        this(color, color, color, null, reflectance)
    }

    constructor(texture: Texture, reflectance: Float) {
        this(defaultColor, defaultColor, defaultColor, texture, reflectance)
    }

    private operator fun invoke(ambientColor: Vector4f, diffuseColor: Vector4f, specularColor: Vector4f, texture: Texture?, reflectance: Float) {
        this.ambientColor = ambientColor
        this.diffuseColor = diffuseColor
        this.specularColor = specularColor
        this.texture = texture
        this.reflectance = reflectance
    }

    fun isTextured(): Boolean {
        return texture != null
    }
}