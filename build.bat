@echo off
dir /s /B *.java > sources.txt

rmdir /s /q output
mkdir output
javac -source 1.8 -target 1.8 @sources.txt -d output

cd output
jar cfe MiniClient.jar miniclient.MiniClient miniclient
del ..\sources.txt
