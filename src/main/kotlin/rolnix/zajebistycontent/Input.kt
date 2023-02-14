package rolnix.zajebistycontent

import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.GLFW_RELEASE
import org.lwjgl.glfw.GLFWCursorEnterCallback
import org.lwjgl.glfw.GLFWCursorPosCallback
import org.lwjgl.glfw.GLFWKeyCallback
import org.lwjgl.glfw.GLFWMouseButtonCallback

object Input {
    object CursorEntered: GLFWCursorEnterCallback() {
        var isOnWindow: Boolean = false

        override fun invoke(window: Long, entered: Boolean) {
            isOnWindow = entered
        }
    }

    object MouseButtons: GLFWMouseButtonCallback() {
        val mouseButtons: Array<Boolean> = Array(255) { false }

        override fun invoke(window: Long, button: Int, action: Int, mods: Int) {
            mouseButtons[button] = action != GLFW_RELEASE
        }

    }

    object Mouse: GLFWCursorPosCallback() {
        var posX: Double = 0.0
        var posY: Double = 0.0

        override fun invoke(window: Long, xpos: Double, ypos: Double) {
            posX = xpos
            posY = ypos
        }
    }

    object Keyboard: GLFWKeyCallback() {
        val keyStates: Array<Boolean> = Array(65535) { false }

        override fun invoke(window: Long, key: Int, scancode: Int, action: Int, mods: Int) {
            if (key != GLFW.GLFW_KEY_UNKNOWN)
                keyStates[key] = action != GLFW_RELEASE
        }
    }
}