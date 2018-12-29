#extension GL_OES_standard_derivatives : enable
precision mediump float;

uniform float uId;
// easier for debugging
//uniform float uModelNum;

void main() {
  // just to return the id in a channel to do a
  // TODO: remove hard code
  float id = uId / 5.0f;
  gl_FragColor = vec4(id, id, id, 1.f);
}