package rolnix.zajebistycontent

import org.joml.Math.toRadians
import org.joml.Matrix4f
import org.joml.Vector3f

object Transformations {
    private val projectionMatrix: Matrix4f = Matrix4f()
    private val modelViewMatrix: Matrix4f = Matrix4f()
    private val viewMatrix: Matrix4f = Matrix4f()
    private val modelMatrix: Matrix4f = Matrix4f()

    fun updateProjectionMatrix(fov: Float, width: Float, height: Float, zNear: Float, zFar: Float): Matrix4f {
        projectionMatrix.identity()
        return projectionMatrix.setPerspective(fov, width / height, zNear, zFar)
    }

    fun updateGenericViewMatrix(position: Vector3f, rotation: Vector3f, matrix: Matrix4f): Matrix4f {
        return matrix.rotationX(toRadians(rotation.x))
                     .rotateY(toRadians(rotation.y))
                     .translate(-position.x, -position.y, -position.z)
    }

    fun updateViewMatrix(camera: Camera): Matrix4f {
        val cameraPos = camera.position
        val rotation = camera.rotation
        viewMatrix.identity()
        // First do the rotation so camera rotates over its position
        viewMatrix.rotate(Math.toRadians(rotation.x.toDouble()).toFloat(), Vector3f(1f, 0f, 0f))
            .rotate(Math.toRadians(rotation.y.toDouble()).toFloat(), Vector3f(0f, 1f, 0f))
        // Then do the translation
        viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z)
        return viewMatrix
    }

    fun getProjectionMatrix(): Matrix4f {
        return projectionMatrix
    }

    fun getViewMatrix(): Matrix4f {
        return viewMatrix
    }

    fun buildModelViewMatrix(gameItem: GameObject, viewMatrix: Matrix4f): Matrix4f {
        val rotation: Vector3f = gameItem.rotation
        modelMatrix.identity().translate(gameItem.position)
            .rotateX(Math.toRadians(-rotation.x.toDouble()).toFloat()).rotateY(
                Math.toRadians(-rotation.y.toDouble()).toFloat()
            ).rotateZ(Math.toRadians(-rotation.z.toDouble()).toFloat()).scale(gameItem.scale)
        modelViewMatrix.set(viewMatrix)
        return modelViewMatrix.mul(modelMatrix)
    }

    fun getModelViewMatrix(gameObject: GameObject, viewMatrix: Matrix4f): Matrix4f {
        val rotation: Vector3f = gameObject.rotation

        modelViewMatrix.identity().translate(gameObject.position).rotateX(toRadians(-rotation.x)).rotateY(
            toRadians(-rotation.y)
        ).rotateZ(toRadians(-rotation.z)).scale(gameObject.scale)
        val viewCurr = Matrix4f(viewMatrix)
        return viewCurr.mul(modelViewMatrix)
    }
}