package project.footballinfo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ApiResponse {

    /**
     * API 요청 (URL을 통한)
     *
     * @param requestURL : API 요청 URL
     * @return : API 응답값
     */

    @Value("${api-key}")
    private String apiKey;

    public ResponseEntity<Map> getAPIResponse(String requestURL) {
        RestTemplate restTemplate = new RestTemplate();
        RequestEntity<Void> req = RequestEntity
                .get(requestURL)
                .header("X-Auth-Token", apiKey)
                .build();
        ResponseEntity<Map> response = restTemplate.exchange(req, Map.class);
        return response;
    }
}
