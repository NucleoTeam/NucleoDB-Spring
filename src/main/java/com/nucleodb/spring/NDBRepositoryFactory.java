package com.nucleodb.spring;

import com.nucleodb.library.NucleoDB;
import com.nucleodb.library.database.tables.connection.Connection;
import com.nucleodb.library.database.tables.table.DataEntry;
import com.nucleodb.spring.impl.NDBConnectionRepositoryImpl;
import com.nucleodb.spring.impl.NDBDataEntryRepositoryImpl;
import com.nucleodb.spring.mapping.NDBEntityInformationConnection;
import com.nucleodb.spring.mapping.NDBEntityInformationDataEntry;
import com.nucleodb.spring.query.exec.NDBDataEntryRepositoryQuery;
import com.nucleodb.spring.types.NDBConnRepository;
import com.nucleodb.spring.types.NDBDataRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.mapping.context.AbstractMappingContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class NDBRepositoryFactory extends RepositoryFactorySupport{
  private static final SpelExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

  private final NucleoDB nucleoDB;

  private final AbstractMappingContext mappingContext;

  /**
   * Create a new {@link NDBRepositoryFactory} with the given {@link NucleoDB}.
   *
   * @param nucleoDB       must not be {@literal null}
   * @param mappingContext
   */
  public NDBRepositoryFactory(NucleoDB nucleoDB, AbstractMappingContext<?, ?> mappingContext) {

    Assert.notNull(nucleoDB, "NucleoDB must not be null");

    this.nucleoDB = nucleoDB;
    this.mappingContext = mappingContext;

  }


  @Override
  public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
    if(DataEntry.class.isAssignableFrom(domainClass)) {
      return new NDBEntityInformationDataEntry(domainClass);
    }else if(Connection.class.isAssignableFrom(domainClass)) {
      return new NDBEntityInformationConnection(domainClass);
    }
    return null;
  }

  @Override
  protected Object getTargetRepository(RepositoryInformation repositoryInformation) {
    return getTargetRepositoryViaReflection(repositoryInformation, nucleoDB, repositoryInformation.getDomainType());
  }

  @Override
  protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
    if(NDBDataRepository.class.isAssignableFrom(metadata.getRepositoryInterface())){
      return NDBDataEntryRepositoryImpl.class;
    }else if(NDBConnRepository.class.isAssignableFrom(metadata.getRepositoryInterface())){
      return NDBConnectionRepositoryImpl.class;
    }
    return null;
  }


  @Override
  protected Optional<QueryLookupStrategy> getQueryLookupStrategy(
      @Nullable QueryLookupStrategy.Key key,
      QueryMethodEvaluationContextProvider evaluationContextProvider
  ) {
    return Optional.of(new NDBQueryLookupStrategy(nucleoDB, evaluationContextProvider));
  }

  private static class NDBQueryLookupStrategy implements QueryLookupStrategy {

    private final QueryMethodEvaluationContextProvider evaluationContextProvider;
    private final NucleoDB nucleoDB;

    NDBQueryLookupStrategy(NucleoDB nucleoDB, QueryMethodEvaluationContextProvider evaluationContextProvider) {
      this.nucleoDB = nucleoDB;
      this.evaluationContextProvider = evaluationContextProvider;
    }

    private static Map<String, RepositoryQuery> cachedHandlers = new TreeMap<>();

    private RepositoryQuery getOrCreateRepositoryQueryHandler(Method method, RepositoryMetadata metadata){
      RepositoryQuery repositoryQuery = cachedHandlers.get(metadata.getRepositoryInterface().getName() + "." + method.getName());
      if(repositoryQuery!=null){
        return repositoryQuery;
      }
      if(NDBDataRepository.class.isAssignableFrom(metadata.getRepositoryInterface())) {
        repositoryQuery = new NDBDataEntryRepositoryQuery(this.nucleoDB, metadata.getDomainType(), method);
      }else if(NDBConnRepository.class.isAssignableFrom(metadata.getRepositoryInterface())){
        return null;// need to implement
      }
      cachedHandlers.put(metadata.getRepositoryInterface().getName() + "." + method.getName(), repositoryQuery);
      return repositoryQuery;
    }

    @Override
    public RepositoryQuery resolveQuery(
        Method method,
        RepositoryMetadata metadata,
        ProjectionFactory factory,
        NamedQueries namedQueries
    ) {
      return getOrCreateRepositoryQueryHandler(method, metadata);
    }
  }

}