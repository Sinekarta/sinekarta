# EZUSB on Ubuntu #

Useful links :
> http://ubuntuforums.org/showthread.php?t=1677372


- connect the reader to usb port
- type command :
>> lsusb
check the device appears correctly as :
Castles Technology Co., Ltd EZUSB PC/SC Smart Card Reader


- install some usefull packages :
>> sudo apt-get install opensc openct pcscd pcsc-tools
>> sudo apt-get install libusb-dev libusb-0.1-4


- download the driver package from the manufacturer :
go to http://www.casauto.com.tw , select english
section Downloads
select PCSC smart card reader , ez100 series
download package : Linux(USupp... libusb...)


---> file già scaricati, a seconda dei casi:
> EZUSB\_Linux\_x86\_64\_v1.5.3.zip
> > oppure
> > EZUSB\_Linux\_x86\_v1.5.3.zip


- download the pcsc-lite package :
https://alioth.debian.org
get pcsclite 1.5.5 (this version)


---> la versione scaricabile dal sito non verrà installata, usare quella scaricata

> pcsc-lite\_1.8.3.orig.tar.bz2


- create a local directory "EZ100"
- unpack pcsc-lite & driver packages in this directory


- install pcsc-lite : move to the directory you created : pcsc-lite-1.5.5 , then type :
>> sudo ./configure --disable-libhal --enable-libusb
(maybe an additional option can be used : --enable-usbdropdir= , and after the = sign you paste the directory path where you will install the manuf drivers , see below)
>> sudo ./make
>> sudo ./make install


---> l'esecuzione di configure fallirà ancora
configure: error: install libudev-dev or use --disable-libudev


Installazione di libudev-dev, estratti da
> http://stackoverflow.com/questions/17181073/ubuntu-12-04-libudev-dev-wont-install-because-of-dependencies


-add repos to apt
>> sudo gedit /etc/apt/sources.list
> [add these two repos ](.md)
deb http://us.archive.ubuntu.com/ubuntu/ precise-updates main
deb-src http://us.archive.ubuntu.com/ubuntu/ precise-updates main


-refresh
>> sudo apt-get update
>> sudo apt-cache madison libudev-dev
> [should see libudev-dev 175-0ubuntu9.3 in the output](you.md)
libudev-dev | 175-0ubuntu9.3 | http://us.archive.ubuntu.com/ubuntu/ precise-updates/main amd64 Packages


-install the package
>> sudo apt-get install libudev-dev


Si riprende quindi l'installazione descritta da
> http://ubuntuforums.org/showthread.php?t=1677372


>> sudo ./configure --disable-libhal --enable-libusb
---> configure: error: You can't use libudev _and_ libusb. Select only one.


>> sudo ./configure --disable-libhal --enable-libusb --disable-libudev
---> configure: error: libusb.h not found, use ./configure LIBUSB\_CFLAGS=...


>> sudo ./configure --disable-libhal --disable-libusb
---> configure: WARNING: unrecognized options: --disable-libhal


>> sudo ./configure
---> OK


>> sudo ./make
---> sudo: ./make: comando non trovato


... vabbè, andiamo avanti...

...
...
...
Anche la guida di installazione stessa di pcsc-lite\_1.8.3 dice
[...]Briefly, the shell commands `./configure; make; make install' should configure, build, and install this package[...]
Ma questo comando make non esiste. Il driver invece contiene un comando install
...
...
...
La guida online cita
[...]unpack pcsc-lite & driver packages in this directory[...]
provo a copiare i rispettivi file in una stessa cartella ed ad eseguire da qui i comandi


>> sudo ./configure
>> sudo ./install
--->USB Device File System Ready!
> PC/SC Daemon Found!
> PC/SC Driver Location - /usr/lib/pcsc
> Reader Installation Successfully!
> Start PCS/SC Daemon Successfully.


- in the manufacturer package find the files :
ezusb.so
Info.plist

- in the pcsc-lite directory created before , create directories & copy files :
/pcsc-lite-1.5.5/drivers/
- (DIR) EZ100usb.bundle
--- (DIR) Contents
------ Info.plist
------ (DIR) Linux
--------- ezusb.so
--------- Info.plist


--->seguire le istruzioni, ma creare la struttura entro /usr/lib/pcsc (nel mio caso ospita già 2 driver)


- at this point normally pcsc-lite should have the needed files to access your EZ100PU smartcard reader hardware , so now let's try it :

- launch the pcsc-lite daemon :
for example to launch the daemon including debugging messages (so you can see live what is going on while you insert the reader / smart card etc) :
>> sudo pcscd -f -d &

- launch the scan of pcsc-lite :
>> sudo pcsc\_scan
then if it is working , you should get :

Scanning present readers...
0: CASTLES EZ100PU
Card state: Card inserted, etc etc ...


- if there is a problem , read the messages displayed from the pcsc-lite daemon



Attenzione!!!
è fondamentale aggiungere sudo alla chiamata dei comandi pcsc