package me.ctknight.myapplication.ui

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.widget.Toast
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.exceptions.*
import com.google.vr.sdk.audio.GvrAudioEngine
import com.google.vr.sdk.base.GvrActivity
import de.javagl.obj.ObjReader
import me.ctknight.myapplication.R
import me.ctknight.myapplication.Scene
import me.ctknight.myapplication.VRRenderer
import permissions.dispatcher.*
import java.io.IOException

@RuntimePermissions
class MainActivity : GvrActivity() {
  private lateinit var mGLView: VRView
  private lateinit var mRenderer: VRRenderer
  private lateinit var mScene: Scene
  private lateinit var mBackgroundThread: HandlerThread
  private lateinit var mBackgroundHandler: Handler
  private lateinit var mAudioEngine: GvrAudioEngine

  private var mARSession: Session? = null
  private var sessionResumed = false
  val frameMonitor = Object()
  var frame: Frame? = null
    private set

  private var quit = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_main)
    gvrView = findViewById(R.id.surface_view)
    mBackgroundThread = HandlerThread("MainActivity-background")
    mBackgroundThread.start()
    mBackgroundHandler = Handler(mBackgroundThread.looper)

    val updateFrameRunnable = object : Runnable {
      override fun run() {
        val isQuit = synchronized(this@MainActivity) {
          quit
        }
        if (!isQuit) {
          frame = synchronized(frameMonitor) {
            if (sessionResumed) {
              mARSession?.update()
            } else {
              null
            }
          }
          mBackgroundHandler.post(this)
        }
      }
    }
    mBackgroundHandler.post {
      mScene = initScene()
      mAudioEngine = GvrAudioEngine(this, GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY)
      mRenderer = VRRenderer(applicationContext, mScene, this, mARSession, mAudioEngine)
      mGLView.renderer = mRenderer
    }
    mBackgroundHandler.post(updateFrameRunnable)
  }

  private fun initScene(): Scene {
    val scene = Scene()
    try {
      //      val obj1 = ObjReader.read(resources.openRawResource(R.raw.cube))
      //      val id1 = mGLView.mScene.addObj(this, obj1, "models/andy.png")
      //      val model1 = mGLView.mScene.getObj(id1)
      //      model1.translated[0] = 5f

      with(scene) {
        val obj2 = ObjReader.read(resources.assets.open("models/airboat.obj"))
        val id2 = addObj(this@MainActivity, obj2, "models/andy.png")
        val model2 = getObj(id2)

        val obj3 = ObjReader.read(resources.assets.open("models/andy.obj"))
        val id3 = addObj(this@MainActivity, obj3, "models/andy.png")
        val model3 = getObj(id3)
        if (model3 != null) {
          model3.size = 30f
          model3.translate[2] = -5f
        }
        val obj4 = ObjReader.read(resources.assets.open("models/earth.obj"))
        val id4 = addObj(this@MainActivity, obj4, "models/4096_earth.jpg")
        val model4 = getObj(id4)

        if (model4 != null) {
          model4.size = 0.01f
          model4.translate[1] = 10f
        }
      }

    } catch (e: IOException) {
      Log.e(TAG, "onCreate: ", e)
      throw RuntimeException(e)
    }
    return scene
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    onRequestPermissionsResult(requestCode, grantResults)
  }

  @NeedsPermission(Manifest.permission.CAMERA)
  fun resumeArSession() {
    if (mARSession == null) {
      val message: String?
      try {
        mARSession = Session(this)
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
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.e(TAG, "Exception creating AR session", e)
      }
    }
    try {
      mARSession?.resume()
      sessionResumed = true
    } catch (e: CameraNotAvailableException) {
      Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
    }
    mRenderer.frameUpdater = this
  }

  override fun onResume() {
    super.onResume()
    mAudioEngine.resume()
    resumeArSessionWithPermissionCheck()
  }

  override fun onPause() {
    super.onPause()
    mAudioEngine.pause()
    mARSession?.pause()
    sessionResumed = false
  }

  override fun onDestroy() {
    super.onDestroy()
    mBackgroundThread.quitSafely()
    synchronized(this) {
      quit = true
    }
  }

  @OnShowRationale(Manifest.permission.CAMERA)
  fun showRationaleForCamera(request: PermissionRequest) {
//    showRationaleDialog(R.string.permission_camera_rationale, request)
  }

  @OnPermissionDenied(Manifest.permission.CAMERA)
  fun onCameraDenied() {
    Toast.makeText(this, R.string.permission_camera_denied, Toast.LENGTH_SHORT).show()
  }

  @OnNeverAskAgain(Manifest.permission.CAMERA)
  fun onCameraNeverAskAgain() {
    Toast.makeText(this, R.string.permission_camera_never_askagain, Toast.LENGTH_SHORT).show()
  }

  companion object {
    private val TAG = MainActivity::class.java.simpleName
  }
}
