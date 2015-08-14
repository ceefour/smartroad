# Smart Road

**Automatic Road Damage Detection**

Currently, measurement of road damage levels must be done using manual surveys which require a lot of personnel, time and money. We develop a technology that allow these measurements and related analysis to be done within minutes.

## Development Setup

### OpenCV

#### Ubuntu 14.04 / Linux Mint 17

1. Install `libopencv2.4-jni` (works on Power too):

        sudo aptitude install libopencv2.4-java libopencv2.4-jni

2. Symlink `libopencv_java248.so`.
    For `x64`, while you can put it in `/usr/java/packages/lib/amd64` it's still easier and portable to just use `/usr/lib`.
    For `ppc64el`, `opencv_java248` will be looked from
    `/opt/ibm/java-ppc64le-80/jre/lib/ppc64le/compressedrefs:/opt/ibm/java-ppc64le-80/jre/lib/ppc64le:/usr/lib`

        sudo ln -sv /usr/lib/jni/libopencv_java248.so /usr/lib

#### Windows 64-bit

OpenCV for Windows x64 DLL is needed, dan sudah dimasukkan ke git juga biar gampang.

Copy `opencv\win_x64\opencv_java*.dll` DLL tersebut ke `C:\ProgramData\Oracle\Java\javapath`

_Hendy's internal note:_ The `org.opencv:opencv` artifact is published in `soluvas-public-thirdparty`.
You can re-publish (if you update the OpenCV version) to `soluvas-thirdparty` using:

```
mvn deploy:deploy-file -DrepositoryId=soluvas-public-thirdparty -Durl=http://nexus.bippo.co.id/nexus/content/repositories/soluvas-public-thirdparty/ -Dfile=opencv/opencv-2411.jar -Dpackaging=jar -DgroupId=org.opencv -DartifactId=opencv -Dversion=2.4.11
```

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

## Dump Database to Server

Dump it:

    pg_dump -hlocalhost -Upostgres -Fc -f ~/tmp/smartroad_smartroad_dev.postgresql smartroad_smartroad_dev

Important: While you're at it, backup the snapshot to `/media/ceefour/passport/project_passport/smartroad/snapshot`

Rsync to server:

    rsync -P ~/tmp/smartroad_smartroad_dev.postgresql ceefour@luna3:

The restore fully to `smartroad_smartroad_prd` : (WARNING: THIS WILL WIPE THE PRD DATABASE!)

    pg_restore -h nobunaga2.bippo.co.id -Usmartroad_smartroad_prd -d smartroad_smartroad_prd --clean --no-owner smartroad_smartroad_dev.postgresql 


## Production Setup

Production details are in:

* `Dropbox/helpfriend/.../SmartRoad DevOps.odt`
* `Dropbox/helpfriend/.../prd/application.properties`

### OpenCV on Ubuntu 14.04

1. Install `libopencv2.4-jni` (works on Power too):

        sudo aptitude install libopencv2.4-java libopencv2.4-jni

2. Symlink `libopencv_java248.so`.
    For `x64`, while you can put it in `/usr/java/packages/lib/amd64` it's still easier and portable to just use `/usr/lib`.
    For `ppc64el`, `opencv_java248` will be looked from
    `/opt/ibm/java-ppc64le-80/jre/lib/ppc64le/compressedrefs:/opt/ibm/java-ppc64le-80/jre/lib/ppc64le:/usr/lib`

        sudo ln -sv /usr/lib/jni/libopencv_java248.so /usr/lib

### PostgreSQL

    CREATE USER smartroad_smartroad_prd PASSWORD '***';
    CREATE DATABASE smartroad_smartroad_prd TEMPLATE template0 OWNER smartroad_smartroad_prd
        ENCODING 'UTF8' LC_COLLATE 'en_US.UTF8' LC_CTYPE 'en_US.UTF8';
    \c smartroad_smartroad_prd
    CREATE SCHEMA smartroad AUTHORIZATION smartroad_smartroad_prd;

Restore from `dev` snapshot: see **Dump Database to Server** above. 

### Deploy

Run `build-deploy-prd.sh`
