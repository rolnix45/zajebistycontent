package rolnix.zajebistycontent

import org.joml.Vector3f

open class GameObject {
    lateinit var mesh: Mesh
    var position: Vector3f = Vector3f()
    var scale: Float = 1f
    var rotation: Vector3f = Vector3f(0f, 0f, 0f)
}