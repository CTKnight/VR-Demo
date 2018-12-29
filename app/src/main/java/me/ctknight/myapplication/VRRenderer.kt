package me.ctknight.myapplication

import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import com.google.vr.sdk.base.Eye
import com.google.vr.sdk.base.GvrView
import com.google.vr.sdk.base.HeadTransform
import com.google.vr.sdk.base.Viewport
import me.ctknight.myapplication.drawer.IDrawer
import me.ctknight.myapplication.drawer.OffScreenDrawer
import me.ctknight.myapplication.drawer.SimpleDrawer
import me.ctknight.myapplication.ui.VRView
import javax.microedition.khronos.egl.EGLConfig

class VRRenderer(private val glSurfaceView: VRView) : GvrView.StereoRenderer {
  companion object {
    private val TAG = VRRenderer::class.java.simpleName
    private val STEP_FACTOR = 0.05f
  }

  private val scene: Scene
  private lateinit var drawer: IDrawer
  private lateinit var offScreenDrawer: OffScreenDrawer
  // set start location
  //      mAngle[1] += 180;
  //      mAngle[2] += 180;
  var isFlying = false
    set(flying) {
      field = flying

      var x = angle[0]
      var y = angle[1]
      var z = angle[2]
      x *= (Math.PI / 180.0f).toFloat()
      y *= (Math.PI / 180.0f).toFloat()
      z *= (Math.PI / 180.0f).toFloat()
      val cx = Math.cos(x.toDouble()).toFloat()
      val sx = Math.sin(x.toDouble()).toFloat()
      val cy = Math.cos(y.toDouble()).toFloat()
      val sy = Math.sin(y.toDouble()).toFloat()
      flyingLocation[0] = distance * cx * sy
      flyingLocation[1] = distance * sx
      flyingLocation[2] = distance * cx * cy

      if (flying) {
        angle[0] += 180f
      }
    }
  private val flyingLocation = FloatArray(3)


  private val mProjectionMatrix = FloatArray(16)
  private val mViewMatrix = FloatArray(16)
  private val mCameraMatrix = FloatArray(16)
  var distance = 20f
  val angle = floatArrayOf(0f, 0f, 0f)


  init {
    this.scene = glSurfaceView.scene
  }

  override fun onSurfaceCreated(config: EGLConfig) {
    drawer = SimpleDrawer(glSurfaceView.context)
    drawer.prepareOnGLThread()
    offScreenDrawer = OffScreenDrawer(glSurfaceView.context)
    offScreenDrawer.prepareOnGLThread()
    GLES20.glEnable(GLES20.GL_DEPTH_TEST)

    // Enable blending for combining colors when there is transparency
    GLES20.glEnable(GLES20.GL_BLEND)
    GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
  }

  override fun onSurfaceChanged(width: Int, height: Int) {
    GLES20.glViewport(0, 0, width, height)
    offScreenDrawer.setViewport(width, height)
  }

  override fun onNewFrame(headTransform: HeadTransform) {
    Matrix.setLookAtM(mCameraMatrix, 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f)
  }

  override fun onDrawEye(eye: Eye) {
    updateMatrix(eye)
    scene.updateOObjMotion()
    synchronized(scene) {
      drawer.preDraw()
      for (i in 0..scene.objMap.size()) {
        val value = scene.objMap.get(i.toLong())
        if (value != null) {
          drawer.draw(scene, value, mViewMatrix, mProjectionMatrix, 0)
        }
      }
    }
  }

  override fun onFinishFrame(viewport: Viewport) {}

  override fun onRendererShutdown() {
    Log.i(TAG, "onRendererShutdown")
  }

  private fun updateMatrix(eye: Eye) {
    System.arraycopy(eye.getPerspective(0.01f, 10f), 0,
        mProjectionMatrix, 0, mProjectionMatrix.size)
    Matrix.multiplyMM(mViewMatrix, 0, eye.eyeView, 0, mCameraMatrix, 0)

  }

  private fun oldUpdateMatrix() {
    var x = angle[0]
    var y = angle[1]
    var z = angle[2]
    x *= (Math.PI / 180.0f).toFloat()
    y *= (Math.PI / 180.0f).toFloat()
    z *= (Math.PI / 180.0f).toFloat()
    val cx = Math.cos(x.toDouble()).toFloat()
    val sx = Math.sin(x.toDouble()).toFloat()
    val cy = Math.cos(y.toDouble()).toFloat()
    val sy = Math.sin(y.toDouble()).toFloat()
    val cz = Math.cos(z.toDouble()).toFloat()
    val sz = Math.sin(z.toDouble()).toFloat()
    if (!isFlying) {
      Matrix.setLookAtM(mViewMatrix, 0,
          distance * cx * sy,
          distance * sx,
          distance * cx * cy,
          0f, 0f, 0f,
          0f, 1f, 0f)
    } else {
      val nextX = flyingLocation[0] + STEP_FACTOR * cx * sy
      val nextY = flyingLocation[1] - STEP_FACTOR * sx
      val nextZ = flyingLocation[2] + STEP_FACTOR * cx * cy
      Matrix.setLookAtM(mViewMatrix, 0,
          flyingLocation[0],
          flyingLocation[1],
          flyingLocation[2],
          nextX, nextY, nextZ,
          0f, 1f, 0f)
      flyingLocation[0] = nextX
      flyingLocation[1] = nextY
      flyingLocation[2] = nextZ
    }
  }

  fun getId(x: Int, y: Int): Long {
    synchronized(scene) {
      offScreenDrawer.preDraw()
      for (i in 0..scene.objMap.size()) {
        val value = scene.objMap.get(i.toLong())
        if (value != null) {
          offScreenDrawer.draw(scene, value, mViewMatrix, mProjectionMatrix, 0)
        }
      }
    }
    return offScreenDrawer.getId(x, y)
  }
}
