package org.opengroup.osdu.core.common.model.legal.validation;

import org.opengroup.osdu.core.common.model.legal.DataTypeValues;
import org.opengroup.osdu.core.common.model.http.RequestInfo;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DataTypeValidator implements ConstraintValidator<ValidDataType, String> {

    private RequestInfo requestInfo;
    private DataTypeValues dataTypeValues = new DataTypeValues();

    @Inject
    public DataTypeValidator(RequestInfo requestInfo){
        this.requestInfo = requestInfo;
    }

    @Override
    public void initialize(ValidDataType constraintAnnotation) {
        //needed by interface - we don't use
    }

    @Override
    public boolean isValid(String dataType, ConstraintValidatorContext context) {
        String ruleSet = requestInfo.getComplianceRuleSet();
        return dataType == null ? false :
                dataTypeValues.getDataTypeValues(ruleSet).stream().anyMatch(dataType::equalsIgnoreCase);
    }

    //to enable integration tests
    public void setRequestInfo(RequestInfo requestInfo){
        this.requestInfo = requestInfo;
    }
}
