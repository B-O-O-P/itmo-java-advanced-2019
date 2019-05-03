package ru.ifmo.rain.chizhikov.jarimplementor;

import info.kgeorgiy.java.advanced.implementor.*;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * Implementation class for {@link JarImpler} interface
 */
public class JarImplementor implements JarImpler {
    
    /**
     * Main function is used to choose which way of implementation to execute.
     * Runs {@link JarImplementor} in two possible ways:
     * <ul>
     * <li> 2 arguments: className rootPath - runs {@link #implement(Class, Path)} with given arguments</li>
     * <li> 3 arguments: -jar className jarPath - runs {@link #implementJar(Class, Path)} with two second arguments</li>
     * </ul>
     * If arguments are incorrect or an error occurred during implementation returns message with information about it
     *
     * @param args arguments for running an application
     */
    public static void main(String[] args) {
        if (args == null || (args.length != 2 && args.length != 3)) {
            System.out.println("Two or three arguments expected");
            return;
        }
        if (Arrays.stream(args).filter(Objects::isNull)
                .collect(Collectors.toList()).size() != 0) {
            System.err.println("Not null arguments expected");
        }

        JarImpler implementor = new JarImplementor();
        try {
            if (args.length == 2) {
                implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
            } else {
                implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
            }
        } catch (LinkageError | ClassNotFoundException e) {
            System.err.println("Invalid class name: " + e.getMessage());
        } catch (InvalidPathException e) {
            System.err.println("Invalid path to root: " + e.getMessage());
        } catch (ImplerException e) {
            System.err.println("Error while implementation: " + e.getMessage());
        }
    }

    /**
     * Creates .jar file implementing class or interface specified by provided class.
     *
     * During implementation creates temporary folder to store temporary .java and .class files.
     * If program fails to delete temporary folder, it provides information about it.
     *
     * @throws ImplerException if the given class cannot be generated for one of following reasons:
     *                         <ul>
     *                         <li> Given class is null</li>
     *                         <li> Error occurs during implementation via {@link #implement(Class, Path)} </li>
     *                         <li> {@link JavaCompiler} failed to compile implemented class </li>
     *                         <li> The problems with I/O occurred during writing implementation. </li>
     *                         </ul>
     */
    @Override
    public void implementJar(Class<?> clazz, Path path) throws ImplerException {
        checkClass(clazz);
        createDirectory(path);
        Path tempDirectory;

        try {
            tempDirectory = Files.createTempDirectory(path.toAbsolutePath().getParent(), "temp");
        } catch (IOException e) {
            throw new ImplerException("Cannot create temporary directory", e);
        }

        try {
            implement(clazz, tempDirectory);
            compileFiles(clazz, tempDirectory);
            createJar(clazz, path, tempDirectory);
        } finally {
            try {
                clean(tempDirectory);
            } catch (IOException e) {
                System.err.println("Cannot delete temporary directory");
            }
        }
    }

    /**
     * Create implementation of given {@link Class} or Interface and write it by specified path
     *
     * @param clazz {@link Class} to get implementation from
     * @param path  {@link Path} to get class from
     * @throws ImplerException if the given class cannot be generated for one of following reasons:
     *                         <ul>
     *                         <li>Given class is null. </li>
     *                         <li> Given class is primitive or array. </li>
     *                         <li> Given class is final class or {@link Enum}. </li>
     *                         <li> class isn't an interface and contains only private constructors. </li>
     *                         <li> The problems with I/O occurred during writing implementation. </li>
     *                         </ul>
     */
    @Override
    public void implement(Class<?> clazz, Path path) throws ImplerException {
        checkClass(clazz);
        path = getFilePath(clazz, path, ".java");
        createDirectory(path);

        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {
            bufferedWriter.write(toUnicode(generateClassHeader(clazz)));

            if (!clazz.isInterface()) {
                for (Constructor<?> constructor : generateConstructors(clazz)) {
                    bufferedWriter.write(toUnicode(generateExecutable(constructor)));
                }
            }

            for (ClassMethod method : generateMethods(clazz)) {
                bufferedWriter.write(toUnicode(generateExecutable(method.instance)));
            }

            bufferedWriter.write(toUnicode("}" + NEW_LINE));
        } catch (IOException e) {
            throw new ImplerException("ERROR: Can not write implementation");
        }
    }

