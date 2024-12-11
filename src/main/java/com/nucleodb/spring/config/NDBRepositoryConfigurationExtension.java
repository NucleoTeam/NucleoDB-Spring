package com.nucleodb.spring.config;

import com.nucleodb.library.NucleoDB;
import com.nucleodb.library.database.tables.annotation.Conn;
import com.nucleodb.library.database.tables.annotation.Table;
import com.nucleodb.library.mqs.config.MQSConfiguration;
import com.nucleodb.spring.NDBRepositoryFactoryBean;
import com.nucleodb.spring.mapping.NDBMappingContext;
import com.nucleodb.spring.types.NDBConnRepository;
import com.nucleodb.spring.types.NDBDataRepository;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.repository.config.AnnotationRepositoryConfigurationSource;
import org.springframework.data.repository.config.RepositoryConfigurationExtensionSupport;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.data.repository.config.XmlRepositoryConfigurationSource;
import org.springframework.data.repository.core.RepositoryMetadata;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public class NDBRepositoryConfigurationExtension extends RepositoryConfigurationExtensionSupport{
  @Override
  public String getModuleName() {
    return "NucleoDB";
  }

  @Override
  protected String getModulePrefix() {
    return "nucleodb";
  }


  @Override
  public String getRepositoryFactoryBeanClassName() {
    return NDBRepositoryFactoryBean.class.getName();
  }

  @Override
  public void registerBeansForRoot(BeanDefinitionRegistry registry, RepositoryConfigurationSource configurationSource) {
    // Only register ndbMappingContext if it doesn't already exist
    if (!registry.containsBeanDefinition("ndbMappingContext")) {
      RootBeanDefinition beanDefinition = new RootBeanDefinition(NDBMappingContext.class);
      registry.registerBeanDefinition("ndbMappingContext", beanDefinition);
    }
    super.registerBeansForRoot(registry, configurationSource);
  }

  @Override
  public void postProcess(BeanDefinitionBuilder builder, AnnotationRepositoryConfigurationSource config) {
    builder.addAutowiredProperty("publisher");
    builder.addAutowiredProperty("nucleoDB");
    builder.addDependsOn("ndbMappingContext");

    Optional<String> mqsConfig = config.getAttribute("mqsConfiguration");
    if(mqsConfig.isPresent()){
      builder.addPropertyValue("mqsConfiguration", mqsConfig.get());
    }
    Optional<String> readToTime = config.getAttribute("readToTime");
    if(readToTime.isPresent()){
      builder.addPropertyValue("readToTime", readToTime.get());
    }
    Optional<String> nodeFilterConnection = config.getAttribute("nodeFilterConnection");
    if(nodeFilterConnection.isPresent()){
      builder.addPropertyValue("nodeFilterConnection", nodeFilterConnection.get());
    }
    Optional<String> nodeFilterDataEntry = config.getAttribute("nodeFilterDataEntry");
    if(nodeFilterDataEntry.isPresent()){
      builder.addPropertyValue("nodeFilterDataEntry", nodeFilterDataEntry.get());
    }
    Optional<NucleoDB.DBType> dbType = config.getAttribute("dbType", NucleoDB.DBType.class);
    if(dbType.isPresent()){
      builder.addPropertyValue("dbType", dbType.get());
    }
    Optional<String[]> scanPackages = config.getAttribute("scanPackages", String[].class);
    if(scanPackages.isPresent()){
      builder.addPropertyValue("scanPackages", scanPackages.get());
    }
  }

  @Override
  protected Collection<Class<? extends Annotation>> getIdentifyingAnnotations() {
    return Arrays.asList(Table.class, Conn.class);
  }

  @Override
  protected Collection<Class<?>> getIdentifyingTypes() {
    return Arrays.asList(NDBDataRepository.class, NDBConnRepository.class);
  }


  @Override
  protected boolean useRepositoryConfiguration(RepositoryMetadata metadata) {
    return !metadata.isReactiveRepository();
  }
}