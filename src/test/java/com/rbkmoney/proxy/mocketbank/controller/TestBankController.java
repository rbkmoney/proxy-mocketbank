package com.rbkmoney.proxy.mocketbank.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.rbkmoney.proxy.mocketbank.DeadlineTest.SLEEP_FOR_BANK_CONTROLLER;

@RestController
@RequestMapping(value = "/mpi")
public class TestBankController {

    @RequestMapping(value = "verifyEnrollment", method = RequestMethod.POST)
    public String verifyEnrollment(@RequestParam(value = "pan", required = true) String pan,
                                   @RequestParam(value = "year", required = true) String year,
                                   @RequestParam(value = "month", required = true) String month) throws Exception {
        Thread.sleep(SLEEP_FOR_BANK_CONTROLLER);
        throw new RuntimeException("Runtime exception from bank!");
    }
}
