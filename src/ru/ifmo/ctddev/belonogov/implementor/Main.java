package ru.ifmo.ctddev.belonogov.implementor;

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
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * The class represent implementation of interface {@link info.kgeorgiy.java.advanced.implementor.JarImpler} interface.
 * It can generate implementation (the <code>.java</code> file) for classes or interfaces except generic classes.
 * New class args consist of the args class that is implemented plus "Impl" suffix.
 * This class can generate <code> .jar </code> file with generated class.
 * <p>
 * This class can throw {@link info.kgeorgiy.java.advanced.implementor.ImplerException}  if it's impossible
 * to generate implementation.
 *
 * @author Ivan Belonogov
 */


public class Main implements JarImpler {
    /**
     * This {@link java.util.HashSet} contain information about visited classes. When
     * {@link #recImplemented} method search methods which need to be implemented.
     */
    private HashSet<Class> visited;

    /**
     * After invocation {@link #implement} this {@link java.io.File} contains link to file with implementaion.
     */
    private File sourceFile;

    /**
     * Contain the number of arguments in current method or constructor
     */

    private int countArgs;


    /**
     * Invokes {@link #implement(Class, java.io.File)} with parameters
     * passed through command line.
     *
     * @param args              arguments from command line
     * @throws ImplerException  when class can't be implemented (final, primitive, all constructors are private)
     */

