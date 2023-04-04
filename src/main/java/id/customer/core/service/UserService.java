package id.customer.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import id.customer.core.dto.*;
import id.customer.core.models.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {
  ResponseEntity<?> createUser(SignupRequestDto signUpRequestDto);
  
  ResponseEntity<?> updateUser(UserRequestDto UserRequestDto);

  ResponseEntity<?> refreshToken(TokenRefreshRequestDto tokenRefreshRequestDto);

  ResponseEntity<?> signIn(LoginRequestDto loginRequestDto, String uri);

  ResponseEntity<?> updateProfilePictureBlob(long id, MultipartFile profilePicture, String uri);

  ResponseEntity<?> updateProfilePictureFolder(long id, MultipartFile profilePicture, String uri);

  ResponseEntity<?> deleteUser(boolean banned, long id, String uri);

  ResponseEntity<?> changePassword(long id, String password, String uri);

  ResponseEntity<?> forgotPassword(String email, String uri) throws IOException;

  ResponseEntity<?> changePasswordForgot(ChangePasswordRequestDto param) throws JsonProcessingException;

  Result signOut(long id, String uri);

  Result active(String tokenVerification, String uri);

  Result getAllUser(String uri);

  Result getUserData(String uri, String search, Integer limit, Integer page);

  Result getUserById(long id, String uri);
}
