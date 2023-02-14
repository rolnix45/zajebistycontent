package rolnix.zajebistycontent

import mu.KotlinLogging
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.nanovg.NanoVGGL3.*
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.NULL
import java.nio.ByteBuffer
import java.nio.DoubleBuffer

object Hud {
    private val logger = KotlinLogging.logger {}

    private var vg: Long = 0

    private lateinit var fontBuffer: ByteBuffer

    private lateinit var color: NVGColor

    private lateinit var posx: DoubleBuffer
    private lateinit var posy: DoubleBuffer

    private const val lineSpacing: Float = 20f

    @Throws(Exception::class)
    fun init() {
        vg = nvgCreate(NVG_STENCIL_STROKES)
        if (vg == NULL) {
            logger.error { "Error during NanoVG initialization" }
            throw Exception("could not init nanovg")
        }
        fontBuffer = Utils.ioResourceToByteBuffer("/assets/fonts/Gamer.ttf", 150 * 1024)
        val font: Int = nvgCreateFontMem(vg, "REGULAR", fontBuffer, 0)
        if (font == -1) {
            throw Exception("Could not add font")
        }
        color = NVGColor.create()
        posx = MemoryUtil.memAllocDouble(1)
        posy = MemoryUtil.memAllocDouble(1)
    }

    fun render() {
        nvgBeginFrame(vg, Game.width.toFloat(), Game.height.toFloat(), 1f)

        // CROSSHAIR
        nvgBeginPath(vg)
        nvgCircle(vg, Game.width / 2f, Game.height / 2f, 5f)
        nvgStrokeColor(vg, rgba(0xc1, 0xe3, 0xf9, 200, color))
        nvgStrokeWidth(vg, .75f)
        nvgStroke(vg)

        //DEBUG INFO
        nvgFontSize(vg, 28f)
        nvgFontFace(vg, "REGULAR")
        nvgTextAlign(vg, NVG_ALIGN_LEFT or NVG_ALIGN_TOP)
        nvgFillColor(vg, rgba(0x00, 0x00, 0x00, 0xff, color))
        nvgText(vg, 1f, 0f,
            "FPS: ${Timer.getFPS()}   TPS: ${Timer.getTICK()}")

        nvgText(vg, 1f, lineSpacing,
            "MOUSE SENSITIVITY: ${String.format("%.2f", Game.mouseSensitivity)} [ and ]")

        nvgText(vg, 1f, lineSpacing * 2,
            "CAMERA SPEED: ${String.format("%.2f", Game.cameraSpeed)} ; and \'")

        nvgEndFrame(vg)

        Game.restoreState()
    }


    private fun rgba(r: Int, g: Int, b: Int, a: Int, colour: NVGColor): NVGColor {
        colour.r(r / 255.0f)
        colour.g(g / 255.0f)
        colour.b(b / 255.0f)
        colour.a(a / 255.0f)
        return colour
    }

    fun cleanup() {
        nvgDelete(vg)
        MemoryUtil.memFree(posx)
        MemoryUtil.memFree(posy)
    }
}