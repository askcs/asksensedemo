## Ask Sense Demo  &nbsp; [![Build Status](https://travis-ci.org/askcs/asksensedemo.png)](https://travis-ci.org/askcs/asksensedemo)

A small demo app to test 
[Sense's Android plugin](https://github.com/senseobservationsystems/sense-android-library).

Download a pre-built APK [here](https://github.com/askcs/asksensedemo/blob/master/asksensedemo-1.0-SNAPSHOT-release.apk?raw=true), 
or follow the steps below to build your own.


## Setup local environment

### 0. Prerequisites

You need to have the following installed on your machine:

* Git
* Maven 3.0.3+
* Andriod SDK

Google's most recent SDK(s) and libraries, like GCM, are not in public Maven
repositories. You'll need to install them via Google's SDK manager, and then
put them in your local Maven repository. Start the SDK manager by doing:

```bash
$ANDROID_HOME/tools/android sdk
```

*(this may take a while, [go duel](http://xkcd.com/303/))*

Put the SDK's and libraries in your local Maven repository using the
*Maven Android SDK Deployer*:

```bash
git clone https://github.com/mosabua/maven-android-sdk-deployer.git
cd maven-android-sdk-deployer
mvn install
```

More info, see: https://github.com/mosabua/maven-android-sdk-deployer

### 1. Clone this demo

Clone this project: 

```bash
git clone https://github.com/askcs/asksensedemo.git
```

### 2. Create a local.properties

Create `local.properties` inside the root of the project and add a single key to 
it, `sdk.dir`, pointing to your local installation of the Android SDK:

```
sdk.dir=/path/to/your/android/sdk
```

### 3. Add Sense APKLIB 

Add the Sense APKLIB to your local Maven repository:

```bash
git clone https://github.com/bkiers/sense-android-library.git -b maven-apklib
cd sense-android-library
mvn clean install
```

### 4. Test

To test your setup, either run the unit tests:

```
mvn clean test
```

or deploy the App on your phone (given it is properly connected):

```
mvn install android:deploy
```

To deploy *and* run the App, simply do:

```
mvn install android:deploy android:run
```

### 5. IDE integration

#### 5.1. IntelliJ

* open project
* select `pom.xml`

#### 5.2. Eclipse

* see: http://rgladwell.github.io/m2e-android
