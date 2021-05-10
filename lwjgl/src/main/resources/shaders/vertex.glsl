#version 330

layout(location = 0) in vec3 pos;
layout(location = 1) in vec2 texCoords;

out vec2 uv;

uniform mat4 projection;
uniform mat4 view;
uniform mat4 model;

void main() {
    mat4 mvp = projection * view * model;
    uv = texCoords;
    gl_Position = mvp * vec4(pos, 1.0);
}