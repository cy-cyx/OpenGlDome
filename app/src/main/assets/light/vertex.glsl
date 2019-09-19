#version 300 es

layout(location = 0) in vec4 vPosition;
layout(location = 1) uniform mat4 uMVPMatrix;
layout(location = 2) in vec4 vNormal;
layout(location = 3) uniform vec4 vObjectColor;
layout(location = 4) uniform vec4 vLightColor;
layout(location = 5) uniform vec4 vLightDir;
layout(location = 6) uniform vec4 vEyeLocal;

out vec4 p_position;
out vec4 p_normal;

void main() {
    gl_Position = uMVPMatrix * vPosition;
    p_position = vPosition;
    p_normal = vNormal;
}
