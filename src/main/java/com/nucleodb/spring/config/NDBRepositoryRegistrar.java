package com.nucleodb.spring.config;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

import java.lang.annotation.Annotation;

public class NDBRepositoryRegistrar extends RepositoryBeanDefinitionRegistrarSupport{
  @Override
  protected Class<? extends Annotation> getAnnotation() {
    return EnableNDBRepositories.class;
  }

  @Override
  protected RepositoryConfigurationExtension getExtension() {
    return new NDBRepositoryConfigurationExtension();
  }
}
