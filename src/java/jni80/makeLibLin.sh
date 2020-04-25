# to build cpm80 .so lib

JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64

#g++ -c -fPIC -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux JavaRunCPM.cpp -o JavaRunCPM.o
#g++ -shared -fPIC -o libcpm80.so JavaRunCPM.o -lc

g++ -c -fPIC -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux cpm80/xts_string.cpp -o xts_string.o
g++ -c -fPIC -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux cpm80/main.cpp -o main.o -DUSE_EXTERNAL_CONSOLE -DUSE_EXTERNAL_BDOS
g++ -c -fPIC -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux com_xtase_jni80_JavaRunCPM.cpp -o com_xtase_jni80_JavaRunCPM.o
g++ -shared -fPIC -o libcpm80.so com_xtase_jni80_JavaRunCPM.o xts_string.o main.o -lc

##for Win MinGW
#g++ -c -I%JAVA_HOME%\include -I%JAVA_HOME%\include\win32 com_baeldung_jni_HelloWorldJNI.cpp -o com_baeldung_jni_HelloWorldJNI.o
#g++ -shared -o native.dll com_baeldung_jni_HelloWorldJNI.o -Wl,--add-stdcall-alias


rm *.o

mv *.so bin/