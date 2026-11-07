#include <jni.h>
#include <vector>
#include <string>
#include <mutex>
#include "cactus.h"

static std::mutex engineLock;
static std::string result(int rc, const std::vector<char>& out) {
    if (rc < 0) return std::string("ENGINE ERROR: ") + (cactus_last_error() ? cactus_last_error() : "unknown");
    return std::string(out.data());
}
extern "C" JNIEXPORT jstring JNICALL Java_com_halthinks_cactusx_MainActivity_loadModel(JNIEnv* e, jobject, jbyteArray data) {
    std::lock_guard<std::mutex> guard(engineLock);
    if (!data) return e->NewStringUTF("missing model");
    jsize len=e->GetArrayLength(data);
    std::vector<unsigned char> bytes((size_t)len);
    e->GetByteArrayRegion(data,0,len,reinterpret_cast<jbyte*>(bytes.data()));
    int rc=cactus_load(bytes.data(),(unsigned long long)bytes.size());
    if (rc<0) return e->NewStringUTF(cactus_last_error());
    return e->NewStringUTF("loaded");
}
extern "C" JNIEXPORT jstring JNICALL Java_com_halthinks_cactusx_MainActivity_initNeedle(JNIEnv* e, jobject) {
    std::lock_guard<std::mutex> guard(engineLock);
    const char* tools=R"([{"name":"get_time","description":"Read the current device time","parameters":{"type":"object","properties":{},"required":[]}}])";
    int rc=cactus_init("Only choose explicitly registered tools. Never invent capabilities.",tools,nullptr);
    return e->NewStringUTF(rc<0?cactus_last_error():"ready");
}
extern "C" JNIEXPORT jstring JNICALL Java_com_halthinks_cactusx_MainActivity_complete(JNIEnv* e, jobject, jstring prompt) {
    std::lock_guard<std::mutex> guard(engineLock);
    const char* s=e->GetStringUTFChars(prompt,nullptr);
    std::vector<char> out(65536);
    int rc=cactus_complete(s,nullptr,0,128,out.data(),(int)out.size());
    e->ReleaseStringUTFChars(prompt,s);
    return e->NewStringUTF(result(rc,out).c_str());
}
extern "C" JNIEXPORT jstring JNICALL Java_com_halthinks_cactusx_MainActivity_transcribe(JNIEnv* e, jobject, jfloatArray input) {
    std::lock_guard<std::mutex> guard(engineLock);
    jsize n=e->GetArrayLength(input);
    std::vector<float> pcm((size_t)n);
    e->GetFloatArrayRegion(input,0,n,pcm.data());
    std::vector<char> out(65536);
    int rc=cactus_transcribe(pcm.data(),n,nullptr,nullptr,0,out.data(),(int)out.size());
    return e->NewStringUTF(result(rc,out).c_str());
}
extern "C" JNIEXPORT jstring JNICALL Java_com_halthinks_cactusx_MainActivity_describe(JNIEnv* e, jobject, jbyteArray input, jint w, jint h) {
    std::lock_guard<std::mutex> guard(engineLock);
    jsize n=e->GetArrayLength(input);
    if (w<=0 || h<=0 || (long long)n != (long long)w*h*3) return e->NewStringUTF("Invalid RGB image");
    std::vector<unsigned char> rgb((size_t)n);
    e->GetByteArrayRegion(input,0,n,reinterpret_cast<jbyte*>(rgb.data()));
    std::vector<char> out(131072);
    int rc=cactus_describe(rgb.data(),w,h,"{}",nullptr,nullptr,out.data(),(int)out.size());
    return e->NewStringUTF(result(rc,out).c_str());
}
