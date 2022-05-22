#!/usr/bin/env bash
EO=$(mktemp)
wget "https://hg.sr.ht/~duangle/majoreo/raw/eo?rev=tip" -O $EO
chmod +x $EO

echo "y" | $EO import "https://raw.githubusercontent.com/ScopesCommunity/eo-packages/main/scopes-community.eo"
$EO import sdl2
$EO import wgpu
$EO import stb
$EO install -y sdl2 wgpu-native stb

BOTTLE_DIR=$(mktemp -d)
pushd $BOTTLE_DIR
wget "https://github.com/radgeRayden/bottle/archive/master.tar.gz"
tar -zxvf master.tar.gz
popd

mkdir -p ./lib/scopes/packages
mv -f $BOTTLE_DIR/bottle-main ./lib/scopes/packages/bottle
