#!/bin/sh

gradle build

cd packages
dpkg-buildpackage -tc -b -us -uc

