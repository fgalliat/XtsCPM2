# to build cpm80 .so lib

JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64

g++ -c -fPIC -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux JavaRunCPM.cpp -o JavaRunCPM.o
g++ -shared -fPIC -o libcpm80.so JavaRunCPM.o -lc