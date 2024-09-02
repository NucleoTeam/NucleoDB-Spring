package com.nucleodb.spring;

import com.nucleodb.library.NucleoDB;
import com.nucleodb.library.database.modifications.ConnectionCreate;
import com.nucleodb.library.database.modifications.ConnectionDelete;
import com.nucleodb.library.database.modifications.ConnectionUpdate;
import com.nucleodb.library.database.modifications.Create;
import com.nucleodb.library.database.modifications.Delete;
import com.nucleodb.library.database.modifications.Update;
import com.nucleodb.library.database.tables.connection.Connection;
import com.nucleodb.library.database.tables.connection.NodeFilter;
import com.nucleodb.library.database.tables.table.DataEntry;
import com.nucleodb.library.database.utils.exceptions.IncorrectDataEntryClassException;
import com.nucleodb.library.database.utils.exceptions.MissingDataEntryConstructorsException;
import com.nucleodb.library.event.ConnectionEventListener;
import com.nucleodb.library.event.DataTableEventListener;
import com.nucleodb.library.mqs.config.MQSConfiguration;
import com.nucleodb.spring.events.ConnectionCreatedEvent;
import com.nucleodb.spring.events.ConnectionDeletedEvent;
import com.nucleodb.spring.events.ConnectionUpdatedEvent;
import com.nucleodb.spring.events.DataEntryCreatedEvent;
import com.nucleodb.spring.events.DataEntryDeletedEvent;
import com.nucleodb.spring.events.DataEntryUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.Repository;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Map;


