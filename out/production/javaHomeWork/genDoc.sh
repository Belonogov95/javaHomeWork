#!/usr/bin/env bash
#javadoc -d R ru.ifmo.ctddev.belonogov.implementor


mcp="-classpath ../java-advanced-2015/artifacts/*:.:../java-advanced-2015/lib/*"
linkkkk="-linkoffline http://docs.oracle.com/javase/8/docs/api/ http://docs.oracle.com/javase/8/docs/api/"
sb="-subpackages info.kgeorgiy.java.advanced.implementor"
package="ru.ifmo.ctddev.belonogov.implementor"


javadoc $linkkkk -d doc $package $mcp $sb -private
#-classpath ~/Documents/prog/Java/java-advanced-2015/artifacts/:.
#javadoc -d doc 
#-classpath ~/Documents/prog/Java/java-advanced-2015/artifacts/
#-sourcepath ru.ifmo.ctddev.belonogov.implementor

#/usr/lib/jvm/jdk1.8.0_20/bin/javadoc $mcp $link $sb -private -author $package -d doc

#./src/ru/ifmo/ctddev/peresadin/* 
#-subpackages info.kgeorgiy.java.advanced.implementor 

#-sourcepath ./src/ -subpackages info.kgeorgiy.java.advanced.implementor -d ./doc
#/usr/lib/jvm/jdk1.8.0_20/bin/javadoc
#-classpath ImplementorTest.jar:../libs/*
#./src/ru/ifmo/ctddev/peresadin/* 
#-sourcepath ./src/
#-d ./doc





