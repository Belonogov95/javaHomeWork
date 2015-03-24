package ru.ifmo.ctddev.belonogov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;
import javafx.util.Pair;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class Main implements JarImpler {
    HashSet<Class> visited;
    File sourceFile;
    int cur;

    public static void main(String[] args) throws ImplerException {
        new Main().run(C.class, new File("."));
        if (true) return;
        System.out.println(String.class);
        if (args.length != 1 || args[0] == null)
            throw new ImplerException();
        try {
            System.out.println(args[0]);
            new Main().run(Class.forName(args[0]), new File("."));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
             destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    ArrayList<Method> recDeclared(Class position) {
        ArrayList<Method> result = new ArrayList<>();
        if (position.isInterface() || position.equals(Object.class)) {
            return result;
        }
        Method[] methods = position.getDeclaredMethods();
        for (Method method : methods) {
            if (Modifier.isAbstract(method.getModifiers())) {
                continue;
            }
            result.add(method);
        }
        result.addAll(recDeclared(position.getSuperclass()));
        return result;
    }

    ArrayList<Method> recImplemented(Class position) {
        ArrayList<Method> result = new ArrayList<>();
        if (position == null) {
            return result;
        }
        visited.add(position);
        Method[] methods = position.getDeclaredMethods();
        boolean flagInterface = position.isInterface();
        for (Method method : methods) {
            if (flagInterface) {
                result.add(method);
            } else {
                if (Modifier.isAbstract(method.getModifiers())) {
                    result.add(method);
                }
            }
        }
        Class[] interfaces = position.getInterfaces();
        for (Class inter : interfaces) {
            if (!visited.contains(inter)) {
                result.addAll(recImplemented(inter));
            }
        }
        if (!visited.contains(position.getSuperclass())) {
            result.addAll(recImplemented(position.getSuperclass()));
        }
        return result;
    }

    public String generateParameterString(Type[] args, boolean isVarArgs) {
        String result = "(";
        cur = 0;
        for (int i = 0; i < args.length - 1; i++) {
            Type arg = args[i];
            if (cur > 0) {
                result += ", ";
            }
            result += arg.getTypeName() + " a" + cur;
            cur++;
        }
        if (args.length > 0) {
            if (isVarArgs) {
                Type arg = args[args.length - 1];
                if (cur > 0) {
                    result += ", ";
                }
                String tmpS = arg.getTypeName();
                result += tmpS.substring(0, tmpS.length() - 2) + " ... a" + cur;
            } else {
                Type arg = args[args.length - 1];
                if (cur > 0) {
                    result += ", ";
                }
                result += arg.getTypeName() + " a" + cur;
            }
            cur++;
        }
        result += ")";
        return result;
    }

    public Pair<String, Pair<String, Type>> makeStringFromMethod(Method method) {
        //method.getGenericParameterTypes()
                //method.getGenericReturnType();
        String access = "";
        String result = "";

        if (Modifier.isProtected(method.getModifiers())) {
            access = "protected ";
        }
        if (Modifier.isPublic(method.getModifiers())) {
            access = "public ";
        }

        result += method.getName();
        result += generateParameterString(method.getGenericParameterTypes(), method.isVarArgs());
        result += " {";
        if (method.getReturnType().equals(void.class)) {
            result += String.format("}");
        } else {
            result += String.format("%n        return");
            if (method.getReturnType().isPrimitive()) {
                if (method.getReturnType().equals(boolean.class)) {
                    result += " false";
                }
                else {
                    result += " 0";
                }
            } else {
                result += " null";
            }
            result += String.format(";%n    }");
        }
        //method.getGenericReturnType();

        return new Pair<>(result, new Pair<>(access, method.getGenericReturnType()));

    }


    void run(Class<?> token, File root) throws ImplerException {
        String className = token.getCanonicalName();
        token.getCanonicalName();
        String path = "";
        try {
            path = root.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        path = path + File.separator + className.replace('.', File.separatorChar);
        File file = new File(path);
        File file2 = file.getParentFile();
        file2.mkdirs();
        sourceFile = new File(path.toString() + "Impl.java");
        try (PrintWriter out = new PrintWriter(sourceFile, "UTF-8")) {
            System.out.println(sourceFile);
            String myClassName = token.getSimpleName() + "Impl";
            out.println("package " + token.getPackage().getName() + ";");
            out.println();
            out.print("public class " + myClassName + " ");

            if (token.isInterface()) {
                out.print("implements ");
            }
            else {
                out.print("extends ");
            }
            out.print(token.getName());
            out.println(" {");

            if (!token.isInterface()) {
                Constructor<?>[] constructors = token.getDeclaredConstructors();
                int cntImpl = 0;
                for (Constructor constructor : constructors) {
                    out.print("    ");
                    if (Modifier.isPublic(constructor.getModifiers())) {
                        out.print("public ");
                    }
                    if (Modifier.isProtected(constructor.getModifiers())) {
                        out.print("protected ");
                    }
                    if (Modifier.isPrivate(constructor.getModifiers())) {
                        continue;
                    }
                    cntImpl++;
                    out.print(myClassName + " ");
                    out.print(generateParameterString(constructor.getGenericParameterTypes(), constructor.isVarArgs()));
                    Class[] exceptions = constructor.getExceptionTypes();
                    if (exceptions.length > 0) {
                        out.print(" throws ");
                    }
                    int cnt = 0;
                    for (Class exception: exceptions) {
                        if (cnt > 0) {
                            out.print(", ");
                        }
                        out.print(exception.getCanonicalName());
                        cnt++;
                    }
                    out.println(" {");
                    out.print("        " + "super(");
                    for (int i = 0; i < cur; i++) {
                        if (i > 0) {
                            out.print(", ");
                        }
                        out.print("a" + i);
                    }
                    out.println(");");
                    out.println("    }");
                }
                if (constructors.length > 0 && cntImpl == 0) {
                    throw new ImplerException("no not private constructor");
                }

            }
            ArrayList<Method> declaredMethods = recDeclared(token);
            visited = new HashSet<>();
            ArrayList<Method> implementedMethods = recImplemented(token);

            HashMap<String, Pair<String, Type >> forImplementation = new HashMap<>();

            for (Method method : implementedMethods) {
                Pair<String, Pair<String, Type>> result = makeStringFromMethod(method);
                if (!forImplementation.containsKey(result.getKey())) {
                    forImplementation.put(result.getKey(), result.getValue());
                }
                else {
                    Type oldClass = result.getValue().getValue();
                    Type newClass = forImplementation.get(result.getKey()).getValue();
                    if (((Class)newClass).isAssignableFrom((Class)oldClass)) {

                        forImplementation.remove(result.getKey());
                        forImplementation.put(result.getKey(), result.getValue());
                    }
                }
            }
            for (Method method : declaredMethods) {
                Pair<String, Pair<String, Type>> result = makeStringFromMethod(method);
                if (forImplementation.containsKey(result.getKey()) &&
                        forImplementation.get(result.getKey()).getValue().equals(result.getValue().getValue())) {
                    forImplementation.remove(result.getKey());
                }
            }

            for (Map.Entry<String, Pair<String, Type>> s : forImplementation.entrySet()) {
                out.print("    ");
                out.print(s.getValue().getKey());
                Type returnType = s.getValue().getValue();
                if (returnType.getTypeName().equals("T")) {
                    out.print("<T>");
                }
                out.print(s.getValue().getValue().getTypeName() + " ");
                out.print(s.getKey());
                out.println();
            }

            out.println("}");

        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        File destFile = new File("./a" + sourceFile);
//        new File(destFile.getParent()).mkdirs();
//        try {
//            copyFile(sourceFile, destFile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void implement(Class<?> aClass, File file) throws ImplerException {
        if (aClass == null || aClass.isPrimitive() || Modifier.isFinal(aClass.getModifiers()) || file == null) {
            throw new ImplerException(String.format("usage: Class - not null, not primitive, not final; %n" +
                    "File - not null"));
        }
        run(aClass, file);
    }

    @Override
    public void implementJar(Class<?> aClass, File file) throws ImplerException {
        implement(aClass, new File("."));
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String path = sourceFile.getAbsolutePath();
        if (compiler.run(null, null, null, path) != 0) {
            throw new ImplerException();
        }
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        String fileS = path;
        File pathToClass= new File(fileS.substring(0, fileS.length() - 4) + "class");
//        File pathToJar = new File(".");
        try (InputStream input = new BufferedInputStream(new FileInputStream(pathToClass))) {





        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
