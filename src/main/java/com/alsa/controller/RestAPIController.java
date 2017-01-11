package com.alsa.controller;

import com.alsa.WebConstants;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by alsa on 03.11.2016.
 */

@RestController
public class RestAPIController {
    @RequestMapping(value = WebConstants.PING, method = RequestMethod.GET)
    public long gap() {
        return 0;
    }
}
