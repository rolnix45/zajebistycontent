package rolnix.zajebistycontent

import org.joml.Vector3f

class Skybox(objModel: String, textureFile: String) : GameObject() {

    init {
        val skyboxMesh = OBJLoader.loadMesh(objModel)
        val skyboxTexture = Texture(Utils.ioResourceToByteBuffer(textureFile, 1024))
        skyboxMesh.material = Material(skyboxTexture, 0.0f)
        this.position = Vector3f(0f, 0f, 0f)
        this.mesh = skyboxMesh
    }
}