    /**
     * Converts given string to Unicode
     * @param s {@link String} to convert
     * @return converted string
     */
    private static String toUnicode(String s) {
        StringBuilder b = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= 128) {
                b.append(String.format("\\u%04X", (int) c));
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }

    /**
     * Compile implementation with needed classes or interfaces of given class by specified path
     *
     * @param clazz given {@link Class} to create its implementation
     * @param path  specified {@link Path}
     * @throws ImplerException throws if {@link JavaCompiler} failed to compile implemented class.
     */
    private void compileFiles(Class<?> clazz, Path path) throws ImplerException {
        String[] args = new String[]{
                "-cp",
                path.toString() + File.pathSeparator
                        + System.getProperty("java.class.path"),
                "-encoding",
                "UTF8",
                getFilePath(clazz, path, ".java").toString()

        };

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null || compiler.run(null, null, null, args) != 0) {
            throw new ImplerException("Cannot compile files");
        }
    }

    /**
     * Creates .jar file implementing class or interface specified by provided clazz.
     *
     * @param clazz         given {@link Class} to get implementation
     * @param path          specified {@link Path}
     * @param tempDirectory {@link Path} - temporary directory where implementation will be located.
     * @throws ImplerException throws if {@link JarOutputStream} failed to write implementation .jar.
     */
    private void createJar(Class<?> clazz, Path path, Path tempDirectory) throws ImplerException {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(Attributes.Name.IMPLEMENTATION_VENDOR, "Chizhikov Dmitriy");

        try (JarOutputStream jarOutputStream = new JarOutputStream(Files.newOutputStream(path), manifest)) {
            jarOutputStream.putNextEntry(new ZipEntry(clazz.getName().replace('.', '/').concat("Impl.class")));
            Files.copy(getFilePath(clazz, tempDirectory, ".class"), jarOutputStream);
        } catch (IOException e) {
            throw new ImplerException("Cannot write Jar file", e);
        }
    }

    /**
     * Recursively deletes directory represented by path
     *
     * @param path directory to be deleted
     * @throws IOException if error occurred during deleting
     */
    private void clean(Path path) throws IOException {
        Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(file -> file.delete());
    }

    /**
     * Adds "Impl" suffix to simple name of given class
     *
     * @param clazz class to get name
     * @return {@link String} with specified class name
     */
    private String getClassName(Class<?> clazz) {
        return clazz.getSimpleName().concat("Impl");
    }

    /**
     * Return {@link Path} to file, containing implementation of given class, with specific file suffix
     * located in directory represented by path
     *
     * @param path    path to parent directory of class
     * @param clazz   class to get name from
     * @param fileEnd file suffix
     * @return {@link Path} representing path to certain file
     */
    private Path getFilePath(Class<?> clazz, Path path, String fileEnd) {
        return path.resolve(clazz.getPackage().getName().replace('.', File.separatorChar))
                .resolve(getClassName(clazz) + fileEnd);
    }

    /**
     * Return {@link String} of all modifiers of {@link Executable} excluding {@link Modifier#TRANSIENT} and {@link Modifier#ABSTRACT}
     *
     * @param executable {@link Executable} to get modifiers from
     * @return {@link String} with all allowed modifiers for this class
     */
    private String generateModifiers(Executable executable) {
        return Modifier.toString(executable.getModifiers() & ~Modifier.TRANSIENT & ~Modifier.ABSTRACT);
    }

    /**
     * Returns name of {@link Parameter} and optionally excluding its type
     *
     * @param parameter    parameter to get name from
     * @param withoutTypes flag responsible for excluding parameter type
     * @return {@link String} representing parameter's name
     */
    private String generateParameter(Parameter parameter, boolean withoutTypes) {
        return (withoutTypes ? EMPTY_STRING : parameter.getType().getCanonicalName() + " ") + parameter.getName();
    }

    /**
     * Returns list of parameters of {@link Executable}, surrounded by round parenthesis, separated by commas and
     * optionally excluding their types
     *
     * @param executable   {@link Executable} to get parameters from
     * @param withoutTypes flag responsible for adding parameter type
     * @return {@link String} representing list of parameters
     */
    private String generateParameters(Executable executable, boolean withoutTypes) {
        return Arrays.stream(executable.getParameters())
                .map(parameter -> generateParameter(parameter, withoutTypes))
                .collect(Collectors.joining(", ", "(", ")"));
    }


    /**
     * Returns list of {@link Exception}s, that given {@link Executable} can throw
     *
     * @param executable {@link Executable} to get exceptions from
     * @return {@link String} representing list of exceptions
     */
    private String generateExceptions(Executable executable) {
        StringBuilder stringBuilder = new StringBuilder();
        Class<?>[] exceptions = executable.getExceptionTypes();

        if (exceptions.length != 0) {
            stringBuilder.append(" throws ").append(
                    Arrays.stream(exceptions)
                            .map(Class::getCanonicalName)
                            .collect(Collectors.joining(", "))
            );
        }

        return stringBuilder.toString();
    }

    /**
     * Creates signature of given {@link Executable} by using {@link #generateModifiers(Executable)},
     * {@link #generateParameters(Executable, boolean)}, {@link #generateExceptions(Executable)} and specified
     * for {@link Constructor} or {@link Method}.
     * If given {@link Executable} is instance of {@link Constructor} returns name of generated class,
     * otherwise returns return type and name of such {@link Method}
     *
     * @param executable given {@link Constructor} or {@link Method}
     * @return {@link String} representing such return type and name
     */
    private String generateSignature(Executable executable) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("      ")
                .append(generateModifiers(executable))
                .append(" ");

        if (executable instanceof Method) {
            Method method = (Method) executable;
            stringBuilder.append(method.getReturnType().getCanonicalName())
                    .append(" ")
                    .append(method.getName());
        } else {
            stringBuilder.append(getClassName(((Constructor<?>) executable).getDeclaringClass()));
        }

        stringBuilder.append(generateParameters(executable, false))
                .append(generateExceptions(executable))
                .append(" ");

        return stringBuilder.toString();
    }

    /**
     * Creates default return value of given class
     *
     * @param clazz class to get default return value
     * @return {@link String} representing default value
     */
    private String generateReturn(Class<?> clazz) {
        if (clazz.equals(boolean.class)) {
            return " false";
        } else if (clazz.equals(void.class)) {
            return EMPTY_STRING;
        } else if (clazz.isPrimitive()) {
            return " 0";
        }
        return " null";
    }

    /**
     * Calls constructor of super class if given {@link Executable} is instance of {@link Constructor},
     * otherwise return default value of return type of such {@link Method}
     *
     * @param executable given {@link Constructor} or {@link Method}
     * @return {@link String} representing body of {@link Constructor} of {@link Method}
     */
    private String generateBody(Executable executable) {
        if (executable instanceof Method) {
            return "return" + generateReturn(((Method) executable).getReturnType());
        } else {
            return "super" + generateParameters(executable, true);
        }
    }

    /**
     * Returns code of completed {@link Executable}, that calls constructor of super class if
     * executable is instance of {@link Constructor} or returns default value of return type
     * of such {@link Method}
     *
     * @param executable given {@link Constructor} or {@link Method}
     * @return {@link String} representing code of  given {@link Constructor} or {@link Method}
     */
    private String generateExecutable(Executable executable) {

        return generateSignature(executable) +
                "{" +
                NEW_LINE +
                "       " +
                generateBody(executable) +
                ";" +
                NEW_LINE +
                "   " +
                "}" +
                NEW_LINE;
    }

    /**
     * Check is class can be generated
     *
     * @param clazz given class to be checked
     * @throws ImplerException if the implementation of given class cannot be generated for one of following reasons:
     *                         <ul>
     *                         <li> Given class is null</li>
     *                         <li> Given class is primitive or array. </li>
     *                         <li> Given class is final class or {@link Enum}. </li>
     *                         </ul>
     */
    private void checkClass(Class<?> clazz) throws ImplerException {
        if (clazz == null
                || clazz.isArray()
                || clazz.isPrimitive()
                || Modifier.isFinal(clazz.getModifiers())
                || clazz == Enum.class) {
            throw new ImplerException("ERROR: Invalid class!");
        }
    }


    /**
     * Creates parent directory for file represented by file
     *
     * @param path file to create parent directory
     * @throws ImplerException if error occurred during creation
     */
    private void createDirectory(Path path) throws ImplerException {
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new ImplerException("ERROR: can not create directories for output", e);
            }
        }
    }

    /**
     * Gets package of given file. Package is empty, if class is situated in default package
     * @param clazz class to get package
     * @return {@link String} representing package
     */
    private static String getPackage(Class<?> clazz) {
        StringBuilder res = new StringBuilder();
        if (!clazz.getPackage().getName().equals(EMPTY_STRING)) {
            res.append("package" + " ").append(clazz.getPackage().getName()).append(";").append(NEW_LINE);
        }
        res.append(NEW_LINE);
        return res.toString();
    }

    /**
     * Returns beginning block of the class, containing its package, name, extended class or
     * implemented interface
     *
     * @param clazz extended class or implemented interface
     * @return {@link String} representing beginning block of class
     */
    private String generateClassHeader(Class<?> clazz) {
        StringBuilder stringBuilder = new StringBuilder();
        String packageName = getPackage(clazz);
        String extOrImpl = clazz.isInterface() ? "implements" : "extends";
        if (!packageName.isEmpty()) {
            stringBuilder.append("package ")
                    .append(clazz.getPackage().getName())
                    .append(";")
                    .append(NEW_LINE);
        }


        stringBuilder.append(NEW_LINE)
                .append("public class ")
                .append(getClassName(clazz))
                .append(" ")
                .append(extOrImpl)
                .append(" ")
                .append(clazz.getCanonicalName())
                .append(" ")
                .append("{")
                .append(NEW_LINE);

        return stringBuilder.toString();
    }

    /**
     * Returns {@link Set} of abstract methods of given {@link Class}
     *
     * @param clazz given class to get methods from
     * @return {@link Set} of abstract methods
     */
    private Set<ClassMethod> generateMethods(Class<?> clazz) {
        Set<ClassMethod> methods = new HashSet<>();
        Arrays.stream(clazz.getMethods())
                .filter(method -> Modifier.isAbstract(method.getModifiers()))
                .map(ClassMethod::new)
                .collect(Collectors.toCollection(() -> methods));

        while (clazz != null) {
            Arrays.stream(clazz.getDeclaredMethods())
                    .filter(method -> Modifier.isAbstract(method.getModifiers()))
                    .map(ClassMethod::new)
                    .collect(Collectors.toCollection(() -> methods));
            clazz = clazz.getSuperclass();
        }

        return methods;
    }

    /**
     * Returns {@link Array} of non-private constructors of given {@link Class}
     *
     * @param clazz given class to get constructors from
     * @return {@link Array} of constructors
     * @throws ImplerException if class doesn't have any non-private constructors
     */
    private Constructor<?>[] generateConstructors(Class<?> clazz) throws ImplerException {
        Constructor<?>[] constructors = Arrays.stream(clazz.getDeclaredConstructors())
                .filter(constructor -> !Modifier.isPrivate(constructor.getModifiers()))
                .toArray(Constructor<?>[]::new);

        if (constructors.length == 0) {
            throw new ImplerException("No available constructors in class");
        }

        return constructors;
    }

    /**
     * Creates new instance of {@link JarImplementor}
     */
    public JarImplementor() {

    }


    /**
     * Empty string for generated classes
     */
    private static final String EMPTY_STRING = "";

    /**
     * Line separator for generated classes
     */
    private static final String NEW_LINE = System.lineSeparator();

    /**
     * Class which is used for representing {@link Method}
     */
    private class ClassMethod {
        /**
         * Instance of {@link Method}
         */
        private Method instance;

        /**
         * Creates representation for specified instance of {@link Method}
         *
         * @param method instance of {@link Method} to be represented
         */
        ClassMethod(Method method) {
            instance = method;
        }


        /**
         * Compares the specified object with this representation for equality. Representations are equal, if their
         * hash codes of their return type, names and parameters of their {@link #instance} are equal.
         *
         * @param object the object to be compared for equality with this {@link Method} representation
         * @return true if specified object is equal to this representation
         */
        @Override
        public boolean equals(Object object) {
            if (object == null) {
                return false;
            } else if (object instanceof ClassMethod) {
                return object.hashCode() == hashCode();
            }
            return false;
        }

        /**
         * Calculates simple hashcode for this representation
         * using hashes of name, return type and parameters types of its {@link #instance}
         *
         * @return hashcode for this wrapper
         */
        @Override
        public int hashCode() {
            return (instance.getReturnType().hashCode() + instance.getName().hashCode()) * 17 + Arrays.hashCode(instance.getParameterTypes());
        }
    }
}
