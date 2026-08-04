package es.vargontoc.educational.framework.shared.mapper;

public interface IMapper<T1, T2> {
    
    T1 toDomain(T2 source);

    T2 toJpa(T1 source);
}
