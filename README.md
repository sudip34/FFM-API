# JEXTRACT

```yaml

//in powershell
 & "C:\Users\sudip\Downloads\JAVA\FFM-API\src\main\resources\jextract.bat" -t myclib -l myclib -d out mylib.h 


& "<qualified-path\jextract.bat" 
```


# install gcc with mingw
``` 
choco install mingw -y

gcc --version
```

#compile and build .dll from native code 

```
gcc -shared -o <image_inverter>.dll -DBUILD_DLL <image_inverter>.c

```
# build and run with maven

``` 
 mvn clean jaavfx:run
```


