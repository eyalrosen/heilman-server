# HTTP access to the Heilman simplifier

## Setup
Clone heilman repository into the same directory level:

    develop
        |
        + heilman
        + heilman-server


## Build
    gradle war

## Deployment

Copy build/libs/heilman-server.war, systemd/*.service, and heilman-master.zip
(the heilman repository zip file downloaded from github) to ubuntu@server.

Run the following commands on the server:

    apt-get update
    apt-get install tomcat8 unzip
    cp /home/ubuntu/*.service /lib/systemd/system
    cp /home/ubuntu/heilman-s/heilman-server.war /var/lib/tomcat8/webapps/
    cd /usr/share/tomcat8
    unzip /home/ubuntu/heilman-master.zip
    mv heilman-master/ heilman
    systemctl enable sst
    systemctl start sst
    systemctl status -l sst
    systemctl enable stanfordparser
    systemctl start stanfordparser
    systemctl status -l stanfordparser
    systemctl restart tomcat8

To test goto:

    http://server:8080/heilman-server/simplify?input=Danny+was+born+in+1992+and+loves+choclate
