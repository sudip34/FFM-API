package dev.sudipsaha;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
import java.nio.file.Path;

public class NativeImageProcessor {

    public static final Linker linker = Linker.nativeLinker();
    public static final SymbolLookup lookup = SymbolLookup.libraryLookup(Path.of("libs/image_inverter.dll"), Arena.global());

    static MethodHandle invertImageHandler;
    static MethodHandle tintImageHandler;

    static {
        var function = lookup.find("invert_image").orElseThrow();
        invertImageHandler = linker.downcallHandle(
                lookup.find("invert_image").orElseThrow(),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
        );

        tintImageHandler = linker.downcallHandle(
                function,
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,
                        ValueLayout.JAVA_INT,
                        ValueLayout.JAVA_BYTE,
                        ValueLayout.JAVA_BYTE,
                        ValueLayout.JAVA_BYTE)
        );
    }

    public static void invertImage(ByteBuffer buffer) throws Throwable {
        try(var arena = Arena.ofConfined()){
            MemorySegment memorySegment = arena.allocate(buffer.remaining());
            memorySegment.copyFrom(MemorySegment.ofBuffer(buffer));
            invertImageHandler.invoke(memorySegment, buffer.remaining());
            buffer.clear();
            buffer.put(memorySegment.asByteBuffer());
        }
    }

    public static void tintImage(ByteBuffer buffer, byte rScale, byte gScale, byte bScale) throws Throwable {
        try (var arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(buffer.remaining());
            segment.copyFrom(MemorySegment.ofBuffer(buffer));
            tintImageHandler.invoke(segment, buffer.remaining(), rScale, gScale, bScale);
            buffer.clear();
            buffer.put(segment.asByteBuffer());
        }
    }
}
