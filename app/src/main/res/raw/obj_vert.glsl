#version 300 es

precision highp float;

in vec4 vPosition;
in vec4 vCoord;
in vec4 vNormal;

uniform mat4 uMatrix;

out vec4 vert_coord;
out vec4 vert_position;
out vec4 vert_normal;

void main() {
    vert_position = uMatrix * vPosition;
    gl_Position = uMatrix * vPosition;
    vert_coord = vCoord;
    vert_normal = vNormal;
}
