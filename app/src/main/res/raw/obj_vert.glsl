#version 300 es

precision highp float;

in vec4 vPosition;
in vec4 vCoord;

uniform mat4 uMatrix;

out vec4 vert_coord;

void main() {
    gl_Position = uMatrix * vPosition;
    vert_coord = vCoord;
}
