package fr.alexpado.cgc.annotations;

import fr.alexpado.cgc.enums.InteractionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface InteractAt {

    InteractionType[] value() default {InteractionType.BUTTON, InteractionType.SLASH};

}
