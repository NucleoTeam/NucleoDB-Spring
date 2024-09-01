package com.nucleodb.spring.mapping;

import org.springframework.data.repository.core.EntityInformation;

import java.lang.reflect.Field;

public class NDBEntityInformation<T, ID> implements EntityInformation<T, ID> {
    
    private final Class<T> entityClass;
    private final Class<ID> idClass;
    
    public NDBEntityInformation(Class<T> entityClass, Class<ID> idClass) {
        this.entityClass = entityClass;
        this.idClass = idClass;
    }

    @Override
    public boolean isNew(T entity) {
        // Assuming that if the ID is null, the entity is new
        return getId(entity) == null;
    }

    @Override
    public ID getId(T entity) {
        try {
            Field idField = entityClass.getDeclaredField("id");
            if(idField == null) {
                idField = entityClass.getDeclaredField("uuid");
            }
            idField.setAccessible(true);
            return (ID) idField.get(entity);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Could not retrieve id field", e);
        }
    }


    @Override
    public Class<ID> getIdType() {
        return idClass;
    }
    
    @Override
    public Class<T> getJavaType() {
        return entityClass;
    }
}