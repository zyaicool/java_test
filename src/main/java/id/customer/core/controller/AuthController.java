package id.customer.core.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import id.customer.core.dto.LoginRequestDto;
import id.customer.core.models.Result;
import id.customer.core.repository.UserRepository;
import id.customer.core.utils.StringUtil;
import id.customer.core.dto.ChangePasswordRequestDto;
import id.customer.core.dto.SignupRequestDto;
import id.customer.core.dto.TokenRefreshRequestDto;
import id.customer.core.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  UserService service;

  @Autowired
  StringUtil stringUtil;

  @Autowired
  HttpServletRequest request;

  @Autowired
  UserRepository userRepository;

  @Autowired
  PasswordEncoder encoder;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(
          @Valid 
          @RequestBody LoginRequestDto loginRequestDto) {
    String uri = stringUtil.getLogParam(request);
    logger.info(uri);

    Result result = new Result();

    Boolean isVerified = userRepository.findByUsername(loginRequestDto.getUsername()).get().isVerified();

    if(isVerified.equals(false)){
      result.setMessage("Akun Belum Ter-Verifikasi, Silahkan Verifikasi Akun Terlebih Dahulu Melalui Tautan yang Dikirim Melalui Email untuk Dapat Sign In!");
      result.setCode(HttpStatus.BAD_REQUEST.value());
      return ResponseEntity
              .badRequest()
              .body(result);
    }

    Boolean isActive = userRepository.findByUsername(loginRequestDto.getUsername()).get().isIsActive();

    if(isActive.equals(false)){
      result.setMessage("Akun Belum Aktif, Silahkan Hubungi Admin untuk Aktivasi Akun Terlebih Dahulu agar Dapat Sign In!");
      result.setCode(HttpStatus.BAD_REQUEST.value());
      return ResponseEntity
              .badRequest()
              .body(result);
    }
    
    return service.signIn(loginRequestDto, uri);
  }

  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(
          @Valid 
          @RequestParam(value = "user activation", defaultValue = "Aktif", required = true) String userActivation,
          @RequestBody SignupRequestDto signUpRequestDto) {//Password pada Signup Request di generate auto pada UserServiceImpl dan ikut disend pada email
    service.createUser(signUpRequestDto); //Format Isian untuk memberikan id, token, email, dan password disamakan isiannya dengan Forgot Password di UserServiceImpl

    String tokenVerification = userRepository.findByEmail(signUpRequestDto.getEmail()).get().getTokenVerification();
    String statusAktif = "Aktif";
    statusAktif = userActivation;

    Result result = new Result();
    if(statusAktif.toUpperCase().equals("AKTIF")){
      String uri = stringUtil.getLogParam(request);
      logger.info(uri);

      service.active(tokenVerification, uri);

      result.setMessage("User telah berhasil terdaftar dan akun sudah teraktivasi serta terverifikasi!");
      result.setCode(HttpStatus.OK.value());
      return ResponseEntity.ok(result);  
    }

    result.setMessage("User telah berhasil terdaftar, tetapi akun belum teraktivasi dan terverifikasi!");
    result.setCode(HttpStatus.OK.value());
    return ResponseEntity.ok(result);
  }

  @PostMapping("/signout")
  public Result logout(
          @RequestParam(value = "id", defaultValue = "0", required = true) long id) {
    String uri = stringUtil.getLogParam(request);
    logger.info(uri);

    return service.signOut(id, uri);
  }

  @GetMapping("/active")
  public Result active(
          @RequestParam(value = "tokenVerification", defaultValue = "", required = true) String tokenVerification) {
    String uri = stringUtil.getLogParam(request);
    logger.info(uri);

    return service.active(tokenVerification, uri);
  }

  @PatchMapping("/change_password")
  public ResponseEntity<?> changePassword(
          @RequestParam(value = "id", defaultValue = "0", required = true) long id,
          @RequestParam(value = "password lama", defaultValue = "", required = true) String oldPassword,
          @RequestParam(value = "password baru", defaultValue = "", required = true) String newPassword) {
    String uri = stringUtil.getLogParam(request);
    logger.info(uri);

    String password = userRepository.findById(id).getPassword();

    Result result = new Result();
    if(!encoder.matches(oldPassword, password)){
      result.setMessage("Password Lama yang Dimasukkan Tidak Sesuai");
      result.setCode(HttpStatus.BAD_REQUEST.value());
      return ResponseEntity
              .badRequest()
              .body(result);
    }
    if(encoder.matches(newPassword, password)){
      result.setMessage("Password Baru Tidak Boleh Sama dengan Password Lama");
      result.setCode(HttpStatus.BAD_REQUEST.value());
      return ResponseEntity
              .badRequest()
              .body(result);
    }
    return service.changePassword(id, newPassword, uri);
  }

  @PostMapping("/change_password_forgot")
  public ResponseEntity<?> changePasswordForget(
          @RequestBody ChangePasswordRequestDto param) throws JsonProcessingException {
    return service.changePasswordForgot(param);
  }

  @PostMapping("/forgot_password")
  public ResponseEntity<?> forgotPassword(
          @RequestParam(value = "email", defaultValue = "", required = true) String email) throws IOException {
    String uri = stringUtil.getLogParam(request);
    logger.info(uri);

    return service.forgotPassword(email, uri);
  }

  @PostMapping("/refreshtoken")
  public ResponseEntity<?> refreshtoken(
          @Valid 
          @RequestBody TokenRefreshRequestDto request) {
    return service.refreshToken(request);
  }
}
