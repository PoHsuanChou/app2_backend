package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.GoogleSSOMessage;
import org.example.dto.GoogleSSOResponse;
import org.example.dto.GoogleTokenRequest;
import org.example.entity.User;
import org.example.service.GoogleAuthService;
import org.example.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class GoogleAuthController {

     private final GoogleAuthService googleAuthService;
     private final JwtService jwtService;

     @PostMapping("/google")
     public ResponseEntity<GoogleSSOResponse> authenticateWithGoogle(@RequestBody GoogleTokenRequest request) {
          String idToken = request.getIdToken();

          GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                  new NetHttpTransport(), GsonFactory.getDefaultInstance())
                  .setAudience(Collections.singletonList("776267765563-f9rjs6jav75c50mvsdupk9d5s8qrqel8.apps.googleusercontent.com")) // 記得替換
                  .build();

          try {
               GoogleIdToken idTokenObj = verifier.verify(idToken);
               if (idTokenObj != null) {
                    GoogleIdToken.Payload payload = idTokenObj.getPayload();
                    String email = payload.getEmail();
                    // 檢查使用者是否已經存在
                    User user = googleAuthService.googleFindUser(email);
                    if(user == null){
                         return new ResponseEntity<>(new GoogleSSOResponse(true, GoogleSSOMessage.NEW_USER.getMessage(), null, null,true,email), HttpStatus.OK);
                    }else{
                         // 產生 JWT token
                         String jwt = jwtService.generateToken(user);
                         return new ResponseEntity<>(new GoogleSSOResponse(true, GoogleSSOMessage.AUTHENTICATED.getMessage(), jwt, user.getId(),true,email), HttpStatus.OK);
                    }

               } else {
                    return new ResponseEntity<>(new GoogleSSOResponse(false, GoogleSSOMessage.AUTHENTICATION_FAILED.getMessage(), null, null,false,null), HttpStatus.OK);
               }
          } catch (Exception e) {
               return new ResponseEntity<>(new GoogleSSOResponse(false, GoogleSSOMessage.INVALID_ID_TOKEN.getMessage(), null, null,false,null), HttpStatus.INTERNAL_SERVER_ERROR);
          }
     }

}
