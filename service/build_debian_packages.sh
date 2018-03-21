#!/bin/sh
cd packages
dpkg-buildpackage -tc -b -us -uc
