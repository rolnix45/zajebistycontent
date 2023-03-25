package rolnix.zajebistycontent

import org.joml.Vector3f

class Terrain(blocksPerRow: Int, scale: Float, minY: Float, maxY: Float, heightMap: String, textureFile: String, textInc: Int) {
    val gameObjects: Array<GameObject?> = Array(blocksPerRow * blocksPerRow) { null }

    init {
        val heightMapMesh = HeightMapMesh(minY, maxY, Utils.ioResourceToByteBuffer(heightMap, 8192), textureFile, textInc)
        for (row in 0 until blocksPerRow)
            for (col in 0 until blocksPerRow) {
                val xDisplacement: Float = (col - (blocksPerRow.toFloat() - 1) / 2f) * scale * HeightMapMesh.getXLength()
                val zDisplacement: Float = (row - (blocksPerRow.toFloat() - 1) / 2f) * scale * HeightMapMesh.getZLength()

                val terrainBlock = GameObject()

                terrainBlock.mesh = heightMapMesh.mesh
                terrainBlock.scale = scale
                terrainBlock.position = Vector3f(xDisplacement, 0f, zDisplacement)
                gameObjects[row * blocksPerRow + col] = terrainBlock
            }
    }
}