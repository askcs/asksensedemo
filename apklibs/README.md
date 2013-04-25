## How to create a .apklib file

Put the following files and folders into a zip file called, for example, 
`ProjectName-1.0-SNAPSHOT.zip`:

```
ProjectName-1.0-SNAPSHOT.zip
 |
 |`- src/
 |`- res/
 |`- AndroidManifest.xml
  `- project.properties
```

and then rename the zip file:

```
mv ProjectName-1.0-SNAPSHOT.zip ProjectName-1.0-SNAPSHOT.apklib
```

## Adding and .apklib file to your local maven repository

To add the previously created apklib to your local maven repository, execute
the following on your shell:

```
mvn install:install-file \
    -Dfile=ProjectName-1.0-SNAPSHOT.apklib \
    -DgroupId=com.askcs \
    -DartifactId=project-name \
    -Dversion=1.0-SNAPSHOT \
    -Dpackaging=apklib
```

In your `pom.xml`, you can now use the dependency like this:

```xml
<dependency> 
    <groupId>com.askcs</groupId> 
    <artifactId>project-name</artifactId> 
    <version>1.0-SNAPSHOT</version> 
    <type>apklib</type> 
</dependency>
```

--------------------------------------------------------------------------

More info, see: https://code.google.com/p/maven-android-plugin/wiki/ApkLib
