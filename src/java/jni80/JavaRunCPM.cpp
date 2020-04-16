#include <jni.h>

#include "JavaRunCPM.h"

// Globals
static jmethodID midStr;
//static char * sigStr = "(Ljava/lang/String;ILjava/lang/String;)V";
static const char * sigStr = "(II)V";





// sample for Java :
// void javaDefineString(String name, int index, String value)
// 
// // Methods
// static void javaDefineString(JNIEnv * env, jobject o, char * name, jint index, char * value) {
//   jstring string = (*env)->NewStringUTF(env, name);
//   (*env)->CallVoidMethod(env, o, midStr, string, index, (*env)->NewStringUTF(env, value));
// }

// Methods
static void javaDefineString(JNIEnv * env, jobject o,  jint reg, jint value) {

  jclass _class = (env)->GetObjectClass( o );
  // Init - One time to initialize the method id, (use an init() function)
  // midStr = (env)->GetMethodID( _class, "test_XtsBdosCall", sigStr);

  // public int XtsBdosCall(int reg, int value)
  midStr = (env)->GetMethodID( _class, "XtsBdosCall", "(II)I");

  // jstring string = (*env)->NewStringUTF(env, name);
  // (env)->CallVoidMethod( o, midStr, reg, value);
  jint result = (env)->CallIntMethod( o, midStr, reg, value);

  printf("result of call [%d]\n", result);
}


JNIEXPORT void JNICALL Java_JavaRunCPM_startCPM
  (JNIEnv * env, jobject _this) {

printf("coucou\n");

    jint reg = 228;
    jint value = 6;

    javaDefineString(env, _this,   reg, value);

printf("bye bye\n");

}