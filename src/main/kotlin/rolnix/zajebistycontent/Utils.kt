package rolnix.zajebistycontent

import org.lwjgl.BufferUtils.createByteBuffer
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*


object Utils {

    @Throws(Exception::class)
    fun loadResource(fileName: String): String {
        var result: String
        Utils::class.java.getResourceAsStream(fileName).use { `in` ->
            Scanner(`in`!!, StandardCharsets.UTF_8.name()).use { scanner ->
                result = scanner.useDelimiter("\\A").next()
            }
        }
        return result
    }

    @Throws(IOException::class)
    fun ioResourceToByteBuffer(resource: String, bufferSize: Int): ByteBuffer {
        var buffer: ByteBuffer
        val path: Path = Paths.get(resource)
        if (Files.isReadable(path)) {
            Files.newByteChannel(path).use { fc ->
                buffer = createByteBuffer(fc.size().toInt() + 1)
            }
        } else {
            Utils::class.java.getResourceAsStream(resource).use { source ->
                Channels.newChannel(source!!).use { rbc ->
                    buffer = createByteBuffer(bufferSize)
                    while (true) {
                        val bytes: Int = rbc.read(buffer)
                        if (bytes == -1) {
                            break
                        }
                        if (buffer.remaining() == 0) {
                            buffer = resizeBuffer(buffer, buffer.capacity() * 2)
                        }
                    }
                }
            }
        }
        buffer.flip()
        return buffer
    }

    @Throws(Exception::class)
    fun readAllLines(fileName: String): List<String> {
        val list: MutableList<String> = ArrayList()
        Class.forName(Utils::class.java.name).getResourceAsStream(fileName)?.let {
            InputStreamReader(
                it
            )
        }?.let { info ->
            BufferedReader(
                info
            ).use { br ->
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    list.add(line!!)
                }
            }
        }
        return list
    }

    private fun resizeBuffer(buffer: ByteBuffer, newCapacity: Int): ByteBuffer {
        val newBuffer = createByteBuffer(newCapacity)
        buffer.flip()
        newBuffer.put(buffer)
        return newBuffer
    }
}