echo "cleaning"
rm -rf bin 2>/dev/null
mkdir bin
echo "Compiling"
echo ". core"
javac -d bin -cp src src/j80/J80.java
javac -d bin -cp src src/j80/mmu/PlainMMU.java
echo ". crt"
javac -d bin -cp src src/j80/CharDevice.java
javac -d bin -cp src src/j80/vdu/Hazeltine1500.java
javac -d bin -cp src src/j80/vdu/GraphicsCRT.java
