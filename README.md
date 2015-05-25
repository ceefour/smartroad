# Smart Road

**Automatic Road Damage Detection**

Currently, measurement of road damage levels must be done using manual surveys which require a lot of personnel, time and money. We develop a technology that allow these measurements and related analysis to be done within minutes.

## Development Setup

### OpenCV

Ubuntu 14.04 / Linux Mint 17:

1. Install `libopencv2.4-jni` (works on Power too):

        sudo aptitude install libopencv2.4-java libopencv2.4-jni

2. Symlink `libopencv_java248.so`.
    For `x64`, while you can put it in `/usr/java/packages/lib/amd64` it's still easier and portable to just use `/usr/lib`.
    For `ppc64el`, `opencv_java248` will be looked from
    `/opt/ibm/java-ppc64le-80/jre/lib/ppc64le/compressedrefs:/opt/ibm/java-ppc64le-80/jre/lib/ppc64le:/usr/lib`

        sudo ln -sv /usr/lib/jni/libopencv_java248.so /usr/lib

### PostgreSQL

Linux:

    CREATE DATABASE smartroad_smartroad_dev TEMPLATE template0
        ENCODING 'UTF8' LC_COLLATE 'en_US.UTF8' LC_CTYPE 'en_US.UTF8';
    \c smartroad_smartroad_dev
    CREATE SCHEMA smartroad;

Windows:

    CREATE DATABASE smartroad_smartroad_dev TEMPLATE template0
        ENCODING 'UTF8';
    \c smartroad_smartroad_dev
    CREATE SCHEMA smartroad;
