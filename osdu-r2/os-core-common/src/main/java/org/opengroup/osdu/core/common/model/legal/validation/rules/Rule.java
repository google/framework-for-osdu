package org.opengroup.osdu.core.common.model.legal.validation.rules;

import com.google.common.base.Strings;
import org.opengroup.osdu.core.common.model.legal.Properties;

import javax.validation.ConstraintValidatorContext;

public abstract class Rule {
    public abstract boolean shouldCheck(Properties properties);
    public boolean isValid(Properties properties, ConstraintValidatorContext context){
        String errors = hasError(properties);
        if(Strings.isNullOrEmpty(errors))
            return true;
        else
            return createError(context, errors);
    }

    protected abstract String hasError(Properties properties);
    private boolean createError(ConstraintValidatorContext context, String msg) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(msg)
                .addConstraintViolation();
        return false;
    }

}
