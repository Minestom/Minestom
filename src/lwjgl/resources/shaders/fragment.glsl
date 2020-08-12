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
    uint closestDistance = uint(2147483647);
    for(int i = 4; i < paletteSize; i++) {
        vec3 mapColor = texture(palette, vec2((i+0.5f)/paletteSize, 0.0)).rgb;
        int dr = int((mapColor.r - vertexColor.r)*255);
        int dg = int((mapColor.g - vertexColor.g)*255);
        int db = int((mapColor.b - vertexColor.b)*255);

        uint d = uint(dr*dr)+uint(dg*dg)+uint(db*db);
        if(d < closestDistance) {
            closestDistance = d;
            closest = i;
        }
    }

    fragColor = vec4(closest/255.0, closest/255.0, closest/255.0, 1.0);
    //fragColor = vec4(vertexColor, 1.0);
}