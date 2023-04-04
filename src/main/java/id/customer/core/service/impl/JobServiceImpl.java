package id.customer.core.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.customer.core.dto.JobDto;
import id.customer.core.models.Result;
import id.customer.core.service.JobsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class JobServiceImpl implements JobsService {

    @Value("${recruitment-position}")
    private String urlRecruitment;

    @Value("${recruitment-position-by-id}")
    private String urlRecruitmentById;

    private HttpClientBuilder httpClientBuilder;
    @Override
    public Result inquiryListJob() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        Result result = new Result();
        List<JobDto> jobs = new ArrayList<>();
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        httpClientBuilder = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory);
        try(CloseableHttpClient httpClient = httpClientBuilder.build()){
            HttpGet httpGet = new HttpGet(urlRecruitment);
            httpGet.setHeader("Content-type", String.valueOf(MediaType.APPLICATION_JSON));
            try(CloseableHttpResponse httpResponse = httpClient.execute(httpGet)){
                HttpEntity entity = httpResponse.getEntity();
                JsonNode jsonResponse = mapper.readTree(EntityUtils.toString(entity));
                if(jsonResponse.isEmpty()){
                    result.setSuccess(false);
                    result.setMessage("Error: data not found");
                    result.setCode(HttpStatus.BAD_REQUEST.value());
                }else{
                    for(int i=0;i<jsonResponse.size();i++){
                        JobDto job = new JobDto();
                        job.id = jsonResponse.get(i).get("id").toString().replaceAll("\"","");
                        job.type = jsonResponse.get(i).get("type").toString().replaceAll("\"","");
                        job.url = jsonResponse.get(i).get("url").toString().replaceAll("\"","");
                        job.createdAt = jsonResponse.get(i).get("created_at").toString().replaceAll("\"","");
                        job.company = jsonResponse.get(i).get("company").toString().replaceAll("\"","");
                        job.companyUrl = jsonResponse.get(i).get("company_url").toString().replaceAll("\"","");
                        job.location = jsonResponse.get(i).get("location").toString().replaceAll("\"","");
                        job.title = jsonResponse.get(i).get("title").toString().replaceAll("\"","");
                        job.description = jsonResponse.get(i).get("description").toString().replaceAll("\"","");
                        job.howToApply = jsonResponse.get(i).get("how_to_apply").toString().replaceAll("\"","");
                        job.companyLogo = jsonResponse.get(i).get("company_logo").toString().replaceAll("\"","");
                        jobs.add(job);
                    }
                }

            }
        }
        result.setData(jobs);
        result.setMessage("Success");
        return result;
    }

    @Override
    public Result getJobById(String id) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        Result result = new Result();
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        httpClientBuilder = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory);
        try(CloseableHttpClient httpClient = httpClientBuilder.build()){
            HttpGet httpGet = new HttpGet(urlRecruitmentById.replace("{id}",id));
            httpGet.setHeader("Content-type", String.valueOf(MediaType.APPLICATION_JSON));
            try(CloseableHttpResponse httpResponse = httpClient.execute(httpGet)){
                HttpEntity entity = httpResponse.getEntity();
                JsonNode jsonResponse = mapper.readTree(EntityUtils.toString(entity));
                if(jsonResponse.isEmpty()){
                    result.setSuccess(false);
                    result.setMessage("Error: data not found");
                    result.setCode(HttpStatus.BAD_REQUEST.value());
                }else{
                    JobDto job = new JobDto();
                    job.id = jsonResponse.get("id").toString().replaceAll("\"","");
                    job.type = jsonResponse.get("type").toString().replaceAll("\"","");
                    job.url = jsonResponse.get("url").toString().replaceAll("\"","");
                    job.createdAt = jsonResponse.get("created_at").toString().replaceAll("\"","");
                    job.company = jsonResponse.get("company").toString().replaceAll("\"","");
                    job.companyUrl = jsonResponse.get("company_url").toString().replaceAll("\"","");
                    job.location = jsonResponse.get("location").toString().replaceAll("\"","");
                    job.title = jsonResponse.get("title").toString().replaceAll("\"","");
                    job.description = jsonResponse.get("description").toString().replaceAll("\"","");
                    job.howToApply = jsonResponse.get("how_to_apply").toString().replaceAll("\"","");
                    job.companyLogo = jsonResponse.get("company_logo").toString().replaceAll("\"","");
                    result.setData(job);
                    result.setMessage("Success");
                }

            }
        }
        return result;
    }
}
