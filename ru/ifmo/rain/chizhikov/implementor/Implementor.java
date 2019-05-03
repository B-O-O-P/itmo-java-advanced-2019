package ru.ifmo.rain.chizhikov.implementor;

import info.kgeorgiy.java.advanced.implementor.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


public class Implementor implements Impler {


    @Override
    public void implement(Class<?> clazz, Path path) throws ImplerException {
        checkClass(clazz);
        path = getFilePath(clazz, path,".java");
        createDirectories(path);

        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {
            bufferedWriter.write(generateClassHeader(clazz));

            if (!clazz.isInterface()) {
                for (Constructor<?> constructor : generateConstructors(clazz)) {
                    bufferedWriter.write(generateExecutable(constructor));
                }
            }

            for (ClassMethod method : generateMethods(clazz)) {
                bufferedWriter.write(generateExecutable(method.instance));
            }

            bufferedWriter.write("}" + NEW_LINE);
        } catch (IOException e) {
            throw new ImplerException("ERROR: Can not write implementation");
        }
    }


    private String getClassName(Class<?> clazz) {
        return clazz.getSimpleName().concat("Impl");
    }

    private Path getFilePath(Class<?> clazz, Path path, String fileEnd) {
        return path.resolve(clazz.getPackage().getName().replace('.', File.separatorChar))
                .resolve(getClassName(clazz) + fileEnd);
    }


    private String generateModifiers(Executable executable) {
        return Modifier.toString(executable.getModifiers() & ~Modifier.NATIVE & ~Modifier.TRANSIENT & ~Modifier.ABSTRACT);
    }


    private String generateParameter(Parameter parameter, boolean withoutTypes) {
        return (withoutTypes ? EMPTY_STRING : parameter.getType().getCanonicalName() + " ") + parameter.getName();
    }

    private String generateParameters(Executable executable, boolean withoutTypes) {
        return Arrays.stream(executable.getParameters())
                .map(parameter -> generateParameter(parameter, withoutTypes))
                .collect(Collectors.joining(", ", "(", ")"));
    }


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

    private String generateBody(Executable exec) {
        if (exec instanceof Method) {
            return "return" + generateReturn(((Method) exec).getReturnType());
        } else {
            return "super" + generateParameters(exec, true);
        }
    }

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


    private void checkClass(Class<?> aClass) throws ImplerException {
        if (aClass.isArray()
                || aClass.isPrimitive()
                || Modifier.isFinal(aClass.getModifiers())
                || aClass == Enum.class) {
            throw new ImplerException("ERROR: Invalid class!");
        }
    }

    private void createDirectories(Path path) throws ImplerException {
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new ImplerException("ERROR: can not create directories for output", e);
            }
        }
    }

    private String generateClassHeader(Class<?> clazz) {
        StringBuilder stringBuilder = new StringBuilder();
        String packageName = clazz.getPackageName();
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
                .append(clazz.getSimpleName())
                .append(" ")
                .append("{")
                .append(NEW_LINE);

        return stringBuilder.toString();
    }

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

    private Constructor<?>[] generateConstructors(Class<?> clazz) throws ImplerException {
        Constructor<?>[] constructors = Arrays.stream(clazz.getDeclaredConstructors())
                .filter(constructor -> !Modifier.isPrivate(constructor.getModifiers()))
                .toArray(Constructor<?>[]::new);

        if (constructors.length == 0) {
            throw new ImplerException("No available constructors in class");
        }

        return constructors;
    }


    private static final String EMPTY_STRING = "";
    private static final String NEW_LINE = System.lineSeparator();

    private class ClassMethod {
        private Method instance;

        ClassMethod(Method method) {
            instance = method;
        }

        @Override
        public boolean equals(Object object) {
            if (object == null) {
                return false;
            } else if (object instanceof ClassMethod) {
                return object.hashCode() == hashCode();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (instance.getReturnType().hashCode() + instance.getName().hashCode()) * 17 + Arrays.hashCode(instance.getParameterTypes());
        }
    }
}
