# JEXTRACT

```yaml

//in powershell
 & "C:\Users\sudip\Downloads\JAVA\FFM-API\src\main\resources\jextract.bat" -t myclib -l myclib -d out mylib.h 


& "<qualified-path>\jextract.bat" 
```



```
// $ jextract --output classes \
//   --target-package org.stdlib /usr/include/stdlib.h
```

### how to use
```declarative

import static org.stdlib.stdlib_h.*;
...

try(Arena offHeap = Areana.ofConfinded()){
    MemorySegment array = offHeap.allocateFrom(C_INT, 0, 1,2,8,5,6,7, 9,3,4);

    var compareFunc = allocate((a1,a2) -> 
        Integer.compare(a1.get(C_INT,0), a2.get(C_INT,0)),offHeap);
    qsort(array, 10L, compareFunc);
    int[] sorted = array.toArray(JAVA_INT);

}

```


# install gcc with mingw
``` 
choco install mingw -y

gcc --version
```

#compile and build `.dll` from native code 

```
gcc -shared -o <image_inverter>.dll -DBUILD_DLL <image_inverter>.c

```
# build and run with maven

``` 
 mvn clean jaavfx:run
```





---
 FFM
===

# Accessing Flat Memory with

## Accessing Native/Heap memory
### Two kinds of Memory segment
1. Heap Segment: Access to memory inside the JAVA HEAP. (e.g. : a java array)
2. Native Segment: Access to memory outside the JAVA HEAP. (e.g. malloc or mmap[linux])

### Access of all memory is governed by the following characteristics
- Size: No `out-of-bounds-access`
- Lifetime: No `use-after-free` access
- Thread Confinement: No data races(optional)


#### example: MemorySegment

Mechanical or manual allocation:

```
// struct Point2d {
//    double x;
//    double y;
// } point = {3.0, 4.0}

MemorySegment point = Arena.ofAuto().allocate(8*2);  // double is 8 byte

pont.set(ValueLayout.JAVA_DOUBLE, 0, 3d);         // set the vlaue 3.0 at the index 0
pont.set(ValueLayout.JAVA_DOUBLE, 8, 4d);         // set the vlaue 4.0 at the index 8



point.get(ValueLayout.JAVA_DOUBLE, 16); // woops  Exception IndexOutOfBoundException: Out of bound access on memory segment at offset 16
```

### Managing Memory in Java with Arena

***Often regions of memory point to one another***
- Memory Freed too-early ->use-after free
- Memory never freed -> memory leak

***C's `malloc/free` is too low-level***
- Every allocation has a fresh lifetime (aka "lifetime soup")
- Pointer can be freed any time

***Rust is (a lot) safer, but***
- Ownership & borrowing require language support
- Programming model can be inflexible (e.g. cyclic data structures)

**Arena:**
- An `Arena` models the lifecycle of one or more memory segments
- All `segments` allocated in the `Arena` the same lifetime
- All `Arenas` provide safety guaranties(no use-after-free)
- The `Close` operation of an `Arena` is an `Atomic` operation. 
- There are several kinds on `Arena` with different `deallocation/access policies`

### Arena Types

| Type     | Lifetime              | Access          |
|----------|-----------------------|-----------------|
| Global   | Unbound               | Multi-Threaded  |
| Auto     | Automatic(Gc)         | Multi-Threaded  |
| Confined | Explicitly bounded    | Single-Threaded |
| Sheared  | Explicitly bounded(*) | Multi-Threaded  |
| Custom   | ...                   | ...             |

* closing a shared arena triggers a `thread-local handshake` (JEP  312)


### Arean with a auto-closable

```
// struct Point2d {
//    double x;
//    double y;
// } point = {3.0, 4.0}

try ( Arena offHeap = Arena.ofConfined()) {
    MemorySegment point = offHeap.allocate(8*2);  // double is 8 byte

    pont.set(ValueLayout.JAVA_DOUBLE, 0, 3d);         // set the vlaue 3.0 at the index 0
    pont.set(ValueLayout.JAVA_DOUBLE, 8, 4d);         // set the vlaue 4.0 at the index 8
} //free

```
pros: 
- deterministic deallocation
- No `out-of-bounds` access
- No `use-after-free` access

cons:
- Manual offset computation


### Memory Layout   
***_to tackle_ ` manual offset computation`***

- ***Memory Layout*** describes contents of memory region `Programatically`
- `Layouts` can queried to obtain sizes, alignments, name and access expressions.
- More declarative code, less places for bugs to hide

```
// struct Point2d {
//    double x;
//    double y;
// } point = {3.0, 4.0}

MemeoyLayout POINT_2D = MemoryLayout.structLayout(
    ValueLayout.JAVA_DOUBLE.withName("x");
    ValueLayout.JAVA_DOUBLE.withName("y");
);
static final VarHandle XH = POINT_2D.varHandle(PathElement.grouptLayout("x"));  //create varHandle
static final VarHandle YH = POINT_2D.varHandle(PathElement.grouptLayout("y"));  //create varHandle

try ( Arena offHeap = Arena.ofConfined()) {
    MemorySegment point = offHeap.allocate(8*2);  // double is 8 byte

    XH.set(point 0L, 3d);         // set the vlaue 3.0 
    YH.set(point, 0L, 4d);         // set the vlaue 4.0 
} //free

```


***ValueLayout***:

Accessing memory segment needs three ingredients, all of which are packaged in a value layout
- carrier type -> the type of the `java value` to read/write (e.g. double)
- endianness -> whether the dereference operation should swap bytes (e.g. little endian for arm/ big endian for network/ amd)
- alignment -> the alignment constraint of the address being dereferenced (e.g. 8 for double)

### VarHandle
***`Value Layouts` can be used to obtain a MemorySegments `VarHandle`***
- Accepts a MemorySegment and a long as coordinates
- `VarHandle`s for nested value layouts are derived from a group layout, using a layout path


## Call a Native Function   : example

``` 
// extern double distance(struct Point2d p);


  MemorySegment distanceAddress = SymbolLookup.loaderLookup().findOrThrow("distance");

  MethodHandle distanceHandle = Linker.nativeLinker().downcallHandle(distancedAddress, FunctionDescriptor.of(JAVA_DOUBLE, POINT_2D));

  try ( Arena ofHeap = Arena.ofConfined()) {
    MemorySegment point = ofHeap.allocate(POINT_2D);  // double is 8 byte

    XH.set(point 0L, 3d);         // set the vlaue 3.0 
    YH.set(point, 0L, 4d);         // set the vlaue 4.0 
    double dist = (double)distanceHandle.invokeExact(point); //5d
 } //free
```