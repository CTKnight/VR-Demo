/*
 * Copyright 2019 Jiewen Lai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ctknight.vrdemo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import de.javagl.obj.Mtl
import de.javagl.obj.Obj
import de.javagl.obj.ObjData
import me.ctknight.vrdemo.utils.ShaderUtil
import java.io.IOException
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class ModelData(private val context: Context, inputObj: Obj, val mtl: Mtl?, val textureAssetName: String?) {
  companion object {
    private val DEFAULT_COLOR = floatArrayOf(1f, 1f, 1f, 1.0f)
  }

  var id: Long = -1
  val obj: Obj = inputObj
  val numFaceVertices: Int
  private val vertexBuffer: FloatBuffer
  private val faceVertexIndexBuffer: ShortBuffer
  private val normalBuffer: FloatBuffer
  private val textCoordsBuffer: FloatBuffer
  val normalNum: Int
  var textureId = -1
    private set
  var isHighlight: Boolean = false

  val color = FloatArray(4)
    get() {
      if (mtl != null) {
        val kd = mtl.kd
        field[0] = kd.x
        field[1] = kd.y
        field[2] = kd.z
        field[3] = 1f
      } else {
        System.arraycopy(DEFAULT_COLOR, 0, field, 0, 4)
      }
      return field
    }

  private var vertexBufferId: Int = 0
  private var indexBufferId: Int = 0
  // x, y, z
  // to calculate the model matrix
  val angle = floatArrayOf(0f, 0f, 0f)
  val translate = floatArrayOf(0f, 0f, 0f)
  var size = 1f

  private var prepared = false

  private val buffers = IntArray(2)
  var verticesBaseAddress: Int = 0
    private set
  var texCoordsBaseAddress: Int = 0
    private set
  var normalsBaseAddress: Int = 0
    private set

  val vertexBufferGLId: Int
    get() = buffers[0]

  val faceIndexGLId: Int
    get() = buffers[1]

  // member to avoid duplicated allocation
  private val model = FloatArray(16)
  private val rotate = FloatArray(16)

  init {
    vertexBuffer = ObjData.getVertices(obj)
    faceVertexIndexBuffer = ObjData.convertToShortBuffer(ObjData.getFaceVertexIndices(obj))
    normalBuffer = ObjData.getNormals(obj)
    textCoordsBuffer = ObjData.getTexCoords(obj, 2)
    numFaceVertices = ObjData.getTotalNumFaceVertices(obj)
    normalNum = normalBuffer.limit()
  }

  // must be called in GLThread
  fun prepare() {
    if (prepared) {
      return
    }
    prepared = true
    GLES20.glGenBuffers(2, buffers, 0)
    vertexBufferId = buffers[0]
    indexBufferId = buffers[1]

    verticesBaseAddress = 0
    texCoordsBaseAddress = verticesBaseAddress + 4 * vertexBuffer.limit()
    normalsBaseAddress = texCoordsBaseAddress + 4 * textCoordsBuffer.limit()
    val totalBytes = normalsBaseAddress + 4 * normalBuffer.limit()

    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId)
    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, totalBytes, null, GLES20.GL_STATIC_DRAW)
    GLES20.glBufferSubData(
        GLES20.GL_ARRAY_BUFFER, verticesBaseAddress, 4 * vertexBuffer.limit(), vertexBuffer)
    GLES20.glBufferSubData(
        GLES20.GL_ARRAY_BUFFER, texCoordsBaseAddress, 4 * textCoordsBuffer.limit(), textCoordsBuffer)
    GLES20.glBufferSubData(
        GLES20.GL_ARRAY_BUFFER, normalsBaseAddress, 4 * normalBuffer.limit(), normalBuffer)
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

    // Load index buffer
    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBufferId)
    val indexCount = numFaceVertices
    GLES20.glBufferData(
        GLES20.GL_ELEMENT_ARRAY_BUFFER, 2 * indexCount, faceVertexIndexBuffer, GLES20.GL_STATIC_DRAW)
    GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)

    ShaderUtil.checkGLError("ModelData", "OBJ buffer load")

    if (textureAssetName != null) {
      val textures = IntArray(1)

      val textureBitmap: Bitmap
      try {
        textureBitmap = BitmapFactory.decodeStream(context.assets.open(textureAssetName))
      } catch (e: IOException) {
        throw RuntimeException(e)
      }

      GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
      GLES20.glGenTextures(textures.size, textures, 0)
      textureId = textures[0]
      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

      GLES20.glTexParameteri(
          GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR)
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
      GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textureBitmap, 0)
      GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
      ShaderUtil.checkGLError("ModelData", "texture load")
      textureBitmap.recycle()
    }

    if (mtl != null) {

    }
  }

  // set the translate part
  // don't user the Matrix.translateM(), it's factor
  fun getModelMatrix(matrix: FloatArray, offset: Int) {
    Matrix.setIdentityM(model, 0)
    Matrix.scaleM(model, 0, size, size, size)
    Matrix.setRotateEulerM(rotate, 0, angle[0], angle[1], angle[2])
    Matrix.multiplyMM(matrix, offset, model, 0, rotate, 0)
    System.arraycopy(translate, 0, matrix, offset + 12, 3)
  }
}
