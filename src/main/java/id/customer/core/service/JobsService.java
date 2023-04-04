package id.customer.core.service;

import id.customer.core.models.Result;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public interface JobsService {

    Result inquiryListJob() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException;

    Result getJobById(String id)throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException;
}
