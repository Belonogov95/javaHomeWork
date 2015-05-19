package ru.ifmo.ctddev.belonogov.uifilecopy;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by vanya on 18.05.15.
 */
public class CalcSize extends SimpleFileVisitor<Path> {
    private long totalSize;

    public long getTotalSize() {
        return totalSize;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        totalSize = totalSize + new File(String.valueOf(file)).length();
        return super.visitFile(file, attrs);
    }
}
