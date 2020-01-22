//
// Created by Nikhil Lohar on 2019-12-12.
//

#include <jni.h>
#include <jni.h>
#include <android/log.h>
#include <stdio.h>
#include <android/bitmap.h>
#include <cstring>
#include <unistd.h>

//We can do in a easy way by passing the pixels in array then we can find color in it

extern "C" JNIEXPORT jboolean JNICALL
Java_com_scratchcard_ScratchLayout_getTransparentPercent(JNIEnv *env, jobject thiz,
                                                                  jintArray pixels) {
    const jsize length = env->GetArrayLength(pixels);

    jint *pixelArray = env->GetIntArrayElements(pixels, NULL);

    int count = 0;

    for (int i = 0; i < length; i++) {
        int A = (pixelArray[i] >> 24) & 0xFF000000;
        if (A == 0) {
            count++;
        }
    }

    float percent = ((float)count/ (float)length) * 100;

    if (percent >= 40){
        return true;
    }
    return false;
}

/*
 * Or we can pass a bitmap over here can
 * and not just calculating we can do lot more stuff here
 * */

extern "C" JNIEXPORT jboolean JNICALL
Java_com_scratchcard_ScratchLayout_calculatePixel(JNIEnv *env, jobject thiz,
                                                           jobject bitmap) {
    uint8_t *bitmapPixel;

    AndroidBitmapInfo info;

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) {
        __android_log_print(ANDROID_LOG_INFO, "bitmap-processing", "ret valude = %d",
                            AndroidBitmap_getInfo(env, bitmap, &info));
        return NULL;
    }

    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        __android_log_print(ANDROID_LOG_INFO, "bitmap-processing", "Bitmap type error");
        return NULL;
    }

    if ((AndroidBitmap_lockPixels(env, bitmap, (void **) &bitmapPixel)) < 0) {
        __android_log_print(ANDROID_LOG_INFO, "bitmap-processing", "Bitmap type error");
        return NULL;
    }

    struct pixel {
        uint8_t r, g, b, a;
    };
    uint32_t num_transparent = 0;
    for (int y = 0; y < info.height; y++) {
        pixel *row = (pixel *) (bitmapPixel + y * info.stride);
        for (int x = 0; x < info.width; x++) {
            const pixel &p = row[x];
            if (p.a == 0)
                num_transparent++;
        }
    }

    float proportion_transparent = float(num_transparent) / (info.width * info.height) * 100;

    __android_log_print(ANDROID_LOG_INFO, "Bitmap-processing", "Transparent value : %f",
                        proportion_transparent);

    AndroidBitmap_unlockPixels(env, bitmap);

    if (proportion_transparent >= 40){
        return true;
    } else {
        return false;
    }
}
