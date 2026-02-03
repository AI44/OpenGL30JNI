package com.ideacarry.example29

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms.  */
class Main : ApplicationAdapter() {
    private var batch: SpriteBatch? = null
    private var image: Texture? = null

    private var camera: PerspectiveCamera? = null
    private var modelBatch: ModelBatch? = null
    private var environment: Environment? = null

    private var model: Model? = null
    private var modelInstance: ModelInstance? = null

    override fun create() {
        batch = SpriteBatch()
        image = Texture("example29/libgdx.png")

        // 渲染器
        modelBatch = ModelBatch()

        // 相机
        camera = PerspectiveCamera(
            67f,
            Gdx.graphics.width.toFloat(),
            Gdx.graphics.height.toFloat()
        ).apply {
            position.set(3f, 3f, 3f)
            lookAt(0f, 1f, 0f)
            near = 0.1f
            far = 100f
            update()
        }

        // 光照
        environment = Environment().apply {
            set(ColorAttribute(ColorAttribute.AmbientLight, 0.7f, 0.7f, 0.7f, 1f))
            add(
                DirectionalLight().set(
                    Color.WHITE,
                    -1f, -0.8f, -0.2f
                )
            )
        }

        // ⭐ OBJ 加载
        val loader = ObjLoader()
        model = loader.loadModel(Gdx.files.internal("example29/models/bed2.obj"))
        modelInstance = ModelInstance(model).apply {
            transform.setToScaling(0.01f, 0.01f, 0.01f)
//            transform.rotate(0f, 0f, 1f, -90f)
        }
    }

    override fun render() {
        Gdx.gl.glClearColor(0.12f, 0.12f, 0.15f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        batch?.let {
            it.begin()
            it.draw(image, 140f, 210f)
            it.end()
        }

        // 转起来
        //modelInstance?.transform?.rotate(0f, 1f, 0f, Gdx.graphics.deltaTime * 30f)

        modelBatch?.let {
            it.begin(camera)
            it.render(modelInstance, environment)
            it.end()
        }
    }

    override fun dispose() {
        batch?.dispose()
        image?.dispose()
        modelBatch?.dispose()
        model?.dispose()
    }
}
