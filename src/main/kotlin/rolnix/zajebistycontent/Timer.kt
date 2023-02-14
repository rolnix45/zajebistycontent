package rolnix.zajebistycontent

import org.lwjgl.glfw.GLFW.glfwGetTime

object Timer {
    private var lastLoopTime: Double = 0.0
    private var timeCount: Float = 0.0f

    private var fps: Int = 0
    private var fpsCount: Int = 0

    private var ticks: Int = 0
    private var tickCount: Int = 0

    init {
        lastLoopTime = glfwGetTime()
    }

    fun getDelta(): Double {
        val time = glfwGetTime()
        val delta = time - lastLoopTime
        lastLoopTime = time
        timeCount += delta.toFloat()
        return delta
    }

    fun update() {
        if (timeCount > 1f) {
            fps = fpsCount
            fpsCount = 0

            ticks = tickCount
            tickCount = 0

            timeCount -= 1f
        }
    }

    fun getFPS(): Int {
        return if (fps > 0) fps else fpsCount
    }

    fun getTICK(): Int {
        return if (ticks > 0) ticks else tickCount
    }

    fun updateFPS() {
        fpsCount++
    }

    fun updateTICK() {
        tickCount++
    }
}