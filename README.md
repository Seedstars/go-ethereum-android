# Go Ethereum Android

## Readme Notes

* Command line starts with $, the command should run with user privileges
* Command line starts with #, the command should run with root privileges

## Installation

### Go

* [Download the archive](https://golang.org/dl/) and extract it into /usr/local, creating a Go tree in /usr/local/go. For example:

     `$ tar -C /usr/local -xzf go$VERSION.$OS-$ARCH.tar.gz`
* Add /usr/local/go/bin to the PATH environment variable. You can do this by adding this line to your /etc/profile (for a system-wide installation) or $HOME/.profile:

     `export PATH=$PATH:/usr/local/go/bin`

* More info [here](https://golang.org/doc/install).

### JAVA

* `# apt-get install openjdk-7-jdk`

### Android Studio

* Install [Android Studio](http://developer.android.com/sdk/index.html?gclid=COvh8-TPzsgCFdQaGwodS00L-A)

### Code from repository

* `$ git clone x`

### Before Run

* Before you run Go Ethereum Android you need to do this steps:
* `$ go get -u github.com/karalabe/xgo`
* `$ xgo --deps=https://gmplib.org/download/gmp/gmp-6.0.0a.tar.bz2
         --remote=https://github.com/karalabe/go-ethereum               
         --branch=andorid-path-fix                                      
         --targets=android-16/arm                                       
         --pkg=cmd/geth                                                 
         github.com/ethereum/go-ethereum`
* `$ mv geth-android-16-arm {path_local_repository location}/app/main/res/raw/geth`

## Build

Open the project on Android Studio and build it, please note that to build and run the app it is possible that Android Studio 
will ask you to install dependencies.

## Running

* To start the service use the switch on header
* Use the keyboard icon to show/hide the keyboard
* To see previous commands press the physical button 'Volume Up' and then insert 'W'
* The first page displays the output from Go Ethereum Console, the keyboard can be used to interact with it
* Swiping right will change to the view with the debug window (shows the Err output from Go Ethereum Console)
* To stop the service use the switch on status bar

## Help on Geth Console
* Check official Github of [Go-Ethereum](https://github.com/ethereum/go-ethereum/)

## Thanks
* Thanks to Karalabe (Péter Szilágyi) for the help


## Autor
* Fernando Ferreira, Seedstars
