package ru.ifmo.ctddev.belonogov.uifilecopy;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by vanya on 19.05.15.
 */
public class CopyTree extends SimpleFileVisitor <Path>  {
    private final static int BUFFER_SIZE = 1024;
    private final Path source;
    private final Path target;

    public CopyTree(Path target, Path source) {
        this.target = target;
        this.source = source;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Path newDir =  target.resolve(source.relativize(dir));
        assert(new File(String.valueOf(newDir)).mkdir());
        return super.preVisitDirectory(dir, attrs);
    }


    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path destination = target.resolve(source.relativize(file));
        try (InputStream input = new FileInputStream(String.valueOf(file));
             OutputStream output = new FileOutputStream(String.valueOf(destination))) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = input.read(buffer)) > 0) {
                output.write(buffer, 0, len);
            }
        }
        return super.visitFile(file, attrs);
    }
}
