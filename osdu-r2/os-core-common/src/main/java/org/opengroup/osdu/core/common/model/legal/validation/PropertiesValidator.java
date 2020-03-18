package org.opengroup.osdu.core.common.model.legal.validation;

import org.opengroup.osdu.core.common.model.legal.Properties;
import org.opengroup.osdu.core.common.model.legal.validation.rules.Rule;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

//This will hold validation for the Properties model for the properties that rely on other property values for their validation rules
//any properties in this model that do not rely on other properties will have their own validator
public class PropertiesValidator implements ConstraintValidator<ValidLegalTagProperties, Properties> {

    private final List<Rule> ruleSet;

    public PropertiesValidator(List<Rule> ruleSet){
        this.ruleSet = ruleSet;
    }

    @Override
    public void initialize(ValidLegalTagProperties constraintAnnotation) {
        //needed by interface - we don't use
    }

    @Override
    public boolean isValid(Properties legalTagProperties, ConstraintValidatorContext context) {
        for (Rule rule : ruleSet) {
            if(rule.shouldCheck(legalTagProperties)) {
                if (!rule.isValid(legalTagProperties, context)) {
                    return false;
                }
            }
        }
        return true;
    }
}
