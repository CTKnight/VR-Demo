package me.ctknight.myapplication

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import android.widget.Toast
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.exceptions.*
import com.google.vr.sdk.audio.GvrAudioEngine
import com.google.vr.sdk.base.Eye
import com.google.vr.sdk.base.GvrView
import com.google.vr.sdk.base.HeadTransform
import com.google.vr.sdk.base.Viewport
import me.ctknight.myapplication.drawer.BackgroundRenderer
import me.ctknight.myapplication.drawer.IDrawer
import me.ctknight.myapplication.drawer.SimpleDrawer
import me.ctknight.myapplication.utils.ShaderUtil
import javax.microedition.khronos.egl.EGLConfig

class VRRenderer(private val context: Context, private val scene: Scene) : GvrView.StereoRenderer {
  companion object {
    private val TAG = VRRenderer::class.java.simpleName
    private val Z_NEAR = 0.01f
    private val Z_FAR = 100f
  }

  private val gvrAudioEngine: GvrAudioEngine = GvrAudioEngine(context, GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY)
  private lateinit var drawer: IDrawer
  private lateinit var backgroundRenderer: BackgroundRenderer

  private var ARSession: Session? = null

  private val mProjectionMatrix = FloatArray(16)
  private val mViewMatrix = FloatArray(16)
  private val mCameraMatrix = FloatArray(16)
  private val mHeadView = FloatArray(16)
  private val mHeadRotation = FloatArray(4)

  fun onPause() {
    gvrAudioEngine.pause()
    ARSession?.pause()
  }

  fun onResume() {
    gvrAudioEngine.resume()
    // checking vr core leaves to MainActivity
    if (ARSession == null) {
      val message: String?
      try {
        ARSession = Session(context)
      } catch (e: Exception) {
        when (e) {
          is UnavailableArcoreNotInstalledException -> {
            message = "Please install ARCore"
          }
          is UnavailableUserDeclinedInstallationException -> {
            message = "Please install ARCore"
          }
          is UnavailableApkTooOldException -> {
            message = "Please update ARCore"
          }
          is UnavailableSdkTooOldException -> {
            message = "Please update this app"
          }
          is UnavailableDeviceNotCompatibleException -> {
            message = "This device does not support AR"
          }
          else -> {
            message = "Failed to create AR session"
          }
        }
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        Log.e(TAG, "Exception creating AR session", e)
      }
    }
    try {
      ARSession?.resume()
    } catch (e: CameraNotAvailableException) {
      Toast.makeText(context, "Camera not available", Toast.LENGTH_SHORT).show()
    }
  }

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
    val frame = ARSession?.update()

    frame ?: return

    updateMatrix(eye, frame)
    backgroundRenderer.draw(frame)
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

  private fun updateMatrix(eye: Eye, frame: Frame) {
    try {
      ARSession?.setCameraTextureName(backgroundRenderer.textureId)
      val camera = frame.camera

      Matrix.setLookAtM(mCameraMatrix, 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f)
//    System.arraycopy(eye.getPerspective(0.01f, 50f), 0,
//        mProjectionMatrix, 0, mProjectionMatrix.size)
//    Matrix.multiplyMM(mViewMatrix, 0, eye.eyeView, 0, mCameraMatrix, 0)
      camera?.getProjectionMatrix(mProjectionMatrix, 0, Z_NEAR, Z_FAR)
      camera?.getViewMatrix(mViewMatrix, 0)
    } catch (e: Throwable) {
      Log.e(TAG, "exception when uploadMatrix", e)
    }
  }
}
