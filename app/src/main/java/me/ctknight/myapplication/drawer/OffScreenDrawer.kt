package me.ctknight.myapplication.drawer

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.util.Log
import me.ctknight.myapplication.ModelData
import me.ctknight.myapplication.Scene
import me.ctknight.myapplication.utils.ShaderUtil
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer


// not that off-screen, still draw on framebuffer 0
class OffScreenDrawer(
    private val context: Context
) : IDrawer() {
  private val VERTEX_SHADER_NAME = "shaders/vertex.vert"
  private val FRAGMENT_SHADER_NAME = "shaders/touch.frag"
  private val TAG = OffScreenDrawer::class.java.simpleName

  private var width: Int = 0
  private var height: Int = 0
  private var mProgram = -1
  private var mPositionHandle: Int = 0
  private var mColorHandle: Int = 0
  private var mNormalHandle: Int = 0
  private var mTextCoordHandle: Int = 0
  private var mLightPositionHandle: Int = 0
  private var mModelMatrixHandle: Int = 0
  private var mViewMatrixHandle: Int = 0
  private var mProjectionMatrixHandle: Int = 0
  private var mIdHandle = 0

  private lateinit var buffer: IntBuffer
  override fun prepareOnGLThread() {
    try {


      val vertexShader = ShaderUtil.loadGLShader(TAG, context, GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_NAME)
      val fragmentShader = ShaderUtil.loadGLShader(TAG, context, GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_NAME)

      mProgram = GLES20.glCreateProgram()
      GLES20.glAttachShader(mProgram, vertexShader)
      GLES20.glAttachShader(mProgram, fragmentShader)

    } catch (e: IOException) {
      Log.d(TAG, "prepareOnGLThread: ioe when reading shaders", e)
      throw RuntimeException(e)
    }
    GLES20.glBindAttribLocation(mProgram, 1, "aPosition")
    GLES20.glBindAttribLocation(mProgram, 2, "aNormal")
    GLES20.glBindAttribLocation(mProgram, 3, "aTextureCoord")
    GLES20.glLinkProgram(mProgram)
    GLES20.glUseProgram(mProgram)

    mPositionHandle = 1
//        GLES20.glGetAttribLocation(mProgram, "aPosition");
    mNormalHandle = 2
//                GLES20.glGetAttribLocation(mProgram, "aNormal");
    mTextCoordHandle = 3
//                GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
    mColorHandle = GLES20.glGetUniformLocation(mProgram, "uColor")
    mLightPositionHandle = GLES20.glGetUniformLocation(mProgram, "uLightPosition")
    mModelMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uModelMatrix")
    mViewMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uViewMatrix")
    mProjectionMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uProjectionMatrix")
    mIdHandle = GLES20.glGetUniformLocation(mProgram, "uId")
    ShaderUtil.checkGLError(TAG, "build shader")
  }

  override fun draw(scene: Scene, model: ModelData, vMatrix: FloatArray, pMatrix: FloatArray, mode: Int) {

    GLES20.glUseProgram(mProgram)
    // set to 0
    // just one frame, it won't affect visual effect
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    model.prepare()
    // Enable a handle to the triangle vertices
    ShaderUtil.checkGLError("SimpleDrawer", "before draw")
    // Prepare the triangle coordinate data
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, model.vertexBufferGLId)

    GLES20.glVertexAttribPointer(
        mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, model.verticesBaseAddress)
    GLES20.glVertexAttribPointer(
        mTextCoordHandle, 2, GLES20.GL_FLOAT, false, 0, model.texCoordsBaseAddress)
    GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 0, model.normalsBaseAddress)
    GLES20.glEnableVertexAttribArray(mPositionHandle)
    GLES20.glEnableVertexAttribArray(mNormalHandle)
    GLES20.glEnableVertexAttribArray(mTextCoordHandle)


    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

    // get handle to fragment shader's vColor member

    // TODO: add light and ambient
    val color = floatArrayOf(0.5f, 0.5f, 0.5f, 1.0f)
    GLES20.glUniform4fv(mColorHandle, 1, color, 0)

    GLES20.glUniform3fv(mLightPositionHandle, 1, scene.lightPos, 0)


    val modelMatrix = model.modelMatrix
    GLES20.glUniformMatrix4fv(mModelMatrixHandle, 1, false, modelMatrix, 0)
    GLES20.glUniformMatrix4fv(mViewMatrixHandle, 1, false, vMatrix, 0)
    GLES20.glUniformMatrix4fv(mProjectionMatrixHandle, 1, false, pMatrix, 0)

    GLES20.glUniform1f(mIdHandle, model.id.toFloat())

    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, model.faceIndexGLId)
    GLES20.glDrawElements(GLES20.GL_TRIANGLES, model.numFaceVertices, GLES20.GL_UNSIGNED_SHORT, 0)
    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)

    // Disable vertex array
    GLES20.glDisableVertexAttribArray(mPositionHandle)
    GLES20.glDisableVertexAttribArray(mNormalHandle)
    GLES20.glDisableVertexAttribArray(mTextCoordHandle)

    ShaderUtil.checkGLError("SimpleDrawer", "after draw")
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
  }

  fun setViewport(width: Int, height: Int) {
    this.width = width
    this.height = height

    buffer = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder()).asIntBuffer()
  }

  fun getId(x: Int, y: Int): Long {
    buffer.rewind()
    // 0 for debug

    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer)
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    bitmap.copyPixelsFromBuffer(buffer)
    // the whole bitmap is render in reverted y !!
    val pixel = bitmap.getPixel(x, height - y)
    // 5 is related to shader
    // unsigned to signed
    // low 8 bit for r channel
    // 255 is size of UByte
    val id = ((pixel and 0x000000ff) / 255.0 * 5.0).toLong()
    bitmap.recycle()
    return id
  }
}