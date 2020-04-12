echo "Cleaning"
rm -rf bin 2>/dev/null
mkdir bin 

echo "Compiling"
javac -cp . -d bin CPM.java

echo "Result"
ls bin/
