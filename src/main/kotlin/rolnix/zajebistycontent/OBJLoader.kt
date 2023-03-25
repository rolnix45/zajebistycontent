package rolnix.zajebistycontent

import mu.KotlinLogging
import org.joml.Vector2f
import org.joml.Vector3f

object OBJLoader {
    private val logger = KotlinLogging.logger {}

    @Throws(Exception::class)
    fun loadMesh(fileName: String): Mesh {
        val lines: List<String> = Utils.readAllLines(fileName)
        if (lines.isEmpty()) {
            logger.error { "Error loading object file: $fileName" }
        }

        val vertices: MutableList<Vector3f> = ArrayList()
        val textures: MutableList<Vector2f> = ArrayList()
        val normals: MutableList<Vector3f> = ArrayList()
        val faces: MutableList<Face> = ArrayList()

        for (line in lines) {
            val tokens = line.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            when (tokens[0]) {
                "v" -> {
                    // Geometric vertex
                    val vec3f = Vector3f(
                        tokens[1].toFloat(),
                        tokens[2].toFloat(),
                        tokens[3].toFloat())
                    vertices.add(vec3f)
                }
                "vt" -> {
                    // Texture coordinate
                    val vec2f = Vector2f(
                        tokens[1].toFloat(),
                        tokens[2].toFloat())
                    textures.add(vec2f)
                }
                "vn" -> {
                    // Vertex normal
                    val vec3fNorm = Vector3f(
                        tokens[1].toFloat(),
                        tokens[2].toFloat(),
                        tokens[3].toFloat())
                    normals.add(vec3fNorm)
                }
                "f" -> {
                    val face = Face(
                        tokens[1],
                        tokens[2],
                        tokens[3])
                    faces.add(face)
                }
                else -> Unit
            }
        }

        return reorderLists(vertices, textures, normals, faces)
    }

    private fun reorderLists(
        posList      : MutableList<Vector3f>,
        textCoordList: MutableList<Vector2f>,
        normList     : MutableList<Vector3f>,
        facesList    : MutableList<Face>): Mesh {

        val indices: MutableList<Int> = ArrayList()
        val posArray = FloatArray(posList.size * 3)
        for ((i, pos) in posList.withIndex()) {
            posArray[i * 3] = pos.x
            posArray[i * 3 + 1] = pos.y
            posArray[i * 3 + 2] = pos.z
        }

        val textCoordArray = FloatArray(posList.size * 2)
        val normArray = FloatArray(posList.size * 3)

        for (face in facesList) {
            val faceVertexIndex: Array<IdxGroup> = face.getFaceVertexIndices()
            for (indValue in faceVertexIndex) {
                processFaceVertex(
                    indValue, textCoordList, normList,
                    indices, textCoordArray, normArray
                )
            }
        }

        val indicesArray: IntArray = indices.stream().mapToInt { v: Int -> v }.toArray()
        return Mesh(posArray, textCoordArray, normArray, indicesArray)
    }

    private fun processFaceVertex(
        indices       : IdxGroup,
        textCoordList : MutableList<Vector2f>,
        normList      : MutableList<Vector3f>,
        indicesList   : MutableList<Int>,
        textCoordArray: FloatArray,
        normArray     : FloatArray) {

        val posIndex: Int = indices.idxPos
        indicesList.add(posIndex)

        if (indices.idxTextCoord >= 0) {
            val textCoord: Vector2f = textCoordList[indices.idxTextCoord]
            textCoordArray[posIndex * 2]     =     textCoord.x
            textCoordArray[posIndex * 2 + 1] = 1 - textCoord.y
        }

        if (indices.idxVecNormal >= 0) {
            val vecNorm: Vector3f = normList[indices.idxVecNormal]
            normArray[posIndex * 3]     = vecNorm.x
            normArray[posIndex * 3 + 1] = vecNorm.y
            normArray[posIndex * 3 + 2] = vecNorm.z
        }
    }

    class IdxGroup {
        var idxPos: Int = NO_VALUE
        var idxTextCoord: Int = NO_VALUE
        var idxVecNormal: Int = NO_VALUE

        companion object {
            const val NO_VALUE = -1
        }
    }

    class Face(v1: String, v2: String, v3: String) {
        private var idxGroups: Array<IdxGroup> = Array(3) { IdxGroup() }

        init {
            idxGroups[0] = parseLine(v1)
            idxGroups[1] = parseLine(v2)
            idxGroups[2] = parseLine(v3)
        }

        private fun parseLine(line: String): IdxGroup {
            val idxGroup = IdxGroup()

            val lineTokens: List<String> = line.split("/")
            idxGroup.idxPos = lineTokens[0].toInt() - 1
            if (lineTokens.size > 1) {
                val textCoord: String = lineTokens[1]
                idxGroup.idxTextCoord =
                    if (textCoord.isNotEmpty()) textCoord.toInt() - 1 else IdxGroup.NO_VALUE
                if (lineTokens.size > 2) {
                    idxGroup.idxVecNormal = lineTokens[2].toInt() - 1
                }
            }

            return idxGroup
        }

        fun getFaceVertexIndices(): Array<IdxGroup> {
            return idxGroups
        }
    }
}