#!/bin/bash

echo -n "Enter sw720dp value : "
read text
x_720dp=$(echo "scale = 2; $text / 2 + 0.5" | bc)
x_1080=$(echo "scale = 2; $x_720dp * 1080 / 720" | bc)
x_960=$(echo "scale = 2; $x_720dp * 960 / 720" | bc)
x_840=$(echo "scale = 2; $x_720dp * 840 / 720" | bc)
x_600=$(echo "scale = 2; $x_720dp * 600 / 720" | bc)
x_480=$(echo "scale = 2; $x_720dp * 480 / 720" | bc)
x_360=$(echo "scale = 2; $x_720dp * 360 / 720" | bc)
x_240=$(echo "scale = 2; $x_720dp * 240 / 720" | bc)
echo " 1080dp : $x_1080"
echo "  960dp : $x_960"
echo "  840dp : $x_840"
echo "  720dp : $x_720dp"
echo "  600dp : $x_600"
echo "  480dp : $x_480"
echo "  360dp : $x_360"
echo "  240dp : $x_240"