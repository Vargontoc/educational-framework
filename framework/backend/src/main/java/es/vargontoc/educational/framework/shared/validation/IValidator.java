package es.vargontoc.educational.framework.shared.validation;

@FunctionalInterface
public interface IValidator<T> {

    void validate(T target);
}
