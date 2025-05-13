package com.rtb.tenant.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
@CrossOrigin(origins = "*", allowedHeaders = "*")

@RequestMapping(value = "/api/v1")
public abstract class BaseController {

}