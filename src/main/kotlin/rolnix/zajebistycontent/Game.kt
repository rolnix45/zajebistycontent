package rolnix.zajebistycontent

import mu.KotlinLogging
import org.joml.Math.*
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWWindowSizeCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.NULL
import rolnix.zajebistycontent.PointLight.Attenuation
import rolnix.zajebistycontent.Utils.loadResource
import java.nio.IntBuffer

class Game : Runnable {
    private val logger = KotlinLogging.logger {}

    private lateinit var grass: Mesh
    private lateinit var stone: Mesh
    private lateinit var map: Mesh

    private lateinit var chunk: Chunk

    private var cameraInc: Vector3f = Vector3f()

    private var window: Long = 0L

    private val tInterval: Float = 1f / TICKRATE
    private var tAccumulator: Float = 0f

    private var projectionMatrix: Matrix4f? = null

    private var aspectRatio: Float = width.toFloat() / height

    private var lockCursor: Boolean = true

    private val specularPower: Float = 10f

    private lateinit var sceneShaderProgram: ShaderProgram
    private lateinit var skyboxShaderProgram: ShaderProgram

    private var sceneLight: SceneLight = SceneLight()

    private val scene = Scene()

    override fun run() {
        try {
            init()
            loop()
        } catch (e: Exception) {
            logger.error(e) { "Error in main loop!" }
            glfwSetWindowShouldClose(window, true)
        } finally {
            cleanup()
        }
    }

    private fun cleanup() {
        sceneShaderProgram.cleanup()
        skyboxShaderProgram.cleanup()
        Hud.cleanup()

        for (obj in gameObjectArrays) {
            obj.mesh.cleanUp()
        }

        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }

    private fun init() {
        // GLFW INITIALIZATION
        GLFWErrorCallback.createPrint(System.err).set()
        if (!glfwInit()) {
            logger.error { "Could not initialize GLFW" }
            throw IllegalStateException("glfw error")
        }

        // CREATING AND CENTERING WINDOW
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

        // OPENGL INITIALITION
        GL.createCapabilities()
        GL11.glClearColor(1f, 1f, 1f, 1.0f)
        GL11.glDepthMask(true)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthFunc(GL11.GL_LEQUAL)
        GL11.glEnable(GL11.GL_STENCIL_TEST)

        GL11.glEnable(GL11.GL_CULL_FACE)
        GL11.glCullFace(GL11.GL_BACK)

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

        sceneShaderProgram = ShaderProgram()
        skyboxShaderProgram = ShaderProgram()
        setupObjectShader()
        setupSkyboxShader()

        logger.info { "Initialization complete!" }

        skyboxSetup()
        sceneSetup()
    }

    private fun sceneSetup() {
        val sceneLight = SceneLight()
        scene.sceneLight = sceneLight

        sceneLight.directionalLight = DirectionalLight(
            Vector3f(0.0f, 0.0f, 0.0f),
            Vector3f(-1f, 0f, 0f), 1f)

        sceneLight.ambientLight = Vector3f(1.0f, 1.0f, 1.0f)

        sceneLight.pointLightList = listOf(PointLight(
            Vector3f(1f, 1f, 1f),
            Vector3f(8f, 9f, 8f),
            1f)) as MutableList<PointLight>

        val att = Attenuation(0.0f, 0.0f ,1f)
        sceneLight.pointLightList[0].attenuation = att

        val sAtt = Attenuation(0.0f, 0.0f ,0.02f)

        val lightPosition = Vector3f(0f, 0.0f, 10f)
        val pointLight = PointLight(Vector3f(1f, 1f, 1f), lightPosition, 1f)
        pointLight.attenuation = sAtt
        val coneDir = Vector3f(0f, 0f, -1f)
        val cutoff = kotlin.math.cos(Math.toRadians(140.0)).toFloat()
        val spotLight = SpotLight(pointLight, coneDir, cutoff)
        sceneLight.spotLightList = arrayOf(spotLight, SpotLight(spotLight)).toMutableList()


        var material = Material(Texture(Utils.ioResourceToByteBuffer("/assets/textures/sraka.png", 1024)))
        grass = OBJLoader.loadMesh("/assets/models/cube.obj")
        grass.material = material

        material = Material(Texture(Utils.ioResourceToByteBuffer("/assets/textures/lol.png", 1024)), 1f)
        stone = OBJLoader.loadMesh("/assets/models/cube.obj")
        stone.material = material

        material = Material(Texture(Utils.ioResourceToByteBuffer("/assets/textures/chujwie.png", 1024)))
        map = OBJLoader.loadMesh("/assets/models/mapa.obj")
        map.material = material

        /*
        val mapObject = GameObject()
        mapObject.position = Vector3f(-16f, 0f, -16f)
        mapObject.mesh = map
        gameObjectArrays.add(mapObject)
        */

        val terrainScale = 50f
        val terrainSize = 3
        val minY = -0.01f
        val maxY = 0.01f
        val textInc = 40
        val terrain = Terrain(terrainSize, terrainScale, minY, maxY,
            "/assets/textures/noiseTexture.png",
                "/assets/textures/chujwie2.png", textInc)
        gameObjectArrays = (gameObjectArrays + terrain.gameObjects) as MutableList<GameObject>


        chunk = Chunk(grass, stone, 0, 0)
        chunk = Chunk(grass, stone, 1, 0)
        chunk = Chunk(grass, stone, 1, 1)
        chunk = Chunk(grass, stone, 0, 1)


        scene.setGameObjects(gameObjectArrays.toTypedArray())
        logger.info { "World creation complete! Objects count: ${gameObjectArrays.size}" }
    }

