package rolnix.zajebistycontent

import mu.KotlinLogging
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryStack
import rolnix.zajebistycontent.PointLight.Attenuation
import java.nio.FloatBuffer


class ShaderProgram {
    private val logger = KotlinLogging.logger {}

    private var programId: Int = 0

    private var vertexShaderId: Int = 0
    private var fragmentShaderId: Int = 0

    private var uniforms: MutableMap<String, Int>? = null

    init {
        programId = GL30.glCreateProgram()
        if (programId == 0) {
            logger.error { "Could not create Shader" }
            throw Exception()
        }
        uniforms = HashMap()
    }

    @Throws(Exception::class)
    fun createVertexShader(shaderCode: String) {
        vertexShaderId = createShader(shaderCode, GL30.GL_VERTEX_SHADER)
    }

    @Throws(Exception::class)
    fun createFragmentShader(shaderCode: String) {
        fragmentShaderId = createShader(shaderCode, GL30.GL_FRAGMENT_SHADER)
    }

    @Throws(Exception::class)
    fun createShader(shaderCode: String, shaderType: Int): Int {
        val shaderId = GL30.glCreateShader(shaderType)
        if (shaderId == 0) {
            logger.error { "Error creating shader. Type: $shaderType" }
            throw Exception()
        }

        GL30.glShaderSource(shaderId, shaderCode)
        GL30.glCompileShader(shaderId)

        if (GL30.glGetShaderi(shaderId, GL30.GL_COMPILE_STATUS) == 0) {
            logger.error { "Error compiling Shader code: " + GL30.glGetShaderInfoLog(shaderId, 1024) }
            throw Exception("shader compiling failed")
        }

        GL30.glAttachShader(programId, shaderId)

        return shaderId
    }

    @Throws(Exception::class)
    fun link() {
        GL30.glLinkProgram(programId)
        if (GL30.glGetProgrami(programId, GL30.GL_LINK_STATUS) == 0) {
            logger.error { "Error linking Shader code: " + GL30.glGetProgramInfoLog(programId, 1024) }
            throw Exception("shader linking failed")
        }

        if (vertexShaderId != 0) {
            GL30.glDetachShader(programId, vertexShaderId)
        }

        if (fragmentShaderId != 0) {
            GL30.glDetachShader(programId, fragmentShaderId)
        }

        GL30.glValidateProgram(programId)
        if (GL30.glGetProgrami(programId, GL30.GL_VALIDATE_STATUS) == 0) {
            logger.warn { "Problem with validating Shader code: " + GL30.glGetProgramInfoLog(programId, 1024) }
        }
    }

    @Throws(Exception::class)
    fun createUniform(uniformName: String) {
        val uniformLocation: Int = GL30.glGetUniformLocation(programId, uniformName)
        if (uniformLocation < 0) {
            logger.error { "Error getting uniform location $uniformName" }
            throw Exception("locating uniform failed")
        }
        uniforms?.set(uniformName, uniformLocation)
    }

    @Throws(Exception::class)
    fun createPointLightUniform(uniformName: String) {
        createUniform("$uniformName.color")
        createUniform("$uniformName.position")
        createUniform("$uniformName.intensity")
        createUniform("$uniformName.att.constant")
        createUniform("$uniformName.att.linear")
        createUniform("$uniformName.att.exponent")
    }

    @Throws(Exception::class)
    fun createPointLightListUniform(uniformName: String, size: Int) {
        for (i in 0 until size) {
            createPointLightUniform("$uniformName[$i]")
        }
    }

    @Throws(Exception::class)
    fun createSpotLightListUniform(uniformName: String, size: Int) {
        for (i in 0 until size) {
            createSpotLightUniform("$uniformName[$i]")
        }
    }

    @Throws(Exception::class)
    fun createSpotLightUniform(uniformName: String) {
        createPointLightUniform("$uniformName.pl")
        createUniform("$uniformName.conedir")
        createUniform("$uniformName.cutoff")
    }

    @Throws(Exception::class)
    fun createDirectionalLightUniform(uniformName: String) {
        createUniform("$uniformName.color")
        createUniform("$uniformName.direction")
        createUniform("$uniformName.intensity")
    }

    @Throws(Exception::class)
    fun createMaterialUniform(uniformName: String) {
        createUniform("$uniformName.ambient")
        createUniform("$uniformName.diffuse")
        createUniform("$uniformName.specular")
        createUniform("$uniformName.hasTexture")
        createUniform("$uniformName.reflectance")
    }

    fun setUniform(uniformName: String, value: Matrix4f) {
        MemoryStack.stackPush().use { stack ->
            val fb: FloatBuffer = stack.mallocFloat(16)
            value.get(fb)
            uniforms!![uniformName]?.let { GL30.glUniformMatrix4fv(it, false, fb) }
        }
    }

    fun setUniform(uniformName: String, value: Int) {
        uniforms?.get(uniformName)?.let { GL30.glUniform1i(it, value) }
    }

    fun setUniform(uniformName: String, value: Vector3f) {
        uniforms?.get(uniformName)?.let { GL30.glUniform3f(it, value.x, value.y, value.z) }
    }

    fun setUniform(uniformName: String, value: Float) {
        uniforms?.get(uniformName)?.let { GL30.glUniform1f(it, value) }
    }

    fun setUniform(uniformName: String, value: Vector4f) {
        uniforms?.get(uniformName)?.let { GL30.glUniform4f(it, value.x, value.y, value.z, value.w) }
    }

    fun setUniform(uniformName: String, pointLight: PointLight) {
        setUniform("$uniformName.color", pointLight.color)
        setUniform("$uniformName.position", pointLight.position)
        setUniform("$uniformName.intensity", pointLight.intensity)

        val att: Attenuation = pointLight.attenuation
        setUniform("$uniformName.att.constant", att.constant)
        setUniform("$uniformName.att.linear", att.linear)
        setUniform("$uniformName.att.exponent", att.exponent)
    }

    fun setUniform(uniformName: String, pointLight: PointLight, pos: Int) {
        setUniform("$uniformName[$pos]", pointLight)
    }

    fun setUniform(uniformName: String, spotLight: SpotLight, pos: Int) {
        setUniform("$uniformName[$pos]", spotLight)
    }

    fun setUniform(uniformName: String, spotLight: SpotLight) {
        setUniform("$uniformName.pl", spotLight.pointLight)
        setUniform("$uniformName.conedir", spotLight.coneDirection)
        setUniform("$uniformName.cutoff", spotLight.cutOff)
    }

    fun setUniform(uniformName: String, dirLight: DirectionalLight) {
        setUniform("$uniformName.colour", dirLight.color)
        setUniform("$uniformName.direction", dirLight.direction)
        setUniform("$uniformName.intensity", dirLight.intensity)
    }

    fun setUniform(uniformName: String, material: Material) {
        setUniform("$uniformName.ambient", material.ambientColor)
        setUniform("$uniformName.diffuse", material.diffuseColor)
        setUniform("$uniformName.specular", material.specularColor)
        setUniform("$uniformName.hasTexture", if (material.isTextured()) 1 else 0)
        setUniform("$uniformName.reflectance", material.reflectance)
    }

    fun bind() {
        GL30.glUseProgram(programId)
    }

    fun unbind() {
        GL30.glUseProgram(0)
    }

    fun cleanup() {
        unbind()
        if (programId != 0) {
            GL30.glDeleteProgram(programId)
        }
    }
}