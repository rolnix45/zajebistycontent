package rolnix.zajebistycontent

import org.joml.Matrix4f
import org.joml.Vector3f

object Transformations {
    private val projectionMatrix: Matrix4f = Matrix4f()
    private val modelViewMatrix: Matrix4f = Matrix4f()
    private val viewMatrix: Matrix4f = Matrix4f()

    fun getProjMatrix(fov: Float, width: Float, height: Float, znear: Float, zfar: Float): Matrix4f {
        return projectionMatrix.setPerspective(fov, width / height, znear, zfar)
    }

    fun getViewMatrix(camera: Camera): Matrix4f {
        val cameraPos: Vector3f = camera.position
        val cameraRot: Vector3f = camera.rotation

        viewMatrix.identity()
        viewMatrix.rotate(org.joml.Math.toRadians(cameraRot.x), Vector3f(1f, 0f, 0f))
            .rotate(org.joml.Math.toRadians(cameraRot.y), Vector3f(0f, 1f, 0f))
        viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z)
        return viewMatrix
    }

    fun getModelViewMatrix(gameObject: GameObject, viewMatrix: Matrix4f): Matrix4f {
        val rotation: Vector3f = gameObject.rotation

        modelViewMatrix.identity().translate(gameObject.position).rotateX(org.joml.Math.toRadians(-rotation.x)).rotateY(
            org.joml.Math.toRadians(-rotation.y)
        ).rotateZ(org.joml.Math.toRadians(-rotation.z)).scale(gameObject.scale)
        val viewCurr = Matrix4f(viewMatrix)
        return viewCurr.mul(modelViewMatrix)
    }
}