    private fun skyboxSetup() {
        val skybox = Skybox("/assets/models/skybox.obj", "/assets/textures/skybox.png")
        skybox.scale = 500f
        scene.skybox = skybox
    }

    @Throws(Exception::class)
    private fun setupObjectShader() {
        try {
            sceneShaderProgram.createVertexShader(loadResource("/shaders/object.vert"))
            sceneShaderProgram.createFragmentShader(loadResource("/shaders/object.frag"))
            sceneShaderProgram.link()

            projectionMatrix = Matrix4f().perspective(fov, aspectRatio, ZNear, ZFar)
            sceneShaderProgram.createUniform("projectionMatrix")
            sceneShaderProgram.createUniform("modelViewMatrix")
            sceneShaderProgram.createUniform("textSampler")
            sceneShaderProgram.createMaterialUniform("material")
            sceneShaderProgram.createUniform("specularPower")
            sceneShaderProgram.createUniform("ambientLight")
            sceneShaderProgram.createPointLightListUniform("pointLights", MAX_POINT_LIGHTS)
            sceneShaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS)
            sceneShaderProgram.createDirectionalLightUniform("directionalLight")

            logger.info { "Object shader ready!" }
        } catch (e: Exception) {
            logger.error { "Object shaders creation error $e" }
        }
    }

    @Throws(Exception::class)
    private fun setupSkyboxShader() {
        try {
            skyboxShaderProgram.createVertexShader(loadResource("/shaders/skybox.vert"))
            skyboxShaderProgram.createFragmentShader(loadResource("/shaders/skybox.frag"))
            skyboxShaderProgram.link()

            skyboxShaderProgram.createUniform("projectionMatrix")
            skyboxShaderProgram.createUniform("modelViewMatrix")
            skyboxShaderProgram.createUniform("texture_sampler")
            skyboxShaderProgram.createUniform("ambientLight")

            logger.info { "Skybox shader ready!" }
        } catch (e: Exception) {
            logger.error { "Skybox shaders creation error $e" }
        }
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

            render()
            Timer.updateFPS()

            Timer.update()
        }
    }

    private fun render() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT or GL11.GL_STENCIL_BUFFER_BIT)

        Transformations.updateProjectionMatrix(fov, width.toFloat(), height.toFloat(), ZNear, ZFar)
        Transformations.updateViewMatrix(Camera)

        renderScene()
        renderSkybox()

        Hud.render()
        glfwSwapBuffers(window)
    }

    private fun renderScene() {
        GL11.glEnable(GL11.GL_CULL_FACE)
        GL11.glCullFace(GL11.GL_BACK)
        sceneShaderProgram.bind()

        val projectionMatrix: Matrix4f = Transformations.getProjectionMatrix()
        sceneShaderProgram.setUniform("projectionMatrix", projectionMatrix)

        val viewMatrix: Matrix4f = Transformations.getViewMatrix()

        sceneLight = scene.sceneLight
        renderLights(viewMatrix, sceneLight)

        val mapMeshes: Map<Mesh, List<GameObject>> = scene.meshMap
        sceneShaderProgram.setUniform("textSampler", 0)
        for (mesh in mapMeshes.keys) {
            sceneShaderProgram.setUniform("material", mesh.material!!)
            mesh.renderList(
                mapMeshes[mesh]!!
            ) { gameItem: GameObject ->
                val modelViewMatrix: Matrix4f = Transformations.buildModelViewMatrix(gameItem, viewMatrix)
                sceneShaderProgram.setUniform("modelViewMatrix", modelViewMatrix)
            }
        }

        sceneShaderProgram.unbind()
        GL11.glDisable(GL11.GL_CULL_FACE)
    }

    private fun renderLights(vm: Matrix4f, sl: SceneLight) {
        sceneShaderProgram.setUniform("ambientLight", scene.sceneLight.ambientLight)
        sceneShaderProgram.setUniform("specularPower", specularPower)

        // POINT LIGHTS
        val pointLightList = sl.pointLightList
        var numLights = pointLightList.size
        for (i in 0 until numLights) {
            val currPointLight = PointLight(pointLightList[i])
            val lightPos = currPointLight.position
            val aux = Vector4f(lightPos, 1f)
            aux.mul(vm)
            lightPos.x = aux.x
            lightPos.y = aux.y
            lightPos.z = aux.z
            sceneShaderProgram.setUniform("pointLights", currPointLight, i)
        }

        // SPOT LIGHTS
        val spotLightList = sl.spotLightList
        numLights = spotLightList.size
        for (i in 0 until numLights) {
            val currSpotLight = SpotLight(spotLightList[i])
            val dir = Vector4f(currSpotLight.coneDirection, 0f)
            dir.mul(vm)
            currSpotLight.coneDirection = Vector3f(dir.x, dir.y, dir.z)
            val lightPos = currSpotLight.pointLight.position
            val aux = Vector4f(lightPos, 1f)
            aux.mul(vm)
            lightPos.x = aux.x
            lightPos.y = aux.y
            lightPos.z = aux.z
            sceneShaderProgram.setUniform("spotLights", currSpotLight, i)
        }

        // DIRECTIONAL LIGHT
        val currDirLight = DirectionalLight(sl.directionalLight)
        val dir = Vector4f(currDirLight.direction, 0f)
        dir.mul(vm)
        currDirLight.direction = Vector3f(dir.x, dir.y, dir.z)
        sceneShaderProgram.setUniform("directionalLight", currDirLight)
    }

    private fun renderSkybox() {
        skyboxShaderProgram.bind()

        val projectionMatrix: Matrix4f = Transformations.getProjectionMatrix()
        val viewMatrix: Matrix4f = Transformations.getViewMatrix()

        skyboxShaderProgram.setUniform("textSampler", 0)
        skyboxShaderProgram.setUniform("projectionMatrix", projectionMatrix)

        val skybox: Skybox = scene.skybox
        viewMatrix.m30(0f)
        viewMatrix.m31(0f)
        viewMatrix.m32(0f)

        val modelViewMatrix: Matrix4f = Transformations.getModelViewMatrix(skybox, viewMatrix)
        skyboxShaderProgram.setUniform("modelViewMatrix", modelViewMatrix)
        skyboxShaderProgram.setUniform("ambientLight", scene.sceneLight.ambientLight)

        scene.skybox.mesh.render()

        skyboxShaderProgram.unbind()
    }

    private fun tick() {
        glfwPollEvents()

        input()
        Camera.movePosition(cameraInc.x * cameraSpeed,
                            cameraInc.y * cameraSpeed,
                            cameraInc.z * cameraSpeed)

        Camera.updateViewMatrix()
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
        if (Input.Keyboard.keyStates[GLFW_KEY_LEFT_BRACKET] && mouseSensitivity >= 0.025f) {
            mouseSensitivity -= 0.025f
        } else if (Input.Keyboard.keyStates[GLFW_KEY_RIGHT_BRACKET]) {
            mouseSensitivity += 0.025f
        }

        // CAMERA SPEED CHANGE
        if (Input.Keyboard.keyStates[GLFW_KEY_SEMICOLON] && cameraSpeed >= 0.025f) {
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
        var cameraSpeed: Float = 0.025f

        const val fov: Float = 1.39626f
        const val ZNear: Float = 0.01f
        const val ZFar: Float = 1000f

        var gameObjectArrays: MutableList<GameObject> = emptyList<GameObject>().toMutableList()

        const val MAX_POINT_LIGHTS = 4
        const val MAX_SPOT_LIGHTS = 4

        fun restoreState() {
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glEnable(GL11.GL_STENCIL_TEST)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        }
    }
}
