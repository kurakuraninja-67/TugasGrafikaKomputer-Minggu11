#version 330 core

in vec3 FragPos;
in vec3 Normal;
in vec2 TexCoord;

out vec4 FragColor;

uniform vec3 lightPos;
uniform vec3 viewPos;
uniform vec3 lightColor;
uniform vec3 objectColor;

uniform sampler2D shadowMap;
uniform mat4 lightSpaceMatrix;

uniform bool useTexture;
uniform sampler2D texture1;

float hitungBayangan(vec4 posisiCahaya)
{
    vec3 koordinat =
        posisiCahaya.xyz / posisiCahaya.w;

    koordinat = koordinat * 0.5 + 0.5;

    if (koordinat.z > 1.0)
        return 0.0;

    float kedalamanTerdekat =
        texture(shadowMap, koordinat.xy).r;

    float bias = 0.005;

    float bayangan =
        koordinat.z - bias > kedalamanTerdekat
        ? 1.0 : 0.0;

    return bayangan;
}

void main()
{
    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(lightPos - FragPos);
    vec3 viewDir = normalize(viewPos - FragPos);

    float diff = max(dot(norm, lightDir), 0.0);

    vec3 ambient = 0.2 * lightColor;
    vec3 diffuse = diff * lightColor;

    vec3 halfwayDir =
        normalize(lightDir + viewDir);

    float spec = pow(
        max(dot(norm, halfwayDir), 0.0),
        32.0
    );

    vec3 specular = 0.3 * spec * lightColor;

    float shadow =
        hitungBayangan(
            lightSpaceMatrix *
            vec4(FragPos, 1.0)
        );

    vec3 baseColor = objectColor;

    if (useTexture)
        baseColor = texture(texture1, TexCoord).rgb;

    vec3 lighting =
        ambient +
        (1.0 - shadow) * (diffuse + specular);

    FragColor = vec4(lighting * baseColor, 1.0);
}
