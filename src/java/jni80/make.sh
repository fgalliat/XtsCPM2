rm -rf bin 2>/dev/null
mkdir bin

javac -d bin -h . -cp libs/jlme0.1.3.jar:src/main/java src/main/java/com/xtase/jni80/JavaRunCPM.java
javac -d bin -cp libs/jlme0.1.3.jar:src/main/java src/main/java/com/xtase/jni80/JavaPascalCompiler.java
javac -d bin -cp libs/jlme0.1.3.jar:src/main/java src/main/java/com/xtase/jni80/JavaRunCPM_GFX.java