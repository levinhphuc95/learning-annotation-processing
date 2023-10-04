package com.phucle.annotation.processing.processor;

import com.google.auto.service.AutoService;
import com.phucle.annotation.processing.Builder;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("com.phucle.annotation.processing.*")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class BuilderProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (var annotation : annotations) {
            warn("Annotation: " + annotation.getSimpleName());
            Set<Element> elements = roundEnv.getElementsAnnotatedWith(annotation)
                    .stream()
                    .map(item -> (Element) item)
                    .collect(Collectors.toSet());
            elements.forEach(element -> warn("Element: " + element.getKind() + " " + element.getSimpleName()));
        }

        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Builder.class)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                System.out.println("Only class can be annotated with @Builder");
                return true;
            }

            TypeElement typeElement = (TypeElement) annotatedElement;

            createBuilderFile(typeElement);
        }
        return true;
    }

    private void createBuilderFile(TypeElement typeElement) {

        String packageName = processingEnv.getElementUtils().getPackageOf(typeElement).toString();
        String className = typeElement.getSimpleName().toString();

        String builderPackageName = packageName + ".build";
        String builderClassName = className + "Builder";

        TypeSpec.Builder builderClass = TypeSpec.classBuilder(builderClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        List<Element> fieldElements = typeElement.getEnclosedElements()
                .stream()
                .filter(element -> ElementKind.FIELD.equals(element.getKind()))
                .map(element -> (Element) element)
                .toList();

        // Add fields
        for (Element field : fieldElements) {
            builderClass.addField(FieldSpec.builder(
                    TypeName.get(field.asType()), field.getSimpleName().toString(), Modifier.PRIVATE).build());
        }

        // Add no args constructor
        builderClass.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build());

        // Add methods
        for (Element field : fieldElements) {
            builderClass.addMethod(MethodSpec.methodBuilder(field.getSimpleName().toString())
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ClassName.get(builderPackageName, builderClassName))
                    .addParameter(TypeName.get(field.asType()), field.getSimpleName().toString())
                    .addStatement("this.$N = $N", field.getSimpleName().toString(), field.getSimpleName().toString())
                    .addStatement("return this")
                    .build());
        }

        // Add method builder
        MethodSpec.Builder buildMethodBuilder = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(packageName, className));

        String constructorArgs = fieldElements.stream()
                .map(f -> f.getSimpleName().toString())
                .collect(Collectors.joining(", "));

        buildMethodBuilder.addStatement("return new $T($L)", ClassName.get(packageName, className), constructorArgs);

        builderClass.addMethod(buildMethodBuilder.build());

        JavaFile javaFile = JavaFile.builder(builderPackageName, builderClass.build())
                .build();
        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            error("Failed to write builder class: " + e.getMessage(), typeElement);
        }
    }

    private void error(String message, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    private void warn(String message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message);
    }
}
