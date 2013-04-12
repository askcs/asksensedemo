## Ask Sense Demo

A small demo app to test [Sense's Android plugin](https://github.com/senseobservationsystems/sense-android-library).

## Deploy APK

### IDE

There is no officially released APK of this demo App. So to install
this on your phone, clone this project:

```bash
git clone https://github.com/askcs/asksensedemo.git
```

import it in your [favorite IDE](http://www.jetbrains.com/idea/) and
run the App on your phone.

### Command line

Or from the command line, using Ant:

```bash
git clone https://github.com/askcs/asksensedemo.git
cd asksensedemo
```

Now create a file called `local.properties` that contains a single
key called `sdk.dir` pointing to your Android SDK. I.e.:

```
sdk.dir=/usr/local/android/sdk
```

Once that is done, execute the following target from the root of the
project:

```
ant debug install
```

The App should now be installed on your phone (if it was properly
connected, of course). Launch the App manually, it is called
`Ask Sense App`.