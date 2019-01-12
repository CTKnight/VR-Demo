package me.ctknight.myapplication.ui

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.widget.Toast
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.*
import com.google.vr.sdk.audio.GvrAudioEngine
import com.google.vr.sdk.base.GvrActivity
import de.javagl.obj.MtlReader
import de.javagl.obj.ObjReader
import de.javagl.obj.ObjSplitting
import de.javagl.obj.ObjUtils
import me.ctknight.myapplication.ModelData
import me.ctknight.myapplication.R
import me.ctknight.myapplication.Scene
import me.ctknight.myapplication.VRRenderer
import permissions.dispatcher.*
import java.io.IOException

@RuntimePermissions
class MainActivity : GvrActivity() {
  private lateinit var mRenderer: VRRenderer
  private lateinit var mScene: Scene
  private lateinit var mBackgroundThread: HandlerThread
  private lateinit var mBackgroundHandler: Handler
  private lateinit var mAudioEngine: GvrAudioEngine

  private var mARSession: Session? = null

  private var quit = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_main)
    gvrView = findViewById(R.id.surface_view)
    mBackgroundThread = HandlerThread("MainActivity-background")
    mBackgroundThread.start()
    mBackgroundHandler = Handler(mBackgroundThread.looper)

    mAudioEngine = GvrAudioEngine(this, GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY)
    mScene = Scene()
    mBackgroundHandler.post {
      initScene(mScene)
    }
    mRenderer = VRRenderer(applicationContext, mScene, this, mARSession, mAudioEngine)
    gvrView.setRenderer(mRenderer)
  }

  private fun initScene(scene: Scene) = try {
    //      val obj1 = ObjReader.read(resources.openRawResource(R.raw.cube))
    //      val id1 = mGLView.mScene.addObj(this, obj1, "models/andy.png")
    //      val model1 = mGLView.mScene.getObj(id1)
    //      model1.translated[0] = 5f

    with(scene) {
      val originalObj = ObjReader.read(resources.assets.open("models/model.obj"))
      val obj1 = ObjUtils.convertToRenderable(originalObj)
      val mtl = MtlReader.read(resources.assets.open("models/model.materials.mtl"))
      val materialGroups = ObjSplitting.splitByMaterialGroups(obj1)
      materialGroups.forEach { name, obj ->
        addObj(ModelData(this@MainActivity, obj, mtl.findLast { it.name == name }, null))
      }
    }

  } catch (e: IOException) {
    Log.e(TAG, "initScene: ", e)
    throw RuntimeException(e)
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
        val config = Config(mARSession)
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        mARSession?.configure(config)
        mRenderer.mARSession = mARSession
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
    } catch (e: CameraNotAvailableException) {
      Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
    }
    mRenderer.frameUpdater = this
  }

  override fun onStart() {
    super.onStart()
    resumeArSessionWithPermissionCheck()
  }

  override fun onResume() {
    super.onResume()
    mAudioEngine.resume()
  }

  override fun onPause() {
    super.onPause()
    mAudioEngine.pause()
    mARSession?.pause()
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
