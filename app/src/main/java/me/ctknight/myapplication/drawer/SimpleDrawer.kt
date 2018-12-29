package me.ctknight.myapplication.drawer

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import me.ctknight.myapplication.ModelData
import me.ctknight.myapplication.Scene
import me.ctknight.myapplication.utils.ShaderUtil
import java.io.IOException

// must be created in GL render thread
class SimpleDrawer(
    private val context: Context
) : IDrawer() {
  private var mProgram: Int = 0
  private var mPositionHandle: Int = 0
  private var mColorHandle: Int = 0
  private var mNormalHandle: Int = 0
  private var mTextCoordHandle: Int = 0
  private var mLightPositionHandle: Int = 0
  private var mModelMatrixHandle: Int = 0
  private var mViewMatrixHandle: Int = 0
  private var mProjectionMatrixHandle: Int = 0
  private var mTextureHandle: Int = 0
  private var mUseNormalHandle: Int = 0
  private var mHighlightHandle: Int = 0


  private val vertexStride = COORDS_PER_VERTEX * 4

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

    // bind location instead of get location
    // to avoid shader compiler remove attributes
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
    mTextureHandle = GLES20.glGetUniformLocation(mProgram, "uTexture")
    mUseNormalHandle = GLES20.glGetUniformLocation(mProgram, "uUseNormal")
    mHighlightHandle = GLES20.glGetUniformLocation(mProgram, "uHighlight")
    ShaderUtil.checkGLError("SimpleDrawer", "build shader")
  }

  override fun draw(scene: Scene, model: ModelData, vMatrix: FloatArray, pMatrix: FloatArray, mode: Int) {
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
    GLES20.glUseProgram(mProgram)

    model.prepare()
    // Enable a handle to the triangle vertices
    ShaderUtil.checkGLError("SimpleDrawer", "before draw")

    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
    if (model.textureId >= 0) {
      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, model.textureId)
      GLES20.glUniform1i(mTextureHandle, 0)
    } else {
      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }
    // Prepare the triangle coordinate data
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, model.vertexBufferGLId)

    GLES20.glVertexAttribPointer(
        mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, model.verticesBaseAddress)
    GLES20.glVertexAttribPointer(
        mTextCoordHandle, 2, GLES20.GL_FLOAT, false, 0, model.texCoordsBaseAddress)
    GLES20.glVertexAttribPointer(mNormalHandle, 3, GLES20.GL_FLOAT, false, 0, model.normalsBaseAddress)
    GLES20.glEnableVertexAttribArray(mPositionHandle)
    GLES20.glEnableVertexAttribArray(mNormalHandle)
    GLES20.glEnableVertexAttribArray(mTextCoordHandle)


    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

    // get handle to fragment shader's vColor member

    // TODO: add light and ambient

    GLES20.glUniform4fv(mColorHandle, 1, model.color, 0)

    GLES20.glUniform3fv(mLightPositionHandle, 1, scene.lightPos, 0)


    val modelMatrix = model.modelMatrix
    GLES20.glUniformMatrix4fv(mModelMatrixHandle, 1, false, modelMatrix, 0)
    GLES20.glUniformMatrix4fv(mViewMatrixHandle, 1, false, vMatrix, 0)
    GLES20.glUniformMatrix4fv(mProjectionMatrixHandle, 1, false, pMatrix, 0)

    GLES20.glUniform1i(mUseNormalHandle, if (model.normalNum > 0) 1 else -1)
    GLES20.glUniform1i(mHighlightHandle, if (model.isHighlight) 1 else 0)

    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, model.faceIndexGLId)
    GLES20.glDrawElements(GLES20.GL_TRIANGLES, model.numFaceVertices, GLES20.GL_UNSIGNED_SHORT, 0)
    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)

    // Disable vertex array
    GLES20.glDisableVertexAttribArray(mPositionHandle)
    GLES20.glDisableVertexAttribArray(mNormalHandle)
    GLES20.glDisableVertexAttribArray(mTextCoordHandle)

    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

    ShaderUtil.checkGLError("SimpleDrawer", "after draw")
  }

  companion object {

    private val COORDS_PER_VERTEX = 3

    private val TAG = SimpleDrawer::class.java.simpleName
    private val VERTEX_SHADER_NAME = "shaders/vertex.vert"
    private val FRAGMENT_SHADER_NAME = "shaders/fragment.frag"
  }
}
