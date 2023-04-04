package id.customer.core.controller;

import id.customer.core.models.Result;
import id.customer.core.service.JobsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@CrossOrigin
@RestController
@RequestMapping("/jobs")
public class JobsController {

    @Autowired
    JobsService jobsService;

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public Result inquiryListJob() throws NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        return jobsService.inquiryListJob();
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public Result getJobById(@PathVariable("id") String id) throws NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        return jobsService.getJobById(id);
    }
}
