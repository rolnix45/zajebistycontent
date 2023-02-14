package rolnix.zajebistycontent

import mu.KotlinLogging
import org.lwjgl.opengl.GL30.*
import org.lwjgl.stb.STBImage.*
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer


class Texture(imageBuffer: ByteBuffer) {
    private var width: Int = 0
    private var height: Int = 0

    var id = 0

    init {
        var buf: ByteBuffer
        MemoryStack.stackPush().use { stack ->
            val w = stack.mallocInt(1)
            val h = stack.mallocInt(1)
            val channels = stack.mallocInt(1)
            buf = stbi_load_from_memory(imageBuffer, w, h, channels, 4)!!
            width = w.get()
            height = h.get()
        }
        this.id = createTexture(buf)
        stbi_image_free(buf)
    }

    private fun createTexture(buf: ByteBuffer): Int {
        val textureId: Int = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, textureId)

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

        glTexImage2D(
            GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,
            GL_RGBA, GL_UNSIGNED_BYTE, buf
        )
        glGenerateMipmap(GL_TEXTURE_2D)
        return textureId
    }

    fun cleanup() {
        glDeleteTextures(id)
    }
}