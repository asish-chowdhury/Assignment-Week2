package task;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import com.google.common.reflect.ClassPath;
import org.reflections.*;
import org.reflections.scanners.MethodAnnotationsScanner;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.io.*;

public class Solve {

    private static String annotationClassName = "annotation.ClassDoc";
    private static String annotationMethodName = "annotation.MethodDoc";

    public static void main(String[] args) throws IOException {

        String filePath = "C:\\week2\\GradleTask\\src\\main\\resources\\OutputDocument.txt";

        try(FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            PrintStream printStream = new PrintStream(fileOutputStream)) {

            System.setOut(printStream);

            String packageName = "com.example.GradleTask";

            Set<Class<?>> classes = new HashSet<>();
            System.out.println("Classes in the package:");
            try {
                ClassPath classPath = ClassPath.from(Thread.currentThread().getContextClassLoader());
                for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClasses(packageName)) {
                    System.out.println("  |-->"+classInfo.getName());
                    classes.add(classInfo.load());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println();

            Reflections reflections2 = new org.reflections.Reflections(packageName, new MethodAnnotationsScanner());


            Reflections reflections = new org.reflections.Reflections(packageName);

            Set<Class<?>> annotationClasses = reflections.getTypesAnnotatedWith(getClassForName(annotationClassName), true);
            System.out.println("Classes with @" + annotationClassName + ":");
            for (Class<?> annotatedClass : annotationClasses) {
                System.out.println("  |-->"+annotatedClass.getName());
            }
            System.out.println();

            Set<Method> annotatedMethods = reflections2.getMethodsAnnotatedWith(getClassForName(annotationMethodName));
            System.out.println("Methods with @" + annotationMethodName + ":");
            for (Method annotationMethod : annotatedMethods) {
                System.out.println("  |-->"+annotationMethod);
            }
            System.out.println();
//
//        Reflections reflections3 = new Reflections(packageName, new SubTypesScanner());
//        Set<Class<?>> classes = reflections3.getSubTypesOf(Object.class);
            Set<Class<?>> annotationClassWithoutJdoc = new HashSet<>();
            Set<Class<?>> javadocClassWithoutAnnotation = new HashSet<>();
            System.out.println("Classes with javadoc:");
            for (Class<?> clazz : classes) {
                String classPath = clazz.getName().replace(".", "/") + ".java";
//            System.out.println(classPath);
                try {
                    String sourceCode = new String(Files.readAllBytes(Paths.get("src/main/java", classPath)));
                    CompilationUnit cu = StaticJavaParser.parse(sourceCode);

                    cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDeclaration -> {
                        if (classDeclaration.getComment().isPresent() && classDeclaration.getComment().get() instanceof JavadocComment) {
                            String javadoc = ((JavadocComment) classDeclaration.getComment().get()).getContent();
                            System.out.println("Class: " + clazz.getName());
                            System.out.println("Javadoc:\n" + javadoc);
                            System.out.println("===========================================");
                            if(!annotationClasses.contains(clazz)){
                                javadocClassWithoutAnnotation.add(clazz);
                            }
                        } else {
                            annotationClassWithoutJdoc.add(clazz);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println();
            System.out.println("Class with annotation but no javadoc:" );
            for (Class<?> clazz : annotationClassWithoutJdoc) {
                System.out.println("  |-->"+ clazz.getName());
            }
            System.out.println();

            System.out.println("Class with javadoc but no annotation:");
            for (Class<?> clazz : javadocClassWithoutAnnotation) {
                System.out.println("  |-->"+ clazz.getName());
            }
            System.out.println();

            System.out.println("Methods with javadoc:");
            for (Class<?> clazz : classes) {
                String classPath = clazz.getName().replace(".", "/") + ".java";
                try {
                    String sourceCode = new String(Files.readAllBytes(Paths.get("src/main/java", classPath)));
                    CompilationUnit cu = StaticJavaParser.parse(sourceCode);

                    cu.findAll(MethodDeclaration.class).forEach(method -> {
                        if (method.getComment().isPresent() && method.getComment().get() instanceof JavadocComment) {
                            String javadoc = ((JavadocComment) method.getComment().get()).getContent();
                            System.out.println("Method: " + method.getDeclarationAsString());
                            System.out.println("Javadoc:\n" + javadoc);
                            System.out.println("===========================================");
                        }
                    });
                } catch (IOException | ParseProblemException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static Class<? extends Annotation> getClassForName(String className) {
        try {
            return (Class<? extends Annotation>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load class: " + className, e);
        }
    }

}
