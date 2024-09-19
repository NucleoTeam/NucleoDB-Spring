package com.nucleodb.spring.config;

import com.nucleodb.library.NucleoDB;
import com.nucleodb.library.mqs.config.MQSConfiguration;
import com.nucleodb.library.mqs.config.MQSSettings;
import com.nucleodb.library.mqs.kafka.KafkaConfiguration;
import com.nucleodb.spring.NDBRepositoryFactoryBean;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Import({NDBRepositoryRegistrar.class, NDBInstance.class})
public @interface EnableNDBRepositories{
  NucleoDB.DBType dbType() default NucleoDB.DBType.NO_LOCAL;

  String mqsConfiguration() default "com.nucleodb.library.mqs.kafka.KafkaConfiguration";

  String nodeFilterConnection() default "com.nucleodb.library.database.tables.connection.NodeFilter";
  String nodeFilterDataEntry() default "com.nucleodb.library.database.tables.table.NodeFilter";

  String readToTime() default "";

  String[] scanPackages() default {"com.nucleocore.library.database.tables.connection"};
  String[] value() default {};

  /**
   * Base packages to scan for annotated components. {@link #value()} is an alias for (and mutually exclusive with) this
   * attribute. Use {@link #basePackageClasses()} for a type-safe alternative to String-based package names.
   */
  String[] basePackages() default "";

  /**
   * Type-safe alternative to {@link #basePackages()} for specifying the packages to scan for annotated components. The
   * package of each class specified will be scanned. Consider creating a special no-op marker class or interface in
   * each package that serves no purpose other than being referenced by this attribute.
   */
  Class<?>[] basePackageClasses() default {};

  /**
   * Specifies which types are eligible for component scanning. Further narrows the set of candidate components from
   * everything in {@link #basePackages()} to everything in the base packages that matches the given filter or filters.
   */
  Filter[] includeFilters() default {};

  /**
   * Specifies which types are not eligible for component scanning.
   */
  Filter[] excludeFilters() default {};

  /**
   * Returns the postfix to be used when looking up custom repository implementations. Defaults to {@literal Impl}. So
   * for a repository named {@code UserRepository} the corresponding implementation class will be looked up scanning for
   * {@code UserRepositoryImpl}.
   */
  String repositoryImplementationPostfix() default "Impl";

  /**
   * Configures the location of where to find the Spring Data named queries properties file. Will default to
   * {@code META-INF/cassandra-named-queries.properties}.
   */
  String namedQueriesLocation() default "";

  /**
   * Returns the key of the {@link QueryLookupStrategy} to be used for lookup queries for query methods. Defaults to
   * {@link Key#CREATE_IF_NOT_FOUND}.
   */
  QueryLookupStrategy.Key queryLookupStrategy() default Key.CREATE_IF_NOT_FOUND;

  /**
   * Returns the {@link FactoryBean} class to be used for each repository instance. Defaults to
   * {@link NDBRepositoryFactoryBean}.
   */
  Class<?> repositoryFactoryBeanClass() default NDBRepositoryFactoryBean.class;

  /**
   * Configure the repository base class to be used to create repository proxies for this particular configuration.
   *
   * @since 1.3
   */
  Class<?> repositoryBaseClass() default DefaultRepositoryBaseClass.class;

  /**
   * Configures whether nested repository-interfaces (e.g. defined as inner classes) should be discovered by the
   * repositories infrastructure.
   */
  boolean considerNestedRepositories() default true;
}
