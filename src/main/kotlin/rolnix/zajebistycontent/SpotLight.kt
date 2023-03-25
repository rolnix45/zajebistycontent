package rolnix.zajebistycontent

import org.joml.Vector3f
import org.joml.Math.cos
import org.joml.Math.toRadians

class SpotLight {
    var pointLight: PointLight

    var coneDirection = Vector3f()

    var cutOff: Float = 0F

    constructor(pointLight: PointLight, coneDirection: Vector3f, cutOffAngle: Float) {
        this.pointLight = pointLight
        this.coneDirection = coneDirection
        setCutOffAngle(cutOffAngle)
    }

    constructor(spotLight: SpotLight) {
        this.pointLight = spotLight.pointLight
        this.coneDirection = spotLight.coneDirection
        this.cutOff = spotLight.cutOff
    }

    private fun setCutOffAngle(cutOffAngle: Float) {
        this.cutOff = cos(toRadians(cutOffAngle))
    }
}