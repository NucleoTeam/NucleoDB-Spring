package com.nucleodb.spring.query;

import com.nucleodb.spring.query.common.ConditionOperation;
import com.nucleodb.spring.query.common.LookupOperation;
import com.nucleodb.spring.query.common.OperatorProperty;
import com.nucleodb.spring.query.common.Operation;
import com.nucleodb.spring.query.common.QueryOperation;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nucleodb.spring.query.ParserConstants.operator;
import static com.nucleodb.spring.query.ParserConstants.prefix;

public class QueryParser {
  private static Operation recursive(String stringMatch){
    for (Map.Entry<String, String[]> stringEntry : operator.entrySet()) {
      String regexMatch = stringEntry.getKey();
      Pattern regexPattern = Pattern.compile(regexMatch);
      Matcher matcher = regexPattern.matcher(stringMatch);
      while(matcher.find()) {
        stringMatch = stringMatch.replace(matcher.group(0), "");
        return new LookupOperation(new OperatorProperty(stringMatch), new ConditionOperation(stringEntry.getValue()[0], recursive(matcher.group(1))));
      }
    }
    // handle expressions
    return new LookupOperation(new OperatorProperty(stringMatch));
  }

  public static QueryOperation parse(Method method){
    String stringMatch = method.getName();
    for (Map.Entry<String, String[]> entry : prefix.entrySet()) {
      String regexMatch = entry.getKey();
      if (!stringMatch.matches(regexMatch))
        continue;
      Pattern regexPattern = Pattern.compile(regexMatch);
      Matcher matcher = regexPattern.matcher(stringMatch);
      QueryOperation queryOperation = new QueryOperation();
      if (matcher.find()) {
        queryOperation.setMethod(entry.getValue()[0]);
        for (int i = 1; i < entry.getValue().length; i++) {
          if ("operations".equals(entry.getValue()[i])) {
            queryOperation.setNext(recursive(matcher.group(i)));
          }
        }
      }
      queryOperation.setReturnType(method.getGenericReturnType());
      return queryOperation;
    }
    return null;
  }
}
