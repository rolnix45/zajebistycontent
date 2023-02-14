package rolnix.zajebistycontent

import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f

object FrustrumCulling {
    private const val planeNumber: Int = 6

    private val prjViewMatrix: Matrix4f = Matrix4f()
    private val frustrumPlanes: MutableList<Vector4f> = MutableList(planeNumber) { Vector4f() }

    fun updateFrustrum(projMatrix: Matrix4f, viewMatrix: Matrix4f) {
        prjViewMatrix.set(projMatrix)
        prjViewMatrix.mul(viewMatrix)

        for (i in 0 until planeNumber) {
            prjViewMatrix.frustumPlane(i, frustrumPlanes[i])
        }
    }

    private fun isInsideFrustrum(x0: Float, y0: Float, z0: Float, boundingRadius: Float): Boolean {
        for (i in 0 until planeNumber) {
            val plane: Vector4f = frustrumPlanes[i]
            if (plane.x * x0 + plane.y * y0 + plane.z * z0 + plane.w <= -boundingRadius) {
                return false
            }
        }
        return true
    }

    fun filterFrustrum(gameObjectList: List<GameObject>, meshBoundingRadius: Float) {
        var boundingRadius: Float
        var pos: Vector3f

        for (block in gameObjectList) {
            boundingRadius = block.scale * meshBoundingRadius
            pos = block.position
            block.insideFrustrum = isInsideFrustrum(pos.x, pos.y, pos.z, boundingRadius)
        }
    }
}