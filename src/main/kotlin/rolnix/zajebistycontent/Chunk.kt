package rolnix.zajebistycontent

import org.joml.Vector3f

class Chunk(mesh1: Mesh, mesh2: Mesh, chunkOffsetX: Int, chunkOffsetZ: Int) {
    private val size: Int = 16
    private val height: Int = 13

    private lateinit var block: GameObject

    init {
        for (x in 0 until size) {
            for (y in 5 until height) {
                for (z in 0 until size) {
                    if (y >= 7) {
                        block          = GameObject()
                        block.scale    = 0.5f
                        block.mesh     = mesh1
                        block.position = Vector3f(
                            (x + (chunkOffsetX * 16)).toFloat(),
                            y.toFloat(),
                            (z + (chunkOffsetZ * 16)).toFloat())
                    } else {
                        block          = GameObject()
                        block.scale    = 0.5f
                        block.mesh     = mesh2
                        block.position = Vector3f(
                            (x + (chunkOffsetX * 16)).toFloat(),
                            y.toFloat(),
                            (z + (chunkOffsetZ * 16)).toFloat()
                        )
                    }
                    Game.gameObjectArrays.add(block)
                }
            }
        }
    }
}