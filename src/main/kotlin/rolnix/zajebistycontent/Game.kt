package rolnix.zajebistycontent

import mu.KotlinLogging
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWWindowSizeCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.NULL
import java.nio.IntBuffer

class Game : Runnable {
    private val logger = KotlinLogging.logger {}

    private lateinit var grass: Mesh
    private lateinit var stone: Mesh

    private lateinit var chunk: Chunk

    private var cameraInc: Vector3f = Vector3f()

    private var window: Long = 0L

    private val tInterval: Float = 1f / TICKRATE
    private var tAccumulator: Float = 0f

    private var projectionMatrix: Matrix4f? = null

    private var aspectRatio: Float = width.toFloat() / height

    private var lockCursor: Boolean = true

    override fun run() {
        try {
            init()
            loop()
        } catch (e: Exception) {
            logger.error(e) { "Error occured in main loop: $e" }
            glfwSetWindowShouldClose(window, true)
        } finally {
            cleanup()
        }
    }

    private fun cleanup() {
        ShaderProgram.cleanup()
        Hud.cleanup()

        for (obj in chunkGameObjectArrays) {
            obj.mesh.cleanUp()
        }

        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }

    private fun init() {
        GLFWErrorCallback.createPrint(System.err).set()
        if (!glfwInit()) {
            logger.error { "Could not initialize GLFW" }
            throw IllegalStateException("glfw error")
        }

        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

        window = glfwCreateWindow(width, height, "elo", NULL, NULL)

        glfwSetKeyCallback(window, Input.Keyboard)
        glfwSetCursorPosCallback(window, Input.Mouse)
        glfwSetMouseButtonCallback(window, Input.MouseButtons)
        glfwSetCursorEnterCallback(window, Input.CursorEntered)
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)