    public static void main(String[] args) throws ImplerException {
        new Main().implement(A.class, new File(""));
        if (true) return;
        if (args.length == 1 && args[0] != null)
            try {
                new Main().implement(Class.forName(args[0]), new File(""));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        else  if (args.length == 3 && args[0].equals("-jar") && args[1] != null && args[2] != null){
            try {
                new Main().implementJar(Class.forName(args[1]), new File(args[2]));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        else
            System.err.println("Usage: -jar <class args>  <file.jar>  or <class args>");
    }

    /**
     * Traverses all interfaces that implements or classes that extends {@link java.lang.Class}
     * position and search all declared methods
     * @param position      class from which to start search
     * @return              ArrayList < Method > which contains information about methods which was declared
     */

    //javadoc -private -d R ru.ifmo.ctddev.belonogov.implementor


    private ArrayList<Method> recDeclared(Class position) {
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


    /**
     * Traverses all interfaces that implements or classes that extends {@link java.lang.Class} position and search all implemented methods
     * @param position      class from which to start search
     * @return              {@link java.util.ArrayList < {@link java.lang.reflect.Method } ></>
     * which contains information about methods which was implemented
     */


    private ArrayList<Method> recImplemented(Class position) {
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

    /**
     * Generate {@link java.lang.String} for method or constructor parameters
     * @param args      Array with arguments types
     * @param isVarArgs is this method VarArgs
     * @return          {@link java.lang.String} that describe parameters for method or constructor.
     */

    public String generateParameterString(Type[] args, boolean isVarArgs) {
        String result = "(";
        countArgs = 0;
        for (int i = 0; i < args.length - 1; i++) {
            Type arg = args[i];
            if (countArgs > 0) {
                result += ", ";
            }
            result += arg.getTypeName() + " a" + countArgs;
            countArgs++;
        }
        if (args.length > 0) {
            if (isVarArgs) {
                Type arg = args[args.length - 1];
                if (countArgs > 0) {
                    result += ", ";
                }
                String tmpS = arg.getTypeName();
                result += tmpS.substring(0, tmpS.length() - 2) + " ... a" + countArgs;
            } else {
                Type arg = args[args.length - 1];
                if (countArgs > 0) {
                    result += ", ";
                }
                result += arg.getTypeName() + " a" + countArgs;
            }
            countArgs++;
        }
        result += ")";
        return result;
    }

    /**
     * Contains information about method
     */


    private class MethodInfo {
        String args;
        String accessType;
        Type returnType;

        public MethodInfo(String args, String accessType, Type returnType) {
            assert(args != null && accessType != null && returnType != null);
            this.args = args;
            this.accessType = accessType;
            this.returnType = returnType;
        }

        public String getArgs() {
            return args;
        }

        public String getAccessType() {
            return accessType;
        }

        public Type getReturnType() {
            return returnType;
        }
    }

    /**
     * Create MethodInfo from method
     *
     * @param method        method for generation MethodInfo
     * @return {@link ru.ifmo.ctddev.belonogov.implementor.Main.MethodInfo}
     */

    private MethodInfo makeStringFromMethod(Method method) {
        //method.getGenericParameterTypes()
        //method.getGenericReturnType();
        String accessType = "";
        String args = "";

        if (Modifier.isProtected(method.getModifiers())) {
            accessType = "protected ";
        }
        if (Modifier.isPublic(method.getModifiers())) {
            accessType = "public ";
        }

        args += method.getName();
        args += generateParameterString(method.getGenericParameterTypes(), method.isVarArgs());
        args += " {";
        if (method.getReturnType().equals(void.class)) {
            args += String.format("}");
        } else {
            args += String.format("%n        return");
            if (method.getReturnType().isPrimitive()) {
                if (method.getReturnType().equals(boolean.class)) {
                    args += " false";
                } else {
                    args += " 0";
                }
            } else {
                args += " null";
            }
            args += String.format(";%n    }");
        }
        return new MethodInfo(args, accessType, method.getGenericReturnType());

    }



    /**
     * Create implementation for given class.
     *
     * @param token             {@link java.lang.Class} for implementation
     * @param root              Directory where create implementation
     * @throws ImplerException  when class can't be implemented (final, primitive, all constructors are private)
     *
     */


    private void run(Class<?> token, File root) throws ImplerException {
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
            //System.out.println(sourceFile);
            String myClassName = token.getSimpleName() + "Impl";
            out.println("package " + token.getPackage().getName() + ";");
            out.println();
            out.print("public class " + myClassName + " ");

            if (token.isInterface()) {
                out.print("implements ");
            } else {
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
                    for (Class exception : exceptions) {
                        if (cnt > 0) {
                            out.print(", ");
                        }
                        out.print(exception.getCanonicalName());
                        cnt++;
                    }
                    out.println(" {");
                    out.print("        " + "super(");
                    for (int i = 0; i < countArgs; i++) {
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

            HashMap<String, Pair<String, Type>> forImplementation = new HashMap<>();

            for (Method method : implementedMethods) {
                MethodInfo result = makeStringFromMethod(method);
                if (!forImplementation.containsKey(result.getAccessType())) {
                    forImplementation.put(result.getArgs(), new Pair<>(result.getAccessType(), result.getReturnType()));
                } else {
                    Type oldClass = result.getReturnType();
                    Type newClass = forImplementation.get(result.getArgs()).getValue();
                    if (((Class) newClass).isAssignableFrom((Class) oldClass)) {

                        forImplementation.remove(result.getArgs());
                        forImplementation.put(result.getArgs(), new Pair<>(result.getAccessType(), result.getReturnType()));
                    }
                }
            }
            for (Method method : declaredMethods) {
                MethodInfo result = makeStringFromMethod(method);
                if (forImplementation.containsKey(result.getArgs()) &&
                        forImplementation.get(result.getArgs()).getValue().equals(result.getReturnType())) {
                    forImplementation.remove(result.getArgs());
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
    }

    /**
     * Create implementation for given class.
     *
     * @param aClass            {@link java.lang.Class} for implementation
     * @param file              Directory where create implementation
     * @throws ImplerException  when class can't be implemented (final, primitive, all constructors are private)
     *
     */

    @Override
    public void implement(Class<?> aClass, File file) throws ImplerException {
        //System.err.println("final: " + Modifier.isFinal(aClass.getModifiers()));
        if (aClass == null || aClass.isPrimitive() || Modifier.isFinal(aClass.getModifiers()) || file == null) {
            throw new ImplerException(String.format("usage: Class - not null, not primitive, not final; %n" +
                    "File - not null"));
        }
        run(aClass, file);
    }

    /**
     * Create Jar file with implementation for given class.
     *
     * @param aClass            {@link java.lang.Class} for implementation
     * @param file              Directory where will create <code>.jar</code>
     * @throws ImplerException  when class can't be implemented (final, primitive, all constructors are private)
     *
     */


    @Override
    public void implementJar(Class<?> aClass, File file) throws ImplerException {
        //System.out.println("jar case");
        implement(aClass, file);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String path = sourceFile.getAbsolutePath();
        if (compiler.run(null, null, null, path) != 0) {
            throw new ImplerException();
        }
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        String fileS = path;
        File pathToClass = new File(fileS.substring(0, fileS.length() - 4) + "class");
        //System.err.println("tut");

//        File pathToJar = new File(".");
        String jarPath = file.getAbsolutePath() + File.separator + aClass.getSimpleName() + "Impl.jar";
        System.err.println("jarPath: " + jarPath);
        try (InputStream input = new BufferedInputStream(new FileInputStream(pathToClass));
             JarOutputStream target = new JarOutputStream(new FileOutputStream(jarPath), manifest)) {
            //sdfsdf

            String name = aClass.getPackage().getName().replace('.', File.separatorChar) + File.separatorChar
                    + aClass.getSimpleName() + "Impl.class";
            //System.out.println("args: " + args);
            assert (!name.isEmpty());

            if (!name.endsWith("/"))
                name += "/";
            //System.out.println("args: " + args);

            JarEntry entry = new JarEntry(name);
            entry.setTime(pathToClass.lastModified());
            target.putNextEntry(entry);
            int count;
            byte[] buffer = new byte[10000];
            while ((count = input.read(buffer)) >= 0)
                target.write(buffer, 0, count);

            target.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
