#!/bin/bash

echo -n "Enter sw1080dp value : "
read text
x_1080=$(echo "scale = 2; $text * 1080 / 1080" | bc)
x_960=$(echo "scale = 2; $text * 960 / 1080" | bc)
x_840=$(echo "scale = 2; $text * 840 / 1080" | bc)
x_720=$(echo "scale = 2; $text * 720 / 1080" | bc)
x_600=$(echo "scale = 2; $text * 600 / 1080" | bc)
x_480=$(echo "scale = 2; $text * 480 / 1080" | bc)
x_360=$(echo "scale = 2; $text * 360 / 1080" | bc)
x_240=$(echo "scale = 2; $text * 240 / 1080" | bc)
echo " 1080dp : $x_1080"
echo "  960dp : $x_960"
echo "  840dp : $x_840"
echo "  720dp : $x_720"
echo "  600dp : $x_600"
echo "  480dp : $x_480"
echo "  360dp : $x_360"
echo "  240dp : $x_240"