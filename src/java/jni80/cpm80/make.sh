echo "Clean"
rm *.o 2>/dev/null
rm cpm80 2>/dev/null

echo "Compile deps"
g++ -c xts_string.cpp
echo "Compile core"
g++ -c main.cpp
#g++ -D USE_EXTERNAL_CONSOLE -c main.cpp

echo "Link"
g++ xts_string.o main.o
echo "Done"
mv a.out cpm80
#./cpm80
ls 