echo "Compile deps"
g++ -c xts_string.cpp
echo "Compile core"
g++ -c main.cpp
echo "Link"
g++ xts_string.o main.o
echo "Done"
mv a.out cpm80
#./cpm80
ls 