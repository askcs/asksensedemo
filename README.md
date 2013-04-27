## Ask Sense Demo

A small demo app to test 
[Sense's Android plugin](https://github.com/senseobservationsystems/sense-android-library).

## Setup local environment

### 0. Prerequisites

You need to have the following installed on your machine:

* Git
* Maven 3.0.3+
* Andriod SDK

Install all SDK's and extra libraries through the Android SDK manager. Ensure 
that at the very least SDK v4.2 is installed:

```
ls ~/.m2/repository/android/android/
```

should list at least the folder `4.2.2_r2`

Put these SDK's and libraries in your local Maven repository using the 
*Maven Android SDK Deployer*:

``` 
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
