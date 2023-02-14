package rolnix.zajebistycontent

import org.joml.Vector3f

class Chunk(mesh1: Mesh, mesh2: Mesh, chunkOffsetX: Int, chunkOffsetZ: Int) {
    private val size: Int = 16
    private val height: Int = 8

    private lateinit var gameObject: GameObject

    init {
        for (x in 0 until size) {
            for (y in 0 until height) {
                for (z in 0 until size) {
                    if (y >= 7) {
                        gameObject = GameObject(
                            mesh1,
                            Vector3f(
                                (x + (chunkOffsetX * 16)).toFloat(),
                                y.toFloat(),
                                (z + (chunkOffsetZ * 16)).toFloat()
                            )
                        )
                    } else {
                        gameObject = GameObject(
                            mesh2,
                            Vector3f(
                                (x + (chunkOffsetX * 16)).toFloat(),
                                y.toFloat(),
                                (z + (chunkOffsetZ * 16)).toFloat()
                            )
                        )
                    }
                    Game.chunkGameObjectArrays.add(gameObject)
                }
            }
        }
    }
}