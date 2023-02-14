package rolnix.zajebistycontent

import org.joml.Math
import org.joml.Vector2d
import org.joml.Vector2f
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

object Camera {
    private var previousPos: Vector2d = Vector2d(-1.0, -1.0)
    private var currentPos: Vector2d = Vector2d(0.0, 0.0)

    var displVec: Vector2f = Vector2f()

    private var inWindow: Boolean = false

    var position: Vector3f = Vector3f()
    var rotation: Vector3f = Vector3f()

    fun setPosition(x: Float, y: Float, z: Float) {
        position.x = x
        position.y = y
        position.z = z
    }

    init {
        resetPosition()
    }

    fun resetPosition() {
        position.x = Random.nextInt(0, 16).toFloat()
        position.y = 9f
        position.z = Random.nextInt(0, 16).toFloat()

        rotation.set(0f, 0f, 0f)
    }

    fun movePosition(offsetX: Float, offsetY: Float, offsetZ: Float) {
        if (offsetZ != 0f) {
            position.x += (sin(Math.toRadians(rotation.y)) * -1f * offsetZ)
            position.z += (cos(Math.toRadians(rotation.y)) * offsetZ)
        }
        if (offsetX != 0f) {
            position.x += (sin(Math.toRadians(rotation.y - 90f)) * -1f * offsetX)
            position.z += (cos(Math.toRadians(rotation.y - 90f)) * offsetX)
        }
        position.y += offsetY
    }

    fun setRotation(x: Float, y: Float, z: Float) {
        rotation.x = x
        rotation.y = y
        rotation.z = z
    }

    fun moveRotation(offsetX: Float, offsetY: Float, offsetZ: Float) {
        rotation.x += offsetX
        rotation.y += offsetY
        rotation.z += offsetZ
    }

    fun mouseControl() {
        currentPos = Vector2d(Input.Mouse.posX, Input.Mouse.posY)
        inWindow = Input.CursorEntered.isOnWindow

        displVec.x = 0f
        displVec.y = 0f

        if (previousPos.x > 0 && previousPos.y > 0 && inWindow) {
            val deltax = currentPos.x - previousPos.x
            val deltay = currentPos.y - previousPos.y
            val rotateX = deltax != 0.0
            val rotateY = deltay != 0.0
            if (rotateX) {
                displVec.y = deltax.toFloat()
            }
            if (rotateY) {
                displVec.x = deltay.toFloat()
            }
        }
        previousPos.x = currentPos.x
        previousPos.y = currentPos.y
    }
}