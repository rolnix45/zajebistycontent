package rolnix.zajebistycontent

import org.joml.Vector3f

class DirectionalLight {

    var color: Vector3f = Vector3f()
    var direction: Vector3f = Vector3f()
    var intensity: Float = 1f

    constructor(dLight: DirectionalLight) {
        this(dLight.color, dLight.direction, dLight.intensity)
    }

    constructor(color: Vector3f, direction: Vector3f, intensity: Float) {
        this(color, direction, intensity)
    }

    operator fun invoke(color: Vector3f, direction: Vector3f, intensity: Float) {
        this.color = color
        this.direction = direction
        this.intensity = intensity
    }
}