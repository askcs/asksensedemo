## Ask Sense Demo

A small demo app to test [Sense's Android plugin](https://github.com/senseobservationsystems/sense-android-library).

## Deploy APK

### IDE

There is no officially released APK of this demo App. So to install
this on your phone, clone the branch `getdata-fix` of the Sense Android 
Library, and clone this project too:

```bash
git clone https://github.com/bartkiers/sense-android-library -b getdata-fix
git clone https://github.com/askcs/asksensedemo.git
```

import it in your [favorite IDE](http://www.jetbrains.com/idea/) <sup>*</sup> and
run the App on your phone.

### Command line

Or from the command line, using Ant:

```bash
git clone https://github.com/bartkiers/sense-android-library -b getdata-fix
git clone https://github.com/askcs/asksensedemo.git
cd asksensedemo
```

Now create a file called `local.properties` that contains a single
key called `sdk.dir` pointing to your Android SDK. I.e.:

```
sdk.dir=/usr/local/android/sdk
```

Once that is done, execute the following target from the root of the
`asksensedemo` project:

```
ant debug install
```

The App should now be installed on your phone (if it was properly
connected, of course). Launch the App manually, it is called
`Ask Sense App`.

## Run the App

To run the App, you need a Common-Sense account. If you haven't got one, register
one here: https://accounts.sense-os.nl/

## Configure state sensors

`TODO` 

<br>
---------------------------------------------

<sup>*</sup> `;)`
