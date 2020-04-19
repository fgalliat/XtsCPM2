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

  // if (reg < 0) { reg = 256+reg; }
  reg &= 0xff;
  value &= 0xffff;

  jint result = (env)->CallIntMethod( o, midStr, reg, value);

  return result;
}

// ========== External console Hook ========

  typedef unsigned char uint8;

  // TEMP dirty impl
  // int   _ext_kbhit(void) { return 0; }
  // uint8 _ext_getch(void) { return 0; }
  // void  _ext_putch(uint8 ch) { printf("%c", ch); }

  // void _ext_coninit(void)    { printf("Init the console.\n"); }
  // void _ext_conrelease(void) { printf("Release the console.\n"); }
  // void _ext_clrscr(void) { printf("-CLS-\n"); }

  static jmethodID midKbHit;
  static jmethodID midGetCh;
  static jmethodID midPutCh;
  static jmethodID midConInit;
  static jmethodID midConRelease;
  static jmethodID midClrScr;

  static bool XtsInitMethods(JNIEnv * env, jobject o) {
      printf("looking for Java Console methods ...\n");

      jclass _class = (env)->GetObjectClass( o );

      midKbHit = (env)->GetMethodID( _class, "_ext_kbhit", "()I");
      if ( midKbHit == nullptr ) { printf("kbhit failed \n"); return false; }
      midGetCh = (env)->GetMethodID( _class, "_ext_getch", "()C");
      if ( midGetCh == nullptr ) { printf("getch failed \n"); return false; }
      midPutCh = (env)->GetMethodID( _class, "_ext_putch", "(C)V");
      if ( midPutCh == nullptr ) { printf("putch failed \n"); return false; }

      midConInit    = (env)->GetMethodID( _class, "_ext_coninit",    "()V");
      if ( midConInit == nullptr ) { printf("conint failed \n"); return false; }
      midConRelease = (env)->GetMethodID( _class, "_ext_conrelease", "()V");
      if ( midConRelease == nullptr ) { printf("conrel failed \n"); return false; }
      midClrScr     = (env)->GetMethodID( _class, "_ext_clrscr",     "()V");
      if ( midClrScr == nullptr ) { printf("cls failed \n"); return false; }

      return true;
  }

  static JNIEnv * _env;
  static jobject instance;


  int   _ext_kbhit(void)     { return (_env)->CallIntMethod( instance, midKbHit); }
  uint8 _ext_getch(void)     { return (_env)->CallCharMethod( instance, midGetCh); }
  void  _ext_putch(uint8 ch) { (_env)->CallVoidMethod( instance, midPutCh, ch); }

  void _ext_coninit(void)    { (_env)->CallVoidMethod( instance, midConInit); }
  void _ext_conrelease(void) { (_env)->CallVoidMethod( instance, midConRelease); }
  void _ext_clrscr(void)     { (_env)->CallVoidMethod( instance, midClrScr); }

// ==========

  int XtsBdosCall(char reg, int value) {
    return XtsBdosCall( _env, instance, reg, value);
  }


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

    // jint reg = 228;
    // jint value = 6;

    // jint result = XtsBdosCall(env, _this,   reg, value);
    // printf("result of call [%d]\n", result);

    // result = XtsBdosCall(env, _this,   225, 0x1F);
    // printf("result of call [%d]\n", result);

    // int _reg = 228;
    // int _value = 0x08;
    // int _result = XtsBdosCall(env, _this,   _reg, _value);
    // printf("result of call [%d]  (native int version)\n", _result);

    if ( ! XtsInitMethods(env, _this) ) {
      printf("some methods failed\n");
      return;
    } 

    printf("all methods are OK\n");

    _env = env;
    instance = _this;

    startCPM();
}

#define MEMSIZE 64 * 1024
extern uint8 RAM[MEMSIZE];

JNIEXPORT jint JNICALL Java_JavaRunCPM_readRAM
  (JNIEnv * env, jobject _this, jint address) {
    address &= 0xffff;
    return RAM[address];
}

JNIEXPORT void JNICALL Java_JavaRunCPM_writeRAM
  (JNIEnv * env, jobject _this, jint address, jint value) {
    address &= 0xffff;
    value &= 0xff;

    RAM[address] = value;
}