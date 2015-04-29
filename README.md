# Smart Road

Better roads.

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