public class NDBRepositoryFactoryBean<T, ID, R extends Repository<T, ID>>
    extends RepositoryFactoryBeanSupport<R, T, ID>{

  private @NonNull String[] scanPackages;
  private @NonNull String mqsConfiguration;
  private @NonNull String nodeFilterConnection;
  private @NonNull String nodeFilterDataEntry;
  private @NonNull String readToTime;
  private @NonNull NucleoDB.DBType dbType;

  private ApplicationEventPublisher publisher;

  private static @Nullable NucleoDB nucleoDB = null;

  /**
   * Creates a new {@link RepositoryFactoryBeanSupport} for the given repository interface.
   *
   * @param repositoryInterface must not be {@literal null}.
   */
  protected NDBRepositoryFactoryBean(Class<? extends R> repositoryInterface) throws IncorrectDataEntryClassException, MissingDataEntryConstructorsException {
    super(repositoryInterface);
  }

  static Map<String, String> getenv = System.getenv();

  @Override
  protected RepositoryFactorySupport createRepositoryFactory() {
    if(nucleoDB==null) {
      try {
        MQSConfiguration mqsConfigurationInstance = (MQSConfiguration) Class.forName(mqsConfiguration).getDeclaredConstructor().newInstance();

        NodeFilter connectionNodeFilter = (NodeFilter) Class.forName(nodeFilterConnection).getDeclaredConstructor().newInstance();
        com.nucleodb.library.database.tables.table.NodeFilter dataEntryNodeFilter = (com.nucleodb.library.database.tables.table.NodeFilter) Class.forName(nodeFilterDataEntry).getDeclaredConstructor().newInstance();

        boolean jsonExport = Boolean.valueOf(getenv.getOrDefault("NDB_TOPIC_EXPORT","false"));
        boolean storeState = Boolean.valueOf(getenv.getOrDefault("NDB_STORE_STATE","false"));
        boolean loadState = Boolean.valueOf(getenv.getOrDefault("NDB_LOAD_STATE","false"));
        String saveDirectory = Path.of(getenv.getOrDefault("NDB_SAVE_DIR","/data")).toAbsolutePath().toString();
        ConnectionEventListener connectionEventListener = connectionEventListener();
        DataTableEventListener dataTableEventListener = dataTableEventListener();
        if(readToTime!=null && !readToTime.isEmpty()) {
          nucleoDB = new NucleoDB(
              dbType,
              readToTime,
              c -> {
                c.getConnectionConfig().setMqsConfiguration(mqsConfigurationInstance);
                c.getConnectionConfig().setEventListener(connectionEventListener);
                c.getConnectionConfig().setJsonExport(jsonExport);
                c.getConnectionConfig().setSaveChanges(storeState);
                c.getConnectionConfig().setLoadSaved(loadState);
                c.getConnectionConfig().setNodeFilter(connectionNodeFilter);
                c.getConnectionConfig().setConnectionFileName(
                        Path.of(saveDirectory, "conn_"+c.getConnectionConfig().getLabel()+".dat")
                                .toAbsolutePath()
                                .toString()
                );
              },
              c -> {
                c.getDataTableConfig().setMqsConfiguration(mqsConfigurationInstance);
                c.getDataTableConfig().setEventListener(dataTableEventListener);
                c.getDataTableConfig().setJsonExport(jsonExport);
                c.getDataTableConfig().setSaveChanges(storeState);
                c.getDataTableConfig().setLoadSave(loadState);
                c.getDataTableConfig().setNodeFilter(dataEntryNodeFilter);
                c.getDataTableConfig().setTableFileName(
                        Path.of(saveDirectory, "de_"+c.getDataTableConfig().getTable()+".dat")
                                .toAbsolutePath()
                                .toString()
                );
              },
              c -> {
                c.setMqsConfiguration(mqsConfigurationInstance);
              },
              scanPackages
          );
        }else{
          nucleoDB = new NucleoDB(
              dbType,
              c -> {
                c.getConnectionConfig().setMqsConfiguration(mqsConfigurationInstance);
                c.getConnectionConfig().setEventListener(connectionEventListener);
                c.getConnectionConfig().setJsonExport(jsonExport);
                c.getConnectionConfig().setSaveChanges(storeState);
                c.getConnectionConfig().setNodeFilter(connectionNodeFilter);
                c.getConnectionConfig().setLoadSaved(loadState);
                c.getConnectionConfig().setConnectionFileName(
                        Path.of(saveDirectory, "conn_"+c.getConnectionConfig().getLabel()+".dat")
                                .toAbsolutePath()
                                .toString()
                );
              },
              c -> {
                c.getDataTableConfig().setMqsConfiguration(mqsConfigurationInstance);
                c.getDataTableConfig().setEventListener(dataTableEventListener);
                c.getDataTableConfig().setJsonExport(jsonExport);
                c.getDataTableConfig().setSaveChanges(storeState);
                c.getDataTableConfig().setNodeFilter(dataEntryNodeFilter);
                c.getDataTableConfig().setLoadSave(loadState);
                c.getDataTableConfig().setTableFileName(
                        Path.of(saveDirectory, "de_"+c.getDataTableConfig().getTable()+".dat")
                                .toAbsolutePath()
                                .toString()
                );
              },
              c -> {
                c.setMqsConfiguration(mqsConfigurationInstance);
              },
              scanPackages
          );
        }

      } catch (IncorrectDataEntryClassException e) {
        throw new RuntimeException(e);
      } catch (MissingDataEntryConstructorsException e) {
        throw new RuntimeException(e);
      } catch (IntrospectionException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      } catch (InstantiationException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
    return new NDBRepositoryFactory(nucleoDB, publisher);
  }


  @Override
  public boolean isSingleton() {
    return true;
  }

  @Override
  public void afterPropertiesSet() {
    super.afterPropertiesSet();
    Assert.notNull(nucleoDB, "NucleoDB must not be null");
  }

  public void setScanPackages(@NonNull String[] scanPackages) {
    this.scanPackages = scanPackages;
  }

  public void setDbType(@NonNull NucleoDB.DBType dbType) {
    this.dbType = dbType;
  }

  public void setMqsConfiguration(@NonNull String mqsConfiguration) {
    this.mqsConfiguration = mqsConfiguration;
  }

  public void setReadToTime(@NonNull String readToTime) {
    this.readToTime = readToTime;
  }


  public void setNodeFilterDataEntry(@NonNull String nodeFilterDataEntry) {
    this.nodeFilterDataEntry = nodeFilterDataEntry;
  }

  public void setNodeFilterConnection(@NonNull String nodeFilterConnection) {
    this.nodeFilterConnection = nodeFilterConnection;
  }

  public void setPublisher(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }
  ConnectionEventListener connectionEventListener(){
    return new ConnectionEventListener<Connection>(){
      @Override
      public void update(ConnectionUpdate update, Connection entry) {
        publisher.publishEvent(new ConnectionUpdatedEvent(entry));
      }
      @Override
      public void delete(ConnectionDelete delete, Connection entry) {
        publisher.publishEvent(new ConnectionDeletedEvent(entry));
      }
      @Override
      public void create(ConnectionCreate create, Connection entry) {
        publisher.publishEvent(new ConnectionCreatedEvent(entry));
      }
    };
  }
  DataTableEventListener dataTableEventListener(){
    return new DataTableEventListener<DataEntry>(){
      @Override
      public void update(Update update, DataEntry entry) {
        publisher.publishEvent(new DataEntryUpdatedEvent(entry));
      }
      @Override
      public void delete(Delete delete, DataEntry entry) {
        publisher.publishEvent(new DataEntryDeletedEvent(entry));
      }
      @Override
      public void create(Create create, DataEntry entry) {
        publisher.publishEvent(new DataEntryCreatedEvent(entry));
      }
    };
  }
}
