package com.nadav.onevid.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nadav.onevid.dto.UserInfoDto;
import com.nadav.onevid.model.User;
import com.nadav.onevid.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    @Value("${auth0.userinfoEndpoint}")
    private String userInfoEndpoint;

    private final UserRepository userRepository;

    public void registerUser(String tokenValue){
        //make a call to the userInfo Endpoint

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(userInfoEndpoint))
                .setHeader("Authorization",String.format("Bearer %s", tokenValue))
                .build();

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();

        try {
            HttpResponse<String> responseString = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String body = responseString.body();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
            UserInfoDto userInfoDto = objectMapper.readValue(body, UserInfoDto.class);

            User user = new User();
            user.setFirstName(userInfoDto.getGivenName());
            user.setLastName(userInfoDto.getFamilyName());
            user.setFullName(userInfoDto.getName());
            user.setEmailAddress(userInfoDto.getEmail());
            user.setSub(userInfoDto.getSub());

            userRepository.save(user);


        }catch (Exception exception){
            throw new RuntimeException("Exception occurred while registering user",exception);
        }
        //Fetch user details and add the to the DB
    }

}
