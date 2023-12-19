package com.nucleodb.spring;

import com.nucleodb.library.NucleoDB;
import com.nucleodb.library.database.utils.exceptions.IncorrectDataEntryClassException;
import com.nucleodb.library.database.utils.exceptions.MissingDataEntryConstructorsException;
import com.nucleodb.library.mqs.config.MQSConfiguration;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.Repository;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;


public class NDBRepositoryFactoryBean<T extends Repository<S, ID>, S, ID>
    extends RepositoryFactoryBeanSupport<T, S, ID>{

  private @NonNull String[] scanPackages;
  private @NonNull String mqsConfiguration;
  private @NonNull String readToTime;
  private @NonNull NucleoDB.DBType dbType;


  private static @Nullable NucleoDB nucleoDB = null;

  /**
   * Creates a new {@link RepositoryFactoryBeanSupport} for the given repository interface.
   *
   * @param repositoryInterface must not be {@literal null}.
   */
  protected NDBRepositoryFactoryBean(Class<? extends T> repositoryInterface) throws IncorrectDataEntryClassException, MissingDataEntryConstructorsException {
    super(repositoryInterface);
  }
  @Override
  protected RepositoryFactorySupport createRepositoryFactory() {
    if(nucleoDB==null) {
      try {
        MQSConfiguration mqsConfigurationInstance = (MQSConfiguration) Class.forName(mqsConfiguration).getDeclaredConstructor().newInstance();

        if(readToTime!=null && !readToTime.isEmpty()) {
          nucleoDB = new NucleoDB(
              dbType,
              readToTime,
              c -> c.setMqsConfiguration(mqsConfigurationInstance),
              c -> c.setMqsConfiguration(mqsConfigurationInstance),
              scanPackages
          );
        }else{
          nucleoDB = new NucleoDB(
              dbType,
              c -> c.setMqsConfiguration(mqsConfigurationInstance),
              c -> c.setMqsConfiguration(mqsConfigurationInstance),
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
    return new NDBRepositoryFactory(nucleoDB);
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
  //  @Bean
//  public NucleoDB createNucleoDB(ApplicationContext ctx) throws IncorrectDataEntryClassException, MissingDataEntryConstructorsException {
//
//    return
//
//  }
}
