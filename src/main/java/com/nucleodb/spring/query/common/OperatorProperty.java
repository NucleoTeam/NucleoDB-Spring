package com.nucleodb.spring.query.common;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nucleodb.spring.query.ParserConstants.conditional;
import static com.nucleodb.spring.query.ParserConstants.propertyNotExpression;
import static com.nucleodb.spring.query.ParserConstants.propertyNotRegexPattern;

public class OperatorProperty implements Serializable {
    String expression = "=";
    String propertyName;

    boolean not;

    public OperatorProperty(String propertyName) {
      not = propertyName.matches(propertyNotExpression);

      for (Map.Entry<String, String[]> stringEntry : conditional.entrySet()) {

        Pattern regexPattern = Pattern.compile(stringEntry.getKey());
        Matcher matcher = regexPattern.matcher(propertyName);
        if(matcher.find()) {
          if(not) {
            Matcher matcherNot = propertyNotRegexPattern.matcher(propertyName);
            if(matcherNot.find())
              propertyName = matcherNot.group(1);
          }else {
            propertyName = matcher.group(1);
          }
          expression = stringEntry.getValue()[0];
          break;
        }
      }
      this.propertyName = propertyName;

    }

    public boolean isNot() {
      return not;
    }

    public void setNot(boolean not) {
      this.not = not;
    }

    public String getExpression() {
      return expression;
    }

    public void setExpression(String expression) {
      this.expression = expression;
    }

    public String getPropertyName() {
      return propertyName;
    }

    public void setPropertyName(String propertyName) {
      this.propertyName = propertyName;
    }
}