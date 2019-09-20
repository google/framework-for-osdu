package com.osdu.model.osdu.delivery.input;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;

@Data
public class Srns {

    @JsonAlias("SRNS")
    private List<String> srns;

}
