#version 300 es

layout(location = 8) in vec4 vPosition;
layout(location = 9) uniform mat4 uMVPMatrix;
layout(location = 10) in vec4 vNormal;
layout(location = 11) uniform vec4 vObjectColor;
layout(location = 12) uniform vec4 vLightColor;
layout(location = 13) uniform vec4 vLightDir;
layout(location = 14) uniform vec4 vEyeLocal;

out vec4 p_position;
out vec4 p_normal;

void main() {
    gl_Position = uMVPMatrix * vPosition;
    p_position = vPosition;
    p_normal = vNormal;
}
