# HTTP access to the Heilman simplifier

## Setup
Clone heilman repository into the same directory level:

    develop
        |
        + heilman
        + heilman-server


## Build
    gradle war

## To deploy
* Build the heilman project and copy the entire directory tree into /usr/share/tomcat8/heilman/.
* Copy war file into /var/lib/tomcat8/webapps/.
* Copy service files in the systemd subdirectory go into /lib/systemd/system/.
* systemctl enable sst
* systemctl enable stanfordparser
* systemctl start sst
* systemctl start stanfordparser
* systemctl start tomcat8
