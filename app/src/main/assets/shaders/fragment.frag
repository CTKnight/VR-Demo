#extension GL_OES_standard_derivatives : enable
precision mediump float;

uniform sampler2D uTexture;

uniform mat4 uModelMatrix;
uniform mat4 uViewMatrix;
uniform vec4 uColor;
uniform vec3 uLightPosition;
uniform int uUseNormal;
uniform int uHighlight;
uniform int uUseTexture;

varying vec4 vPosition;
varying vec3 vNormal;
varying vec2 vTextureCoord;
varying vec3 vFragPosition;

void main() {
//  vec3 normal = normalize(vNormal);
// compute normal on the fly
  vec3 dX = dFdx(vFragPosition);
  vec3 dY = dFdy(vFragPosition);
  vec3 normal = uUseNormal > 0 ? normalize(vNormal) : normalize(cross(dX, dY));

  vec3 lightPos = vec3(uViewMatrix * vec4(uLightPosition, 0.f));
  vec3 lightDir = normalize(lightPos - vFragPosition);
  float distance = length(lightPos - vFragPosition);
  float diffuse = max(dot(normal, lightDir), 0.1);
  diffuse = diffuse * (1.0 / (1.0 + (0.25 * distance * distance)));
  float ambient = uHighlight > 0 ? 0.8 : 0.5;
  diffuse += ambient;
  vec3 lightColor = vec3(1.0, 1.0, 1.0);
  vec4 texureColor = texture2D(uTexture, vec2(vTextureCoord.x, 1.0 - vTextureCoord.y));
  vec3 result1 = diffuse * lightColor * vec3(uColor);
  vec3 result2 = diffuse * lightColor * texureColor.rgb;
  vec3 endColor = uUseTexture > 0 ? result1 : result2;
  gl_FragColor = vec4(result1, uColor[3]);
}