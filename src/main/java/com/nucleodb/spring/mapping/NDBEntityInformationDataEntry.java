package com.nucleodb.spring.mapping;

import com.nucleodb.library.database.tables.connection.Connection;
import com.nucleodb.library.database.tables.table.DataEntry;
import org.springframework.data.repository.core.EntityInformation;

import java.lang.reflect.Field;

public class NDBEntityInformationDataEntry<T extends DataEntry> implements EntityInformation<T, String> {
    
    private final Class<T> entityClass;
    
    public NDBEntityInformationDataEntry(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public boolean isNew(T entity) {
        // Assuming that if the ID is null, the entity is new
        return getId(entity) == null;
    }

    @Override
    public String getId(T entity) {
        try {
            Field idField = entityClass.getDeclaredField("key");
            idField.setAccessible(true);
            return (String) idField.get(entity);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Could not retrieve id field", e);
        }
    }

    @Override
    public Class<String> getIdType() {
        return String.class;
    }
    
    @Override
    public Class<T> getJavaType() {
        return entityClass;
    }
}