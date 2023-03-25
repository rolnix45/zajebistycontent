package rolnix.zajebistycontent

import org.joml.Vector3f

class SceneLight {
    var ambientLight: Vector3f = Vector3f()

    var pointLightList: MutableList<PointLight> = emptyList<PointLight>().toMutableList()

    var spotLightList: MutableList<SpotLight> = emptyList<SpotLight>().toMutableList()

    lateinit var directionalLight: DirectionalLight
}