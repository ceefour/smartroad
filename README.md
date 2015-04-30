# Smart Road

**Automatic Road Damage Detection**

Currently, measurement of road damage levels must be done using manual surveys which require a lot of personnel, time and money. We develop a technology that allow these measurements and related analysis to be done within minutes.

## Development Setup

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
