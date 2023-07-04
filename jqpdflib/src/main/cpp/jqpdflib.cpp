#include <cstdlib>
#include <cstring>
#include <iostream>

#include <jqpdflib.hh>
#include <qpdf/QPDF.hh>
#include <qpdf/QUtil.hh>
#include <qpdf/Pl_Buffer.hh>

JNIEXPORT jint JNICALL Java_ru_vzotov_jqpdf_JQPDF_pdfToJson(JNIEnv *env, jobject instance, jbyteArray inData, jobject callback)
{
    jclass cb_cls = env->GetObjectClass(callback);
    if (!cb_cls)
        return -1;

    jmethodID cb_method = env->GetMethodID(cb_cls, "callback", "([B)V");
    if (!cb_method)
        return -2;

    jboolean isCopy;
    jint length = env->GetArrayLength(inData);
    const char *buf = (const char *)env->GetPrimitiveArrayCritical(inData, &isCopy);
    try
    {
        QPDF pdf;
        pdf.processMemoryFile("jqpdflib", buf, length);
        env->ReleasePrimitiveArrayCritical(inData, (void *)buf, JNI_ABORT);
        buf = NULL;

        Pl_Buffer out("get json data");
        std::set<std::string> json_objects;
        pdf.writeJSON(2, &out, qpdf_dl_all, qpdf_sj_inline, "", json_objects);

        std::shared_ptr<Buffer> desired_data(out.getBuffer());
        jbyte const *desired_bytes = (jbyte const *)desired_data->getBuffer();
        jsize ret_len = desired_data->getSize();
        jbyteArray ret = env->NewByteArray(ret_len);
        env->SetByteArrayRegion(ret, 0, ret_len, desired_bytes);
        env->CallVoidMethod(callback, cb_method, ret);
    }
    catch (std::exception &e)
    {
        if (buf)
        {
            env->ReleasePrimitiveArrayCritical(inData, (void *)buf, JNI_ABORT);
        }
        std::cerr << ": " << e.what() << std::endl;
        exit(2);
    }

    std::cout << "Hello world!";
    return 0;
}
