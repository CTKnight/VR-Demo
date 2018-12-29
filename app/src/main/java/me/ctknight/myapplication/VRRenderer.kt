package me.ctknight.myapplication

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import com.google.vr.sdk.audio.GvrAudioEngine
import com.google.vr.sdk.base.Eye
import com.google.vr.sdk.base.GvrView
import com.google.vr.sdk.base.HeadTransform
import com.google.vr.sdk.base.Viewport
import me.ctknight.myapplication.drawer.IDrawer
import me.ctknight.myapplication.drawer.SimpleDrawer
import me.ctknight.myapplication.utils.ShaderUtil
import javax.microedition.khronos.egl.EGLConfig

class VRRenderer(private val context: Context, private val scene: Scene) : GvrView.StereoRenderer {
  companion object {
    private val TAG = VRRenderer::class.java.simpleName
  }

  private var gvrAudioEngine: GvrAudioEngine = GvrAudioEngine(context, GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY)
  private lateinit var drawer: IDrawer

  private val mProjectionMatrix = FloatArray(16)
  private val mViewMatrix = FloatArray(16)
  private val mCameraMatrix = FloatArray(16)
  private val mHeadView = FloatArray(16)
  private val mHeadRotation = FloatArray(4)

  override fun onSurfaceCreated(config: EGLConfig) {
    drawer = SimpleDrawer(context)
    drawer.prepareOnGLThread()
  }

  override fun onSurfaceChanged(width: Int, height: Int) {
    Log.i(TAG, "onSurfaceChanged")
  }

  override fun onNewFrame(headTransform: HeadTransform) {
    Matrix.setLookAtM(mCameraMatrix, 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f)

    headTransform.getHeadView(mHeadView, 0)
    headTransform.getQuaternion(mHeadRotation, 0)
    gvrAudioEngine.setHeadRotation(
        mHeadRotation[0], mHeadRotation[1], mHeadRotation[2], mHeadRotation[3])
    // Regular update call to GVR audio engine.
    gvrAudioEngine.update()

    ShaderUtil.checkGLError(TAG, "onNewFrame")
  }

  override fun onDrawEye(eye: Eye) {
    GLES20.glEnable(GLES20.GL_DEPTH_TEST)
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
    System.arraycopy(eye.getPerspective(0.01f, 50f), 0,
        mProjectionMatrix, 0, mProjectionMatrix.size)
    Matrix.multiplyMM(mViewMatrix, 0, eye.eyeView, 0, mCameraMatrix, 0)
  }
}
