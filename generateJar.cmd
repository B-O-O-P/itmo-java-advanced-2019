SET proj=C:\Users\dimka\IdeaProjects\Java-advanced-2019\JarImplementor
SET rep=C:\Users\dimka\IdeaProjects\Java-advanced-2019\java-advanced-2019
SET lib=%rep%\lib\*
SET test=%rep%\artifacts\info.kgeorgiy.java.advanced.implementor.jar
SET dst=%proj%\out\production\JarImplementor
SET man=%proj%\Manifest.txt
SET dep=info\kgeorgiy\java\advanced\implementor\
SET modules=%rep%\modules\
SET source=%proj%\src
cd %proj%
javac -d %dst% -cp %modules%;%lib%;%test%; src\ru\ifmo\rain\chizhikov\jarimplementor\JarImplementor.java

cd %dst%
jar xf %test% %dep%Impler.class %dep%JarImpler.class %dep%ImplerException.class
jar cfm %proj%\Implementor.jar %man% ru\ifmo\rain\chizhikov\jarimplementor\*.class %dep%*.class
cd %proj%