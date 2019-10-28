#version 300 es

in vec4 vPosition;
in vec4 vNormal;

uniform mat4 uMVPMatrix;

out vec4 vert_position;
out vec4 vert_normal;

void main() {
    gl_Position = uMVPMatrix * vPosition;
    vert_position = vPosition;
    vert_normal = vNormal;
}
