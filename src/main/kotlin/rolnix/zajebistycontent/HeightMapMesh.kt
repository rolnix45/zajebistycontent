package rolnix.zajebistycontent

import mu.KotlinLogging
import org.joml.Math.abs
import org.joml.Vector3f
import org.lwjgl.stb.STBImage.*
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer
import java.nio.IntBuffer

private const val MAX_COLOR: Int = 255 * 255 * 255

private const val STARTX: Float = -0.5f
private const val STARTZ: Float = -0.5f

class HeightMapMesh(minY: Float, maxY: Float, heightMap: ByteBuffer, textureFile: String, textInc: Int) {
    private val logger = KotlinLogging.logger {}

    private var minY: Float
    private var maxY: Float

    var mesh: Mesh

    init {
        this.minY = minY
        this.maxY = maxY

        var buf: ByteBuffer?
        var width: Int
        var height: Int
        MemoryStack.stackPush().use { stack ->
            val w: IntBuffer = stack.mallocInt(1)
            val h: IntBuffer = stack.mallocInt(1)
            val channels: IntBuffer = stack.mallocInt(1)
            buf = stbi_load_from_memory(heightMap, w, h, channels, 4)
            if (buf == null) {
                logger.error { "Height map file not loaded: ${stbi_failure_reason()}" }
                throw Exception("error loading height map")
            }
            width = w.get()
            height = h.get()
        }

        val texture = Texture(Utils.ioResourceToByteBuffer(textureFile, 1024))

        val incX = getXLength() / (width - 1)
        val incZ = getZLength() / (height - 1)

        val positions: ArrayList<Float> = ArrayList()
        val textCoords: ArrayList<Float> = ArrayList()
        val indices: ArrayList<Int> = ArrayList()

        for (row in 0 until height)
            for (col in 0 until width) {
                positions.add(STARTX + col * incX)
                positions.add(getHeight(col, row, width, buf!!))
                positions.add(STARTZ + row * incZ)

                textCoords.add(textInc.toFloat() * col.toFloat() / width)
                textCoords.add(textInc.toFloat() * row.toFloat() / height)

                if (col < width - 1 && row < height - 1) {
                    val topLeft = row * width + col
                    val bottomLeft = (row + 1) * width + col
                    val bottomRight = (row + 1) * width + col + 1
                    val topRight = row * width + col + 1

                    indices.add(topLeft)
                    indices.add(bottomLeft)
                    indices.add(topRight)

                    indices.add(topRight)
                    indices.add(bottomLeft)
                    indices.add(bottomRight)
                }
            }

        val posArray: FloatArray = positions.toFloatArray()
        val indicesArray: IntArray = indices.toIntArray()
        val textCoordArray: FloatArray = textCoords.toFloatArray()
        val normalsArray: FloatArray = calcNormals(posArray, width, height)

        this.mesh = Mesh(posArray, textCoordArray, normalsArray, indicesArray)
        val material = Material(texture, 0.0f)
        mesh.material = material

        stbi_image_free(buf!!)
    }

    private fun calcNormals(posArray: FloatArray, width: Int, height: Int): FloatArray {
        val v0 = Vector3f()
        var v1 = Vector3f()
        var v2 = Vector3f()
        var v3 = Vector3f()
        var v4 = Vector3f()
        val v12 = Vector3f()
        val v23 = Vector3f()
        val v34 = Vector3f()
        val v41 = Vector3f()

        val normals: ArrayList<Float> = ArrayList()
        var normal = Vector3f()

        for (row in 0 until height)
            for (col in 0 until width) {
                if (row > 0 && row < height - 1 && col > 0 && col < width - 1) {
                    val i0 = row * width * 3 + col * 3
                    v0.x = posArray[i0]
                    v0.y = posArray[i0 + 1]
                    v0.z = posArray[i0 + 2]

                    val i1 = row * width * 3 + (col - 1) * 3
                    v1.x = posArray[i1]
                    v1.y = posArray[i1 + 1]
                    v1.z = posArray[i1 + 2]
                    v1 = v1.sub(v0)

                    val i2 = (row + 1) * width * 3 + col * 3
                    v2.x = posArray[i2]
                    v2.y = posArray[i2 + 1]
                    v2.z = posArray[i2 + 2]
                    v2 = v2.sub(v0)

                    val i3 = (row) * width * 3 + (col + 1) * 3
                    v3.x = posArray[i3]
                    v3.y = posArray[i3 + 1]
                    v3.z = posArray[i3 + 2]
                    v3 = v3.sub(v0)

                    val i4 = (row - 1) * width * 3 + col * 3
                    v4.x = posArray[i4]
                    v4.y = posArray[i4 + 1]
                    v4.z = posArray[i4 + 2]
                    v4 = v4.sub(v0)

                    v1.cross(v2, v12)
                    v12.normalize()

                    v2.cross(v3, v23)
                    v23.normalize()

                    v3.cross(v4, v34)
                    v34.normalize()

                    v4.cross(v1, v41)
                    v41.normalize()

                    normal = v12.add(v23).add(v34).add(v41)
                    normal.normalize()
                } else {
                    normal.x = 0f
                    normal.y = 1f
                    normal.z = 0f
                }

                normal.normalize()
                normals.add(normal.x)
                normals.add(normal.y)
                normals.add(normal.z)
            }

        return normals.toFloatArray()
    }

    private fun getHeight(x: Int, z: Int, width: Int, buffer: ByteBuffer): Float {
        val r: Byte = buffer.get(x * 4 + 0 + z * 4 * width)
        val g: Byte = buffer.get(x * 4 + 1 + z * 4 * width)
        val b: Byte = buffer.get(x * 4 + 2 + z * 4 * width)
        val a: Byte = buffer.get(x * 4 + 3 + z * 4 * width)
        val argb = ((0xFF and a.toInt()) shl 24) or ((0xFF and r.toInt()) shl 16) or
                ((0xFF and g.toInt()) shl 18) or (0xFF and b.toInt())
        return this.minY + abs(this.maxY - this.minY) * (argb.toFloat() / MAX_COLOR)
    }

    companion object {
        fun getXLength(): Float {
            return abs(-STARTX * 2)
        }

        fun getZLength(): Float {
            return abs(-STARTZ * 2)
        }
    }
}