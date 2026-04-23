//
// Created by Raining on 2026/4/14.
// #I# assimp
//

#include <GLES3/gl3.h>
#include <jni.h>
#include "esUtils.h"
#include <cmath>
#include <algorithm>
#include <string>
#include <vector>
#include <assimp/Importer.hpp>
#include <assimp/scene.h>
#include <assimp/postprocess.h>
#include <glm/glm.hpp>
#include <glm/gtc/matrix_transform.hpp>

namespace example30 {

    // ─── Inline shaders ──────────────────────────────────────────────────────
    static const char *VERTEX_SRC = R"(#version 300 es
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;

out vec3 FragPos;
out vec3 Normal;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main() {
    vec4 worldPos = model * vec4(aPos, 1.0);
    FragPos  = worldPos.xyz;
    Normal   = mat3(transpose(inverse(model))) * aNormal;
    gl_Position = projection * view * worldPos;
}
)";

    static const char *FRAGMENT_SRC = R"(#version 300 es
precision mediump float;

out vec4 FragColor;

in vec3 FragPos;
in vec3 Normal;

uniform vec3 lightPos;
uniform vec3 lightColor;
uniform vec3 objectColor;

void main() {
    // ambient
    float ambientStrength = 0.35;
    vec3 ambient = ambientStrength * lightColor;

    // diffuse
    vec3 norm     = normalize(Normal);
    vec3 lightDir = normalize(lightPos - FragPos);
    float diff    = max(dot(norm, lightDir), 0.0);
    vec3 diffuse  = diff * lightColor;

    // specular
    vec3 viewDir    = normalize(-FragPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec      = pow(max(dot(viewDir, reflectDir), 0.0), 32.0);
    vec3 specular   = 0.3 * spec * lightColor;

    vec3 result = (ambient + diffuse + specular) * objectColor;
    FragColor = vec4(result, 1.0);
}
)";

    // ─── Data structures ─────────────────────────────────────────────────────
    struct Vertex {
        glm::vec3 Position;
        glm::vec3 Normal;
    };

    struct Mesh {
        GLuint VAO = 0, VBO = 0, EBO = 0;
        unsigned int indexCount = 0;
        glm::vec3 diffuseColor{1.0f, 0.6f, 1.0f};
    };

    // ─── State ───────────────────────────────────────────────────────────────
    static esUtils::Shader *shader = nullptr;
    static std::vector<Mesh> meshes;
    static int sWidth = 1, sHeight = 1;
    static float angle = 0.0f;

    // ─── Helpers ─────────────────────────────────────────────────────────────
    static void processMesh(aiMesh *mesh, const aiScene *scene) {
        std::vector<Vertex> vertices;
        vertices.reserve(mesh->mNumVertices);
        for (unsigned int i = 0; i < mesh->mNumVertices; i++) {
            Vertex v{};
            v.Position = {mesh->mVertices[i].x, mesh->mVertices[i].y, mesh->mVertices[i].z};
            if (mesh->HasNormals())
                v.Normal = {mesh->mNormals[i].x, mesh->mNormals[i].y, mesh->mNormals[i].z};
            else
                v.Normal = {0.0f, 1.0f, 0.0f};
            vertices.push_back(v);
        }

        std::vector<unsigned int> indices;
        for (unsigned int i = 0; i < mesh->mNumFaces; i++) {
            const aiFace &face = mesh->mFaces[i];
            for (unsigned int j = 0; j < face.mNumIndices; j++)
                indices.push_back(face.mIndices[j]);
        }

        // Diffuse color from material
        glm::vec3 diffuse{1.0f, 0.6f, 1.0f};
        if (mesh->mMaterialIndex < scene->mNumMaterials) {
            aiColor3D c;
            if (scene->mMaterials[mesh->mMaterialIndex]->Get(AI_MATKEY_COLOR_DIFFUSE, c) == AI_SUCCESS)
                diffuse = {c.r, c.g, c.b};
        }

        Mesh m;
        m.diffuseColor = diffuse;
        m.indexCount = (unsigned int) indices.size();

        glGenVertexArrays(1, &m.VAO);
        glGenBuffers(1, &m.VBO);
        glGenBuffers(1, &m.EBO);

        glBindVertexArray(m.VAO);

        glBindBuffer(GL_ARRAY_BUFFER, m.VBO);
        glBufferData(GL_ARRAY_BUFFER, (GLsizeiptr) (vertices.size() * sizeof(Vertex)),
                     vertices.data(), GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, m.EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, (GLsizeiptr) (indices.size() * sizeof(unsigned int)),
                     indices.data(), GL_STATIC_DRAW);

        // location 0: position
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, GL_FALSE, sizeof(Vertex), nullptr);
        // location 1: normal
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, GL_FALSE, sizeof(Vertex),
                              (void *) offsetof(Vertex, Normal));

        glBindVertexArray(0);
        meshes.push_back(m);
    }

    static void processNode(aiNode *node, const aiScene *scene) {
        for (unsigned int i = 0; i < node->mNumMeshes; i++)
            processMesh(scene->mMeshes[node->mMeshes[i]], scene);
        for (unsigned int i = 0; i < node->mNumChildren; i++)
            processNode(node->mChildren[i], scene);
    }

    static void release() {
        for (auto &m: meshes) {
            glDeleteVertexArrays(1, &m.VAO);
            glDeleteBuffers(1, &m.VBO);
            glDeleteBuffers(1, &m.EBO);
        }
        meshes.clear();
        if (shader) {
            shader->release();
            delete shader;
            shader = nullptr;
        }
    }

} // namespace example30

