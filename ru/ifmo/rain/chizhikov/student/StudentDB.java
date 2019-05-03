package ru.ifmo.rain.chizhikov.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentGroupQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class StudentDB implements StudentGroupQuery {

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getGroups(students, this::sortStudentsByName);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getGroups(students, this::sortStudentsById);
    }

    @Override
    public String getLargestGroup(Collection<Student> students) {
        return getLargestGroup(students,List::size);
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return getLargestGroup(students,first -> getDistinctFirstNames(first).size());
    }



    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getStudentList(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getStudentList(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return getStudentList(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getStudentList(students, getFullName);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return getMapStream(students, Student::getFirstName)
                .sorted(String::compareTo)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream()
                .min(Student::compareTo)
                .map(Student::getFirstName)
                .orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudents(students, Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudents(students, naturalOrder);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudent(students, (Student student) -> student.getFirstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudent(students, (Student student) -> student.getLastName().equals(name));

    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return findStudent(students, (Student student) -> student.getGroup().equals(group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return getFilteredStream(students, (Student student) -> student.getGroup().equals(group))
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName, BinaryOperator.minBy(String::compareTo)));
    }


    private Function<Student, String> getFullName = student -> student.getFirstName() + " " + student.getLastName();

    private Comparator<Student> naturalOrder = Comparator.comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .thenComparing(Student::getId);

    private Stream<String> getMapStream(Collection<Student> students, Function<Student, String> function) {
        return students.stream().map(function);
    }

    private List<String> getStudentList(Collection<Student> students, Function<Student, String> function) {
        return getMapStream(students, function).collect(Collectors.toList());
    }

    private List<Student> sortStudents(Collection<Student> students, Comparator<Student> comparator) {
        return students.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private Stream<Student> getFilteredStream(Collection<Student> students, Predicate<Student> predicate) {
        return students.stream()
                .filter(predicate);
    }

    private List<Student> findStudent(Collection<Student> students, Predicate<Student> predicate) {
        return getFilteredStream(students, predicate)
                .sorted(naturalOrder)
                .collect(Collectors.toList());
    }


    private Stream<Map.Entry<String, List<Student>>> getGroupStream(Collection<Student> students, Supplier<Map<String, List<Student>>> type) {
        return students.stream()
                .collect(Collectors.groupingBy(Student::getGroup, type, Collectors.toList()))
                .entrySet().stream();
    }

    private List<Group> getGroups(Collection<Student> students, Function<List<Student>, List<Student>> order) {
        return getGroupStream(students, TreeMap::new)
                .map(group -> new Group(group.getKey(), order.apply(group.getValue())))
                .collect(Collectors.toList());
    }

    private String getLargestGroup(Collection<Student> students, Function<List<Student>, Integer> getSize) {
        return getGroupStream(students, HashMap::new)
                .max(Comparator.comparingInt(
                        (Map.Entry<String, List<Student>> group) -> getSize.apply(group.getValue()))
                        .thenComparing(Map.Entry::getKey, Collections.reverseOrder(String::compareTo)))
                .map(Map.Entry::getKey).orElse("");
    }
}
