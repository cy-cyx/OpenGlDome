#version 300 es

in vec4 vPosition;

uniform mat4 uEyeMatrix;
uniform mat4 uLightMatrix;

out vec4 frag_LightPosition;

void main() {
    frag_LightPosition = uLightMatrix * vPosition;
    gl_Position = uEyeMatrix * vPosition;
}
