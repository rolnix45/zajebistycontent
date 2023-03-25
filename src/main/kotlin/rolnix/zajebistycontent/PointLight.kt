package rolnix.zajebistycontent

import org.joml.Vector3f

class PointLight {
    lateinit var color: Vector3f
    lateinit var position: Vector3f

    var intensity: Float = 0f

    lateinit var attenuation: Attenuation

    constructor(color: Vector3f, position: Vector3f, intensity: Float) {
        attenuation = Attenuation(1f, 0f, 0f)
        this.color = color
        this.position = position
        this.intensity = intensity
    }

    constructor(pointLight: PointLight) {
        this(
            Vector3f(pointLight.color),
            Vector3f(pointLight.position),
            pointLight.intensity,
            pointLight.attenuation
        )
    }

    private operator fun invoke(color: Vector3f, position: Vector3f, intensity: Float, attenuation: Attenuation) {
        this.color = color
        this.position = position
        this.intensity = intensity
        this.attenuation = attenuation
    }

    class Attenuation(constant: Float, linear: Float, exponent: Float) {
        var constant = 0f
        var linear = 0f
        var exponent = 0f

        init {
            this.constant = constant
            this.linear = linear
            this.exponent = exponent
        }
    }
}