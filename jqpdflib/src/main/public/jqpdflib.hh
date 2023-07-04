#include <jni.h>

#ifndef _Included_ru_vzotov_jqpdf_JQPDF
#define _Included_ru_vzotov_jqpdf_JQPDF

#define POINTERHOLDER_TRANSITION 4

#ifdef __cplusplus
extern "C" {
#endif
JNIEXPORT jint JNICALL Java_ru_vzotov_jqpdf_JQPDF_pdfToJson
  (JNIEnv *, jobject, jbyteArray, jobject);

#ifdef __cplusplus
}
#endif
#endif