        try {
            val stack: MemoryStack = stackPush()
            val pWidth: IntBuffer = stack.mallocInt(1)
            val pHeight: IntBuffer = stack.mallocInt(1)

            glfwGetWindowSize(window, pWidth, pHeight)
            val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor())
            if (vidmode != null) {
                glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
                )
            }
        } catch (e: Exception) {
            logger.error(e) { "An error occured: $e" }
            return
        }

        glfwMakeContextCurrent(window)
        glfwShowWindow(window)

        GL.createCapabilities()
        GL11.glClearColor(0.0f, 0.5f, 0.75f, 1.0f)
        GL11.glDepthMask(true)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthFunc(GL11.GL_LEQUAL)
        GL11.glEnable(GL11.GL_STENCIL_TEST)
        GL11.glEnable(GL11.GL_CULL_FACE)
        GL11.glCullFace(GL11.GL_FRONT_AND_BACK)
        GL11.glViewport(0, 0, width, height)

        //GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE)

        glfwSetWindowSizeCallback(window, object : GLFWWindowSizeCallback() {
            override fun invoke(window: Long, w: Int, h: Int) {
                GL11.glViewport(0, 0, w,  h)
                width = w
                height = h
                aspectRatio = width.toFloat() / height
            }
        })

        Hud.init()

        ShaderProgram.createVertexShader(Utils.loadResource("/shaders/basic.vert"))
        ShaderProgram.createFragmentShader(Utils.loadResource("/shaders/basic.frag"))
        ShaderProgram.link()

        var texture = Texture(Utils.ioResourceToByteBuffer("/assets/textures/sraka.png", 1024))
        grass = OBJLoader.loadMesh("/assets/models/cube.obj")
        grass.texture = texture

        texture = Texture(Utils.ioResourceToByteBuffer("/assets/textures/lol.png", 1024))
        stone = OBJLoader.loadMesh("/assets/models/cube.obj")
        stone.texture = texture

        projectionMatrix = Matrix4f().perspective(fov, aspectRatio, ZNear, ZFar)
        ShaderProgram.createUniform("projectionMatrix")
        ShaderProgram.createUniform("modelViewMatrix")
        ShaderProgram.createUniform("textSampler")
        ShaderProgram.createUniform("color")
        ShaderProgram.createUniform("useColor")

        logger.info { "Initialization complete!" }

        createWorld()
    }

    private fun createWorld() {
        chunk = Chunk(grass, stone, 0, 0) //4x4 0, 0
        chunk = Chunk(grass, stone, 1, 0)
        chunk = Chunk(grass, stone, 1, 1)
        chunk = Chunk(grass, stone, 0, 1)

        chunk = Chunk(grass, stone, 2, 0) //4x4 1, 0
        chunk = Chunk(grass, stone, 3, 0)
        chunk = Chunk(grass, stone, 3, 1)
        chunk = Chunk(grass, stone, 2, 1)

        chunk = Chunk(grass, stone, 2, 2) //4x4 1, 1
        chunk = Chunk(grass, stone, 3, 2)
        chunk = Chunk(grass, stone, 3, 3)
        chunk = Chunk(grass, stone, 2, 3)

        chunk = Chunk(grass, stone, 0, 2) //4x4 0, 1
        chunk = Chunk(grass, stone, 1, 2)
        chunk = Chunk(grass, stone, 1, 3)
        chunk = Chunk(grass, stone, 0, 3)
        logger.info { "World creation complete! Objects count: ${chunkGameObjectArrays.size}" }
    }

    private fun loop() {

        while (!glfwWindowShouldClose(window)) {
            val delta = Timer.getDelta()
            tAccumulator += delta.toFloat()

            while (tAccumulator >= tInterval) {
                tick()
                Timer.updateTICK()
                tAccumulator -= tInterval
            }

            render(chunkGameObjectArrays)
            Timer.updateFPS()

            Timer.update()
        }
    }

    private fun render(gameObjects: MutableList<GameObject>) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT or GL11.GL_STENCIL_BUFFER_BIT)

        ShaderProgram.bind()

        val projectionMatrix: Matrix4f =
            Transformations.getProjMatrix(fov, width.toFloat(), height.toFloat(), ZNear, ZFar)
        ShaderProgram.setUniform("projectionMatrix", projectionMatrix)

        val viewMatrix: Matrix4f = Transformations.getViewMatrix(Camera)

        //FrustrumCulling.updateFrustrum(projectionMatrix, viewMatrix)
        //FrustrumCulling.filterFrustrum(chunkGameObjectArrays, 1f)

        ShaderProgram.setUniform("textSampler", 0)
        for (block in gameObjects) {
            val modelViewMatrix: Matrix4f = block.let { Transformations.getModelViewMatrix(it, viewMatrix) }
            modelViewMatrix.let { ShaderProgram.setUniform("modelViewMatrix", it) }

            ShaderProgram.setUniform("color", block.mesh.color)
            ShaderProgram.setUniform("useColour", if (block.mesh.isTextured()) 0 else 1)

            block.mesh.render()
        }

        ShaderProgram.unbind()
        Hud.render()
        glfwSwapBuffers(window)
    }

    private fun tick() {
        glfwPollEvents()

        input()
        Camera.movePosition(cameraInc.x * cameraSpeed,
                            cameraInc.y * cameraSpeed,
                            cameraInc.z * cameraSpeed)
    }

    private fun input() {
        // CAMERA ROTATION
        Camera.mouseControl()
        if (lockCursor) {
            val rotVec: Vector2f = Camera.displVec
            Camera.moveRotation(rotVec.x * mouseSensitivity, rotVec.y * mouseSensitivity, 0f)
        }

        // CURSOR LOCKING
        if (Input.Keyboard.keyStates[GLFW_KEY_TAB]) {
            lockCursor = !lockCursor
            glfwSetInputMode(window, GLFW_CURSOR, if (lockCursor) GLFW_CURSOR_DISABLED else GLFW_CURSOR_NORMAL)
        }

        // CLOSE APP
        if (Input.Keyboard.keyStates[GLFW_KEY_ESCAPE])
            glfwSetWindowShouldClose(window, true)

        // MOUSE SENSITIVITY CHANGE
        if (Input.Keyboard.keyStates[GLFW_KEY_LEFT_BRACKET]) {
            mouseSensitivity -= 0.025f
        } else if (Input.Keyboard.keyStates[GLFW_KEY_RIGHT_BRACKET]) {
            mouseSensitivity += 0.025f
        }

        // CAMERA SPEED CHANGE
        if (Input.Keyboard.keyStates[GLFW_KEY_SEMICOLON]) {
            cameraSpeed -= 0.025f
        } else if (Input.Keyboard.keyStates[GLFW_KEY_APOSTROPHE]) {
            cameraSpeed += 0.025f
        }

        // CAMERA MOVING
        cameraInc.set(0f, 0f, 0f)
        if (Input.Keyboard.keyStates[GLFW_KEY_W]) {
            cameraInc.z = -1f
        } else if (Input.Keyboard.keyStates[GLFW_KEY_S]) {
            cameraInc.z = 1f
        }
        if (Input.Keyboard.keyStates[GLFW_KEY_A]) {
            cameraInc.x = -1f
        } else if (Input.Keyboard.keyStates[GLFW_KEY_D]) {
            cameraInc.x = 1f
        }
        if (Input.Keyboard.keyStates[GLFW_KEY_Z]) {
            cameraInc.y = -1f
        } else if (Input.Keyboard.keyStates[GLFW_KEY_X]) {
            cameraInc.y = 1f
        }

        // CAMERA POSITION RESET
        if (Input.Keyboard.keyStates[GLFW_KEY_R]) {
            Camera.resetPosition()
        }
    }

    companion object {
        var width: Int = 1280
        var height: Int = 720
        private const val TICKRATE: Byte = 60

        var mouseSensitivity: Float = 0.25f
        var cameraSpeed: Float = 0.25f

        val fov: Float = (Math.toRadians(60.0)).toFloat()
        const val ZNear: Float = 0.01f
        const val ZFar: Float = 1000f

        val chunkGameObjectArrays: MutableList<GameObject> = emptyList<GameObject>().toMutableList()

        fun restoreState() {
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glEnable(GL11.GL_STENCIL_TEST)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        }
    }
}