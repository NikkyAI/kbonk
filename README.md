kbonk

## how to run

### required dependencies

the software needs the magickwand library and headers from imagemagick

```
pacman -S imagemagick
```

### build binary

```bash
./gradlew linkReleaseExecutableLinuxX64
cp build/bin/linuxX64/debugExecutable/kbonk.kexe kbonk
```

### required data

//TODO: configure via commanline options
the binary expects the relative paths `data/bonk.jpg` and `data/OpenSans-Semibold.ttf` to exist


### run server

```
./bonk serve
```