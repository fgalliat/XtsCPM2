#include <jni.h>

#include "JavaRunCPM.h"

// Globals
static jmethodID midStr;

static bool xtsBdosCallMeth_inited = false;

// for a Java .xxx(String, int, String) -> void
// static const char * sigStr = "(Ljava/lang/String;ILjava/lang/String;)V";
//
// sample for Java :
// void javaDefineString(String name, int index, String value)
// 
// // Methods
// static void javaDefineString(JNIEnv * env, jobject o, char * name, jint index, char * value) {
//   jstring string = (*env)->NewStringUTF(env, name);
//   (*env)->CallVoidMethod(env, o, midStr, string, index, (*env)->NewStringUTF(env, value));
// }

// Methods
static jint XtsBdosCall(JNIEnv * env, jobject o,  jint reg, jint value) {

  if ( xtsBdosCallMeth_inited == false ) {
    printf("looking for Java method ...\n");

    jclass _class = (env)->GetObjectClass( o );
    // Init - One time to initialize the method id, (use an init() function)
    // midStr = (env)->GetMethodID( _class, "test_XtsBdosCall", sigStr);

    // public int XtsBdosCall(int reg, int value)
    midStr = (env)->GetMethodID( _class, "XtsBdosCall", "(II)I");

    xtsBdosCallMeth_inited = true;
  }

  // jstring string = (*env)->NewStringUTF(env, name);
  // (env)->CallVoidMethod( o, midStr, reg, value);
  jint result = (env)->CallIntMethod( o, midStr, reg, value);

  return result;
}

// ========== External console Hook ========

  typedef unsigned char uint8;

  // TEMP dirty impl
  int _ext_kbhit(void) { return 0; }
  uint8 _ext_getch(void) { return 0; }
  void _ext_putch(uint8 ch) { printf("%c", ch); }

  void _ext_coninit(void)    { printf("Init the console.\n"); }
  void _ext_conrelease(void) { printf("Release the console.\n"); }
  void _ext_clrscr(void) { printf("-CLS-\n"); }

// =========================================

  // from main.cpp
  extern void _console_init();
  extern void setup();
  extern void _console_reset();

  void startCPM() {
    _console_init();

    setup();

    _console_reset();
  }


JNIEXPORT void JNICALL Java_JavaRunCPM_startCPM
  (JNIEnv * env, jobject _this) {

    jint reg = 228;
    jint value = 6;

    jint result = XtsBdosCall(env, _this,   reg, value);
    printf("result of call [%d]\n", result);

    result = XtsBdosCall(env, _this,   225, 0x1F);
    printf("result of call [%d]\n", result);

    int _reg = 228;
    int _value = 0x08;
    int _result = XtsBdosCall(env, _this,   _reg, _value);
    printf("result of call [%d]  (native int version)\n", _result);

    startCPM();
}