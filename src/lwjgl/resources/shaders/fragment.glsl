#version 330

in vec2 uv;

out vec4 fragColor;

uniform sampler2D box;
uniform sampler2D palette;
uniform float paletteSize;

void main() {
    vec3 vertexColor = texture(box, uv).rgb;


    // render in map colors
    int closest = 0;
    float distance = 1.0f/0.0f;
    for(int i = 1; i < paletteSize; i++) {
        vec3 mapColor = texture(palette, vec2(i/paletteSize, 0.0)).rgb;
        float dr = mapColor.r - vertexColor.r;
        float dg = mapColor.g - vertexColor.g;
        float db = mapColor.b - vertexColor.b;

        float d = dr*dr+dg*dg+db*db;
        if(d < distance) {
            distance = d;
            closest = i;
        }
    }

    fragColor = vec4(closest/255.0, closest/255.0, closest/255.0, 1.0);
    //fragColor = vec4(vertexColor, 1.0);
}