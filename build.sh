find -name "*.java" > sources.txt

rm -rf output
mkdir output
javac -source 1.8 -target 1.8 @sources.txt -d output

cd output
jar cfe MiniClient.jar miniclient.MiniClient miniclient
rm ../sources.txt
