find -name "*.java" > sources.txt

mkdir output
javac -source 1.8 -target 1.8 @sources.txt -d output

cd output
jar cfe MiniClient.jar miniclient.Miniclient miniclient
rm ../sources.txt
