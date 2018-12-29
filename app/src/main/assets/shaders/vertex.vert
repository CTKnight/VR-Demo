uniform mat4 uModelMatrix;
uniform mat4 uViewMatrix;
uniform mat4 uProjectionMatrix;

attribute vec4 aPosition;
attribute vec3 aNormal;
attribute vec2 aTextureCoord;

varying vec4 vPosition;
varying vec3 vNormal;
varying vec2 vTextureCoord;
varying vec3 vFragPosition;

void main() {
  mat4 MVMatrix = uViewMatrix * uModelMatrix;
  vPosition = aPosition;
  vPosition.w  = 1.f;
  vFragPosition = vec3(uModelMatrix * vPosition);
  vNormal = (MVMatrix * vec4(aNormal, 1.0)).xyz;
  vTextureCoord = aTextureCoord;
  gl_Position = uProjectionMatrix * MVMatrix * vPosition;
}