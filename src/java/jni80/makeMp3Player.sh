mkdir bin 2>/dev/null

javac -d bin -cp libs/jlme0.1.3.jar:src/main/java src/main/java/com/xtase/jni80/XtsJ80MP3Player.java
java  -cp libs/jlme0.1.3.jar:bin com.xtase.jni80.XtsJ80MP3Player $*

