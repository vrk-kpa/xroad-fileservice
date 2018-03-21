#!/bin/bash
docker build -q -t docker-rpmbuild packages/docker
docker run --rm -v $(pwd)/..:/workspace -v /etc/passwd:/etc/passwd:ro -v /etc/group:/etc/group:ro -u $(id -u):$(id -g) docker-rpmbuild sh -c 'cd /workspace/service && ./build_rhel_packages.sh'