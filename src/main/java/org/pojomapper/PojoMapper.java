package org.pojomapper;

import org.pojomapper.copier.Copier;

public final class PojoMapper {
    
    public static <T> Copier<T> copyTo(T to) {
        return new Copier<>(to);
    }
}
