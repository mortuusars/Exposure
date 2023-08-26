#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec3 Mul;
uniform vec3 Add;
uniform float Saturation;
uniform float Contrast;

out vec4 fragColor;

// This shader is a duplication of minecraft:color_convolve but with Contrast and without InSize to stop the warnings about no finding uniform InSize.
void main() {
    vec4 InTexel = texture(DiffuseSampler, texCoord);

    vec3 RGB = InTexel.rgb * Mul + Add;

    //Saturation
    vec3 Gray = vec3(0.3, 0.59, 0.11);
    float Luma = dot(RGB, Gray);
    vec3 Chroma = RGB - Luma;
    RGB = (Chroma * Saturation) + Luma;

    // Contrast
    RGB = (RGB - 0.5) * Contrast + 0.5;

    fragColor = vec4(RGB, 1.0);


//    // Color Matrix
//    float RedValue = dot(InTexel.rgb, RedMatrix);
//    float GreenValue = dot(InTexel.rgb, GreenMatrix);
//    float BlueValue = dot(InTexel.rgb, BlueMatrix);
//    vec3 OutColor = vec3(RedValue, GreenValue, BlueValue);
//
//    // Offset & Scale
//    OutColor = (OutColor * ColorScale) + Offset;
//
//    // Saturation
//    float Luma = dot(OutColor, Gray);
//    vec3 Chroma = OutColor - Luma;
//    OutColor = (Chroma * Saturation) + Luma;
//
//    // Contrast
//    OutColor = (OutColor - 0.5) * Contrast + 0.5;
//
//    fragColor = vec4(OutColor, 1.0);
}
