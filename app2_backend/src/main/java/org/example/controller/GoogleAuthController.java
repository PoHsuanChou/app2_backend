package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.GoogleSSOResponse;
import org.example.dto.GoogleTokenRequest;
import org.example.entity.User;
import org.example.service.GoogleAuthService;
import org.example.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
                  .setAudience(Collections.singletonList("YOUR_GOOGLE_CLIENT_ID")) // 記得替換
                  .build();

          try {
               GoogleIdToken idTokenObj = verifier.verify(idToken);
               if (idTokenObj != null) {
                    GoogleIdToken.Payload payload = idTokenObj.getPayload();

                    String userId = payload.getSubject();
                    String email = payload.getEmail();
                    String name = (String) payload.get("name");
                    String pictureUrl = (String) payload.get("picture");

                    // 檢查使用者是否已經存在，若無則創建
                    User user = googleAuthService.findOrCreateUser(userId, email, name, pictureUrl);

                    // 產生 JWT token
                    String jwt = jwtService.generateToken(user);


                    return new ResponseEntity<>(new GoogleSSOResponse(true, "Authenticated", jwt, user), HttpStatus.OK);
               } else {
                    return new ResponseEntity<>(new GoogleSSOResponse(false, "Invalid ID Token", null, null), HttpStatus.OK);
               }
          } catch (Exception e) {
               return new ResponseEntity<>(new GoogleSSOResponse(false, "Authentication failed", null, null), HttpStatus.INTERNAL_SERVER_ERROR);
          }
     }
}
