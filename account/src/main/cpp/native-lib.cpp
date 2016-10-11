#include <jni.h>
#include <string>
#include "md5.h"

extern "C"
JNIEXPORT jstring JNICALL
Java_tv_ismar_account_IsmartvActivator_stringFromJNI(JNIEnv *env, jobject instance) {

    char *filename = "sys/class/net/eth0/address";
    char ch;
    char macaddress[64];
    FILE *fp;
    int i = 0;
    memset(macaddress, 0, 64);
    char *buf = (char *) malloc(64);
    if ((fp = fopen(filename, "r")) == NULL) {
//        LOGVFINGERPRINT("不能打开文件\n");
        memset(buf, 0, 64);
        memcpy(buf, "noaddress", strlen("noaddress"));
    } else {
        while ((ch = fgetc(fp)) != EOF) {
            if (ch >= 255)
                break;
            macaddress[i++] = ch;
        }
//        LOGVFINGERPRINT("macaddress = %s", macaddress);
        memset(buf, 0, 64);
        memcpy(buf, macaddress, strlen(macaddress));
        fclose(fp);
    }
    return env->NewStringUTF(buf);
}

extern "C"
uint8_t *MD5(unsigned char *data, size_t len, uint8_t *out) {
    MD5_CTX ctx;
    static uint8_t digest[16];

    /* TODO(fork): remove this static buffer. */
    if (out == NULL) {
        out = digest;
    }

    MD5Init(&ctx);
    MD5Update(&ctx, data, len);
    MD5Final(out, &ctx);

    return out;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_tv_ismar_account_IsmartvActivator_helloMd5(JNIEnv *env, jobject instance, jstring str_) {
    const char *source = env->GetStringUTFChars(str_, 0);
    unsigned char md[16];
    int i;
    char tmp[3] = {'\0'}, buf[33] = {'\0'};
    MD5((unsigned char *) source, strlen(source), md);
    for (i = 0; i < 16; i++) {
        sprintf(tmp, "%2.2x", md[i]);
        strcat(buf, tmp);
    }
    env->ReleaseStringUTFChars(str_, source);
    return env->NewStringUTF(buf);
}



