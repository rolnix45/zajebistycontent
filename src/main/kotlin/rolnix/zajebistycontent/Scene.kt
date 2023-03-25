package rolnix.zajebistycontent


class Scene {
    val meshMap: MutableMap<Mesh, MutableList<GameObject>>
    lateinit var skybox: Skybox
    lateinit var sceneLight: SceneLight

    init {
        meshMap = HashMap()
    }

    val gameMeshes: Map<Mesh, MutableList<GameObject>>
        get() = meshMap

    fun setGameObjects(gameObjects: Array<GameObject>) {
        val numGameItems = gameObjects.size
        for (i in 0 until numGameItems) {
            val gameItem: GameObject = gameObjects[i]
            val mesh: Mesh = gameItem.mesh
            var list: MutableList<GameObject>? = meshMap[mesh]
            if (list == null) {
                list = ArrayList()
                meshMap[mesh] = list
            }
            list.add(gameItem)
        }
    }
}