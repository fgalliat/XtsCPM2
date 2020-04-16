echo "Clean"
rm -rf bin 2>/dev/null
mkdir bin

echo "Compile"
javac -d bin JavaRunCPM.java

echo "Make Headers"
javah -cp bin JavaRunCPM