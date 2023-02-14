package rolnix.zajebistycontent

import org.joml.Vector3f
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Mesh(positions: FloatArray, textCoords: FloatArray, normals: FloatArray, indices: IntArray) {
    private var vaoId: Int

    private var vboIdList: MutableList<Int>

    private var vertexCount: Int

    var texture: Texture? = null

    var color: Vector3f = Vector3f()

    private val defaultColor = Vector3f(0f, 255f, 0f)

    init {
        color = defaultColor

        var positionBuffer: FloatBuffer = MemoryUtil.memAllocFloat(0)
        var indicesBuffer: IntBuffer = MemoryUtil.memAllocInt(0)
        var textCoordsBuffer: FloatBuffer = MemoryUtil.memAllocFloat(0)
        var normalsBuffer: FloatBuffer = MemoryUtil.memAllocFloat(0)
        try {
            vertexCount = indices.size
            vboIdList = ArrayList()

            vaoId = GL30.glGenVertexArrays()
            GL30.glBindVertexArray(vaoId)

            // POSITION VBO
            var vboId = GL30.glGenBuffers()
            vboIdList.add(vboId)
            positionBuffer = MemoryUtil.memAllocFloat(positions.size)
            positionBuffer.put(positions).flip()
            GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId)
            GL30.glBufferData(GL30.GL_ARRAY_BUFFER, positionBuffer, GL30.GL_STATIC_DRAW)
            GL30.glEnableVertexAttribArray(0)
            GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, 0, 0)

            // TEXTURE COORDINATES VBO
            vboId = GL30.glGenBuffers()
            vboIdList.add(vboId)
            textCoordsBuffer = MemoryUtil.memAllocFloat(textCoords.size)
            textCoordsBuffer.put(textCoords).flip()
            GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId)
            GL30.glBufferData(GL30.GL_ARRAY_BUFFER, textCoordsBuffer, GL30.GL_STATIC_DRAW)
            GL30.glEnableVertexAttribArray(1)
            GL30.glVertexAttribPointer(1, 2, GL30.GL_FLOAT, false, 0, 0)

            // NORMALS VBO
            vboId = GL30.glGenBuffers()
            vboIdList.add(vboId)
            normalsBuffer = MemoryUtil.memAllocFloat(normals.size)
            normalsBuffer.put(normals).flip()
            GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId)
            GL30.glBufferData(GL30.GL_ARRAY_BUFFER, normalsBuffer, GL30.GL_STATIC_DRAW)
            GL30.glVertexAttribPointer(2, 3, GL30.GL_FLOAT, false, 0, 0)

            // INDEX VBO
            vboId = GL30.glGenBuffers()
            vboIdList.add(vboId)
            indicesBuffer = MemoryUtil.memAllocInt(indices.size)
            indicesBuffer.put(indices).flip()
            GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, vboId)
            GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL30.GL_STATIC_DRAW)

            GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0)
            GL30.glBindVertexArray(0)
        } finally {
            MemoryUtil.memFree(positionBuffer)
            MemoryUtil.memFree(indicesBuffer)
            MemoryUtil.memFree(textCoordsBuffer)
            MemoryUtil.memFree(normalsBuffer)
        }
    }

    fun render() {
        texture?.id?.let {
            GL30.glActiveTexture(GL30.GL_TEXTURE0)
            GL30.glBindTexture(GL30.GL_TEXTURE_2D, it)
        }

        GL30.glBindVertexArray(vaoId)
        GL30.glEnableVertexAttribArray(0)
        GL30.glEnableVertexAttribArray(1)
        GL30.glEnableVertexAttribArray(2)

        GL30.glDrawElements(GL30.GL_TRIANGLES, vertexCount, GL30.GL_UNSIGNED_INT, 0)

        GL30.glDisableVertexAttribArray(0)
        GL30.glDisableVertexAttribArray(1)
        GL30.glDisableVertexAttribArray(2)
        GL30.glBindVertexArray(0)
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0)
    }

    fun isTextured(): Boolean {
        return this.texture != null
    }

    fun cleanUp() {
        GL30.glDisableVertexAttribArray(0)

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0)
        for (vboId in vboIdList) {
            GL30.glDeleteBuffers(vboId)
        }

        texture?.cleanup()

        GL30.glBindVertexArray(0)
        GL30.glDeleteVertexArrays(vaoId)
    }
}