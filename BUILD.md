# Building the plugin

These instructions will be cleaned up later.

## Building the Java code
You need:
* jdk-21 (e.g. `sudo apt install openjdk-21-jdk`)
* maven (e.g. `sudo apt install maven`)

Note tests are currently not fully working.

To buid:
```
cd imagej
mvn -Denforcer.skip -Dmaven.test.skip=true install
```

## Building the C++ code

### Dependencies

You will need to download:
- OpenCV (e.g. 4.12)
- NLopt (e.g. 2.7.1)
- Diplib (e.g. 3.6.0)


Currently this is only tested/working on ubuntu variants. You will need to
apt-install a bunch of stuff. This may be incomplete. You will need to install:

- A recent C++ compiler (e.g. gcc 13, 11 is too old)
- cmake
- ninja


#### Building the dependcies

First create a directory to install the built libraries to:

```
mkdir -p $HOME/tmp/hawkmanbuild
```

##### Building OpenCV

The plugin runs the hawkman executable and communicates by passing TIFFs back
and forth. So, build a static OpenCV with just what we need and no more built
in. Note only the modules we actually use (i.e. imgcodecs) are configured here. 


Uncompress the OpenCV source code, and go into the directory, Run:

```
mkdir build
cd build
cmake \
	-DBUILD_SHARED_LIBS=OFF \
	-DENABLE_PIC=ON \
	-DCMAKE_INSTALL_PREFIX=$HOME/tmp/hawkmanbuild \
	-DWITH_PNG=OFF \
	-DWITH_JPEG=OFF \
	-DWITH_WEBP=OFF \
	-DWITH_JASPER=OFF \
	-DWITH_OPENJPEG=OFF \
	-DWITH_OPENEXR=OFF \
	-DWITH_JPEGXL=OFF \
	-DBUILD_TIFF=ON \
	-DWITH_IMGCODEC_HDR=OFF \
	-DWITH_IMGCODEC_SUNRASTER=OFF \
	-DWITH_IMGCODEC_PXM=OFF \
	-DWITH_IMGCODEC_PFM=OFF \
	-DWITH_IMGCODEC_GIF=OFF \
	-DWITH_PROTOBUF=OFF \
	-DWITH_ADE=OFF \
	-DWITH_EIGEN=OFF \
	-DBUILD_LIST=core,imgproc,imgcodecs \
	-GNinja \
	..
ninja install
```

##### Building nlopt

Uncmpress the nlopt source code, go into the directory and run:

```
mkdir build
cd build
cmake \
	-DBUILD_SHARED_LIBS=OFF \
	-DCMAKE_INSTALL_PREFIX=$HOME/tmp/hawkmanbuild \
	-GNinja \
	..
ninja install
```


##### Building DIPLib

Diplib is not built, untar it in the right place:

```
cd $HOME/tmp/hawkmanbuild
tar -xf ~/Downloads/diplib-3.6.0.tar.gz

```


### Building hawkman exes

From the hawkman root:


```
cd cpp
mkdir build
cd build
cmake  \
	-DCMAKE_BUILD_TYPE=Release  \
	-DDIPLIB_DIR=$HOME/tmp/hawkmanbuild/diplib-3.6.0/  \
	-DCMAKE_PREFIX_PATH=$HOME/tmp/hawkmanbuild/ \
	-DCMAKE_CXX_COMPILER=/opt/gcc-13.2.0/bin/g++-13.2.0  \
	-DDIP_BUILD_JAVAIO=OFF  \
	-DDIP_BUILD_DIPIMAGE=OFF  \
	-DDIP_BUILD_DIPVIEWER=OFF  \
	-DDIP_BUILD_PYDIP=OFF  \
	-DDIP_SHARED_LIBRARY=OFF  \
	-DDIP_ENABLE_DOCTEST=OFF    \
	../tools/ 
```