using namespace example30;

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example30_GLRenderer_surfaceCreated(JNIEnv *env, jobject thiz, jstring res_dir) {
    release();

    const char *resDirPtr = env->GetStringUTFChars(res_dir, nullptr);
    std::string resDir = resDirPtr;
    env->ReleaseStringUTFChars(res_dir, resDirPtr);

    // Build shader from inline source
    shader = new esUtils::Shader(VERTEX_SRC, FRAGMENT_SRC);

    // Load model: DemoActivity copies assets/example29/ → cacheDir/example29/
    std::string bedObjPath = resDir + "/example29/models/bed2.obj";

    Assimp::Importer importer;
    const aiScene *scene = importer.ReadFile(
            bedObjPath,
            aiProcess_Triangulate | aiProcess_GenSmoothNormals | aiProcess_FlipUVs
    );
    if (!scene || (scene->mFlags & AI_SCENE_FLAGS_INCOMPLETE) || !scene->mRootNode) {
        ALOGE("Assimp load error: %s  (path=%s)", importer.GetErrorString(), bedObjPath.c_str());
        return;
    }
    processNode(scene->mRootNode, scene);
    ALOGE("example30: loaded %zu meshes from %s", meshes.size(), bedObjPath.c_str());

    glEnable(GL_DEPTH_TEST);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example30_GLRenderer_surfaceChanged(JNIEnv *env, jobject thiz, jint width, jint height) {
    sWidth = width > 0 ? width : 1;
    sHeight = height > 0 ? height : 1;
    glViewport(0, 0, sWidth, sHeight);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example30_GLRenderer_drawFrame(JNIEnv *env, jobject thiz) {
    glClearColor(0.12f, 0.12f, 0.15f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    if (!shader || meshes.empty()) return;

    // Slow auto-rotation around Y axis
    angle += 0.4f;
    if (angle >= 360.0f) angle -= 360.0f;

    shader->use();

    // Model: scale down (original coords ~±100 units) and rotate
    glm::mat4 model = glm::mat4(1.0f);
    model = glm::rotate(model, glm::radians(angle), glm::vec3(0.0f, 1.0f, 0.0f));
    model = glm::scale(model, glm::vec3(0.01f, 0.01f, 0.01f));

    // Camera
    glm::mat4 view = glm::lookAt(
            glm::vec3(0.0f, 1.5f, 3.5f),   // eye
            glm::vec3(0.0f, 0.0f, 0.0f),    // target
            glm::vec3(0.0f, 1.0f, 0.0f)     // up
    );

    // Projection
    float aspect = (float) sWidth / (float) sHeight;
    glm::mat4 projection = glm::perspective(glm::radians(45.0f), aspect, 0.1f, 100.0f);

    shader->setMat4("model", model);
    shader->setMat4("view", view);
    shader->setMat4("projection", projection);

    // Light in world space (above and to the side of the camera)
    shader->setVec3("lightPos", 3.0f, 5.0f, 4.0f);
    shader->setVec3("lightColor", 1.0f, 1.0f, 1.0f);

    // Draw each mesh with its own diffuse color
    for (auto &m: meshes) {
        shader->setVec3("objectColor", m.diffuseColor);
        glBindVertexArray(m.VAO);
        glDrawElements(GL_TRIANGLES, (GLsizei) m.indexCount, GL_UNSIGNED_INT, nullptr);
        glBindVertexArray(0);
    }

    glUseProgram(0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_ideacarry_example30_GLRenderer_release(JNIEnv *env, jobject thiz) {
    release();
}