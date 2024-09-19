package com.nucleodb.spring.impl;

import com.nucleodb.library.NucleoDB;
import com.nucleodb.library.database.tables.table.DataEntry;
import com.nucleodb.library.database.tables.table.DataTable;
import com.nucleodb.library.database.utils.exceptions.IncorrectDataEntryObjectException;
import com.nucleodb.library.database.utils.exceptions.ObjectNotSavedException;
import com.nucleodb.spring.types.NDBDataRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class NDBDataEntryRepositoryImpl<T extends DataEntry, ID> implements NDBDataRepository<T, ID> {
    private @Nullable DataTable table = null;
    private final NucleoDB nucleoDB;
    private final Class<T> classType;
    private @Nullable Class<?> tableClass = null;
    private final ApplicationEventPublisher publisher;

    public NDBDataEntryRepositoryImpl(NucleoDB nucleoDB, Class<T> classType, ApplicationEventPublisher publisher) {
        this.nucleoDB = nucleoDB;
        this.classType = classType;
        this.publisher = publisher;
        Type[] actualTypeArguments = ((ParameterizedType) classType.getGenericSuperclass()).getActualTypeArguments();
        if (actualTypeArguments.length == 1) {
            this.tableClass = (Class<?>) actualTypeArguments[0];
            this.table = nucleoDB.getTable(this.tableClass);
        }
    }

    @Override
    public T save(T entity) {
        AtomicReference<T> returnedVal = new AtomicReference<>();
        try {
            table.saveAsync(entity, (de) -> {
                returnedVal.set((T) de);
                synchronized (returnedVal) {
                    returnedVal.notify();
                }
            });
            synchronized (returnedVal) {
                returnedVal.wait();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IncorrectDataEntryObjectException e) {
            throw new RuntimeException(e);
        }
        return returnedVal.get();
    }

    public void saveForget(T entity) {
        try {
            table.saveAndForget(entity);
        } catch (IncorrectDataEntryObjectException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<T> saveAll(Iterable<T> entities) {
        List<T> items = new LinkedList<>();
        entities.forEach(entity -> {
            T savedEntity = save(entity);
            if (savedEntity == null) return;
            items.add(savedEntity);
        });
        return items;
    }

    @Override
    public T findById(ID id) {
        Set<T> dataEntrySet = table.get("id", id);
        System.out.println("fetched Id: "+id);
        if (dataEntrySet != null && dataEntrySet.size() > 0) {
            return dataEntrySet.size()>0? (T)dataEntrySet.toArray()[0]:null;
        }
        return null;
    }

    @Override
    public boolean existsById(ID id) {
        Set<DataEntry> dataEntrySet = table.get("id", id);
        System.out.println("fetched Id: "+id);
        if (dataEntrySet != null && dataEntrySet.size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public List<T> findAll() {
        assert table != null;
        return List.copyOf(table.getEntries());
    }

    @Override
    public List<T> findAllById(Iterable<ID> iterable) {
        List<T> items = new LinkedList<>();
        for (ID id : iterable) {
            T byId = findById(id);
            System.out.println("fetched Id: "+id);
            if (byId!=null) {
                items.add(byId);
            }
        }
        return items;
    }

    @Override
    public long count() {
        return table.getSize();
    }

    @Override
    public void deleteById(ID id) {
        System.out.println("delete by id: "+id);
        T byId = findById(id);
        if (byId == null)
            return;
        delete(byId);
    }

    @Override
    public void delete(T entity) {
        try {
            table.deleteSync(entity);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {
        ids.forEach(id -> deleteById(id));
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        entities.forEach(de -> delete(de));
    }

    @Override
    public void deleteAll() {
        table.getEntries().forEach(de -> delete((T) de));
    }

    @Override
    public List<T> findAll(Sort sort) {
        return findAll().stream().sorted((o1, o2) -> {
            for (Sort.Order order : sort) {
                int comparison = 0;
                try {
                    java.lang.reflect.Field field = classType.getDeclaredField(order.getProperty());
                    Object o1Object = field.get(o1);
                    Object o2Object = field.get(o2);
                    if (field.getGenericType() == String.class) {
                        comparison = o1Object.toString().compareTo(o2Object.toString());
                    } else if (field.getGenericType() == Integer.class) {
                        comparison = Integer.compare((Integer) o1Object, (Integer) o2Object);
                    } else if (field.getGenericType() == Long.class) {
                        comparison = Long.compare((Long) o1Object, (Long) o2Object);
                    }
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                if (comparison != 0) {
                    return order.isAscending() ? comparison : -comparison;
                }
            }
            return 0;
        }).collect(Collectors.toList());
    }
}
