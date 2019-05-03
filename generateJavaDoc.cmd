SET proj=C:\Users\dimka\IdeaProjects\Java-advanced-2019\JarImplementor
SET rep=C:\Users\dimka\IdeaProjects\Java-advanced-2019\java-advanced-2019
SET lib=%rep%\lib\*
SET test=%rep\artifacts\info.kgeorgiy.java.advanced.implementor.jar
SET data=%rep%\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor\
SET link=https://docs.oracle.com/en/java/javase/11/docs/api/
SET package=ru.ifmo.rain.chizhikov.jarimplementor

cd %proj%\src

javadoc -d %proj%\javadoc -link %link% -cp src\;%lib%;%test%; -private -author -version %package% %data%Impler.java %data%JarImpler.java %data%ImplerException.java
