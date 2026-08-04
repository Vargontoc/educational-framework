package es.vargontoc.educational.framework.shared.mapper;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMapper<T1, T2> implements IMapper<T1, T2> {
    
    public List<T1> toDomain(List<T2> source) {
        List<T1> result = new ArrayList<T1>();
        for (T2 item : source) {
            result.add(toDomain(item));
        }
        return result;
    }

    public List<T2> toJpa(List<T1> source) {
        List<T2> result = new ArrayList<T2>();
        for (T1 item : source) {
            result.add(toJpa(item));
        }
        return result;
    }
}
