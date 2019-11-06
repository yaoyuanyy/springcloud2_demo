package com.skyler.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@RestController
public class EmployeeController {

	@SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @RequestMapping(value = "/getZone" ,method = RequestMethod.GET)
    public String getZone() {
        return null;
    }

}
