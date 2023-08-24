#!/bin/sh

wget -O data.zip "https://health-atlas.de/data_files/594/download?version=1"
unzip data.zip

mkdir data
cd Vorhofflimmern
for file in *.json.zip
do
    unzip -o "$file" -d ../data
done

cd ..
rm data.zip
rm -rf Vorhofflimmern