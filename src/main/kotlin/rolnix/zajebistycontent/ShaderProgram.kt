package rolnix.zajebistycontent

import mu.KotlinLogging
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryStack
import java.nio.FloatBuffer


object ShaderProgram {
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