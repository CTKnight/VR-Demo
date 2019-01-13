package me.ctknight.vrdemo

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.vr.sdk.base.Eye
import com.google.vr.sdk.base.GvrView
import com.google.vr.sdk.base.HeadTransform
import com.google.vr.sdk.base.Viewport
import me.ctknight.vrdemo.drawer.BackgroundRenderer
import me.ctknight.vrdemo.drawer.IDrawer
import me.ctknight.vrdemo.drawer.SimpleDrawer
import me.ctknight.vrdemo.ui.MainActivity
import me.ctknight.vrdemo.utils.ShaderUtil
import me.ctknight.vrdemo.utils.toViewPoseTranslation
import javax.microedition.khronos.egl.EGLConfig

class VRRenderer(
    private val context: Context,
    var scene: Scene,
    var frameUpdater: MainActivity,
    var mARSession: Session? = null
) : GvrView.StereoRenderer {
  companion object {
    private val TAG = VRRenderer::class.java.simpleName
    private val Z_NEAR = 0.01f
    private val Z_FAR = 100f
    private val USE_AR = true
  }

  private lateinit var drawer: IDrawer
  private lateinit var backgroundRenderer: BackgroundRenderer

  private val mProjectionMatrix = FloatArray(16)
  private val mViewMatrix = FloatArray(16)
  private val mCameraMatrix = FloatArray(16)

  override fun onSurfaceCreated(config: EGLConfig) {
    drawer = SimpleDrawer(context)
    drawer.prepareOnGLThread()
    backgroundRenderer = BackgroundRenderer()
    backgroundRenderer.createOnGlThread(context)
  }

  override fun onSurfaceChanged(width: Int, height: Int) {
    Log.i(TAG, "onSurfaceChanged")
  }

  override fun onNewFrame(headTransform: HeadTransform) {
    ShaderUtil.checkGLError(TAG, "onNewFrame")
  }

  override fun onDrawEye(eye: Eye) {
    GLES20.glEnable(GLES20.GL_DEPTH_TEST)

    if (USE_AR) {
      mARSession?.setCameraTextureName(backgroundRenderer.textureId)
      val frame = mARSession?.update()

      if (frame != null) {
        updateMatrixByAR(eye, frame)
      }

//      backgroundRenderer.draw(frame)
    } else {
      updateMatrixByVR(eye)
    }
    val scene = this.scene
    scene.updateObjMotion()
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

  private val translationMatrix = FloatArray(16)
  private val sceneMatrix = FloatArray(16)
  private fun updateMatrixByAR(eye: Eye, frame: Frame) {
    val camera = frame.camera
    val cameraPose = camera.pose.toViewPoseTranslation()
    val sensorPose = frame.androidSensorPose.toViewPoseTranslation()
    val pose = cameraPose
    // the phone is rotated by 90 degree and the pose is still in sensor coordinate system
    pose.toMatrix(translationMatrix, 0)
    Matrix.multiplyMM(sceneMatrix, 0, eye.eyeView, 0, scene.viewPositionMatrix, 0)
    Matrix.multiplyMM(mViewMatrix, 0, translationMatrix, 0, sceneMatrix, 0)
    System.arraycopy(eye.getPerspective(0.01f, 50f), 0,
        mProjectionMatrix, 0, mProjectionMatrix.size)
  }

  private fun updateMatrixByVR(eye: Eye) {
    Matrix.setLookAtM(mCameraMatrix, 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f)
    System.arraycopy(eye.getPerspective(0.01f, 50f), 0,
        mProjectionMatrix, 0, mProjectionMatrix.size)
    Matrix.multiplyMM(mViewMatrix, 0, eye.eyeView, 0, mCameraMatrix, 0)
  }
}
