package id.customer.core.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import id.customer.core.dto.*;
import id.customer.core.enums.EnumRole;
import id.customer.core.utils.GlobalUtil;
import id.customer.core.utils.JwtUtils;
import id.customer.core.utils.StringUtil;
import id.customer.core.utils.ValidatorUtil;
import id.customer.core.exception.TokenRefreshException;
import id.customer.core.models.EmailDetails;
import id.customer.core.response.JwtResponse;
import id.customer.core.models.RefreshToken;
import id.customer.core.models.Result;
import id.customer.core.models.Role;
import id.customer.core.models.User;
import id.customer.core.repository.RoleRepository;
import id.customer.core.repository.UserRepository;
import id.customer.core.response.TokenRefreshResponse;
import id.customer.core.service.EmailService;
import id.customer.core.service.UserService;

import java.io.IOException;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
@Service
public class UserServiceImpl implements UserService {

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  StringUtil stringUtil;

  @Autowired
  ValidatorUtil validator;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  JwtUtils jwtUtils;

  @Autowired
  RefreshTokenService refreshTokenService;

  @Autowired
  EmailService emailService;

  @Autowired
  GlobalUtil globalUtil;

  @Value("${app.expired.token.forgot-password}")
  private long jwtExpiredTokenForgotPassword;


  @Value("${customer.app.jwtExpirationMs}")
  private int jwtExpirationMs;


  private Result result;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  public Result getAllUser(String uri) {
    result = new Result();
    try {
      Map<String, List<User>> items = new HashMap<>();
      items.put("items", userRepository.findAll());
      result.setData(items);
    } catch (Exception e) {
      logger.error(stringUtil.getError(e));
    }

    return result;
  }

  @Override
  public Result getUserData(String uri, String search, Integer limit, Integer page) {
    result = new Result();

    Integer total = userRepository.totalUnbannedUser();
    if (total == null) { total = 0; }

    int jumlahPage = (int) Math.ceil(total.intValue() / (double) limit);
    
    // limit 0 or negative integer
    if (limit < 1) { limit = 1; }
    // jumlah page 0 or less, set to 1
    if (jumlahPage < 1) { jumlahPage = 1; }
    // page greater then jumlah page
    if (page > jumlahPage) { page = jumlahPage; }
    // page 0 or negative integer
    if (page < 1) { page = 1; }
    // search in null condition
    if (search == null) { search = ""; }

    try {
      Map<String, Object> items = new HashMap<>();
      List<User> user = userRepository.findUserData(search.toLowerCase(), limit.intValue(), page.intValue());
      List<SubUser> subUser = new ArrayList<>();
      for (int i = 0; i < user.size(); i++) {
        User dataUser = user.get(i);
        SubUser su = new SubUser(dataUser.getId(), dataUser.getNamaLengkap(),
                     dataUser.getEmail(), dataUser.getNoHp(), dataUser.getRole(),
                     dataUser.isIsActive());
        subUser.add(su);
      }
      items.put("items", subUser);
      items.put("totalDataResult", subUser.size());
      items.put("totalData", total);
      result.setData(items);
    } catch (Exception e) {
      logger.error(stringUtil.getError(e));
      result.setSuccess(false);
      result.setMessage(e.getMessage());
      result.setCode(HttpStatus.BAD_REQUEST.value());
      return result;
    }
    return result;
  }

  @Override
  public Result getUserById(long id, String uri) {
    result = new Result();
    try {
      User user = userRepository.findById(id);
      if (user == null) {
        result.setSuccess(false);
        result.setMessage("cannot find user");
        result.setCode(HttpStatus.BAD_REQUEST.value());
      } else {
        Map<String, User> items = new HashMap<>();
        items.put("items", userRepository.findById(id));
        result.setData(items);
      }

    } catch (Exception e) {
      logger.error(stringUtil.getError(e));
      result.setSuccess(false);
      result.setMessage(e.getMessage());
      result.setCode(HttpStatus.BAD_REQUEST.value());
      return result;
    }

    return result;
  }

  @Override
  public ResponseEntity<?> createUser(SignupRequestDto signUpRequestDto) {
    result = new Result();

    try {
      String nama = signUpRequestDto.getNamaLengkap();
      if (nama.isBlank() || nama.isEmpty()) {
        result.setMessage("Error: Nama lengkap harus diisi!");
        result.setSuccess(false);
        result.setCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(result);
      }

      String email = signUpRequestDto.getEmail();
      if (email.isBlank() || email.isEmpty()) {
        result.setMessage("Error: Email harus diisi!");
        result.setSuccess(false);
        result.setCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(result);
      }

      String nomorHp = signUpRequestDto.getNoHp();
      if (nomorHp.isBlank() || nomorHp.isEmpty()) {
        result.setMessage("Error: Nomor telepon harus diisi!");
        result.setSuccess(false);
        result.setCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(result);
      }

      if (!validator.isEmailValid(signUpRequestDto.getEmail())) {
        result.setMessage("Error: Format email tidak sesuai!");
        result.setSuccess(false);
        result.setCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity
                .badRequest()
                .body(result);
      }

      if (!validator.isPhoneValid(signUpRequestDto.getNoHp())) {
        result.setMessage("Error: Nomor telepon tidak sesuai!");
        result.setSuccess(false);
        result.setCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity
                .badRequest()
                .body(result);
      }

      Integer checkUserEmail = userRepository.existsByEmail(signUpRequestDto.getEmail());
      if (checkUserEmail != null && checkUserEmail > 0) {
        result.setMessage("Error: Email sudah digunakan!");
        result.setSuccess(false);
        result.setCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity
                .badRequest()
                .body(result);
      }

      Integer checkUserNoHp = userRepository.existsByNoHp(signUpRequestDto.getNoHp());
      if (checkUserNoHp != null && checkUserNoHp > 0) {
          result.setMessage("Error: Nomor telepon sudah digunakan!");
          result.setSuccess(false);
          result.setCode(HttpStatus.BAD_REQUEST.value());
          return ResponseEntity
                  .badRequest()
                  .body(result);
      }

      Optional<Role> role = roleRepository.findById(signUpRequestDto.getRole());
      if (!role.isPresent()) {
        result.setMessage("Error: Role tidak ditemukan!");
        result.setSuccess(false);
        result.setCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity
                .badRequest()
                .body(result);
      }

      if (signUpRequestDto.getIsActive() == null) {
        signUpRequestDto.setIsActive(true);
      }

      String password = StringUtil.alphaNumericString();
      String username = signUpRequestDto.getEmail().split("@")[0];
      String token =  StringUtil.getRandomNumberString();

      // Variable tampungan username
      String tempUsername = username;
      // Lakukan perulangan jika username sudah ada
      while (userRepository.existsByUsername(username) > 0) {
        Random rnd = new Random();
        String newUsername = tempUsername + rnd.nextInt(100);
        username = newUsername;
      }
      
      // Lakukan perulangan jika ternyata token sudah digunakan
      while (userRepository.existsByToken(token) > 0) {
        token = StringUtil.getRandomNumberString();
      }

      User user = new User(username, signUpRequestDto.getEmail(), encoder.encode(password), signUpRequestDto.getNamaLengkap(),
                          signUpRequestDto.getNoHp(), role.get(), signUpRequestDto.getIsActive(), token);
      User userResult = userRepository.save(user);

      result.setMessage("User berhasil dibuat.");
      result.setCode(HttpStatus.OK.value());
    } catch (Exception e) {
      logger.error(stringUtil.getError(e));
      result.setSuccess(false);
      result.setMessage(e.getMessage());
      result.setCode(HttpStatus.BAD_REQUEST.value());
      return ResponseEntity.badRequest().body(result);
    }
    return ResponseEntity.ok(result);
  }

  @Override
  public ResponseEntity<?> signIn(LoginRequestDto loginRequestDto, String uri) {
    result = new Result();
    // email validation
//    if(!validator.isEmailValid(loginRequestDto.getEmail())){
//      result.setMessage("Email not valid!!");
//      result.setCode(400);
//      result.setSuccess(false);
//      return ResponseEntity.badRequest().body(result);
//    }

    User getUser = userRepository.findByUsername(loginRequestDto.getUsername()).orElse(new User());

    if(getUser.getUsername() == null){
      result.setSuccess(false);
      result.setCode(HttpStatus.BAD_REQUEST.value());
      result.setMessage("User not registered");
      return ResponseEntity.ok(result);
    }

    // password validation
    //if(!validator.isPasswordValid(loginRequestDto.getPassword())){
      //result.setMessage("Password must be longer than 8 characters,use at least 1 uppercase letter,spesial characters and not contain spaces!!");
      //result.setCode(400);
      //result.setSuccess(false);
      //return ResponseEntity.badRequest().body(result);
    //}

    if (getUser.getUsername() != null) {
      Date dateNow = new Date();
      Date dateExpired = new Date((dateNow).getTime() + jwtExpirationMs);
      logger.info("cek password db "+getUser.getPassword());
      logger.info("cek password from request "+encoder.encode(loginRequestDto.getPassword()));
      if (!encoder.matches(loginRequestDto.getPassword(), getUser.getPassword())){
        result.setSuccess(true);
        result.setCode(HttpStatus.BAD_REQUEST.value());
        result.setMessage("Password salah");
        return ResponseEntity.ok(result);
      }
      Authentication authentication = authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(getUser.getUsername(), loginRequestDto.getPassword()));
      SecurityContextHolder.getContext().setAuthentication(authentication);
      String jwt = jwtUtils.generateJwtToken(authentication, dateNow, dateExpired);

      UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
      Role role = roleRepository.findByName(EnumRole.valueOf(userDetails.getAuthorities().stream().findAny().get().getAuthority())).orElse(new Role());
      RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

      result.setSuccess(true);
      result.setMessage("success");
      result.setData(new JwtResponse(jwt, refreshToken.getToken(), userDetails.getId(), userDetails.getUsername(),
              userDetails.getEmail(), role, dateExpired.getTime()));

      userRepository.setIsLogin(true, userDetails.getId());
    }

    return ResponseEntity.ok(result);
  }

  @Override
  public Result signOut(long id, String uri) {
    result = new Result();
    try {
      int status = userRepository.setIsLogin(false, id);
      if (status == 1) {
        result.setMessage("success");
      } else {
        result.setMessage("failed");
        result.setSuccess(false);
        result.setCode(HttpStatus.BAD_REQUEST.value());
      }

    } catch (Exception e) {
      logger.error(stringUtil.getError(e));
    }

    return result;
  }

  @Override
  public Result active(String tokenVerification, String uri) {
    result = new Result();
    try {
      // variable set bisa dijadikan parameter pada method
      // jika dijadikan parameter, maka method ini bisa digunakan
      // untuk aktifkan atau non-aktifkan akun
      boolean set = true;
      boolean cek = userRepository.isUserVerified(tokenVerification);

      int status = userRepository.setIsActive(true, tokenVerification);

      // // cek apakah token sudah diverifikasi atau belum
      if (set && cek == false) {
        userRepository.setIsVerified(true, tokenVerification);
      }

      if (status == 1) {
        result.setMessage("user is actived");
      } else {
        result.setMessage("failed");
        result.setSuccess(false);
        result.setCode(HttpStatus.BAD_REQUEST.value());
      }

    } catch (Exception e) {
      logger.error(stringUtil.getError(e));
    }

    return result;
  }

  @Override
  public ResponseEntity<?> changePassword(long id, String password, String uri) {
    result = new Result();

    int resultModel = userRepository.changePassword(encoder.encode(password), id);
    if (resultModel == 1) {
      return ResponseEntity.ok(result);
    } else {
      return ResponseEntity.badRequest().body(result);
    }
  }

  @Override
  public ResponseEntity<?> refreshToken(TokenRefreshRequestDto tokenRefreshRequestDto) {
    String requestRefreshToken = tokenRefreshRequestDto.getRefreshToken();
    return refreshTokenService.findByToken(requestRefreshToken)
            .map(refreshTokenService::verifyExpiration)
            .map(RefreshToken::getUser)
            .map(user -> {
              String token = jwtUtils.generateTokenFromUsername(user.getUsername());
              return ResponseEntity.ok(new TokenRefreshResponse(token, requestRefreshToken));
            })
            .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                    "Refresh token is not in database!"));
  }

  @Override
  public ResponseEntity<Result> updateUser(UserRequestDto userRequestDto) {
    result = new Result();

    try {
      Optional<User> userLama = userRepository.findById(userRequestDto.getId());

      if (!userLama.isPresent()) {
        result.setCode(HttpStatus.BAD_REQUEST.value());
        result.setSuccess(false);
        result.setMessage("Error: Id user tidak ditemukan!");
        return ResponseEntity
                .badRequest()
                .body(result);
      }

      if (!validator.isPhoneValid(userRequestDto.getNoHp())) {
        result.setMessage("Error: Format telepon tidak sesuai!");
        result.setSuccess(false);
        result.setCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity
                .badRequest()
                .body(result);
      }

      if (!validator.isEmailValid(userRequestDto.getEmail())) {
        result.setMessage("Error: Format email tidak sesuai!");
        result.setSuccess(false);
        result.setCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity
                .badRequest()
                .body(result);
      }

      User checkUserEmail = userRepository.findByEmail(userRequestDto.getEmail()).orElse(new User());
      if (checkUserEmail.getEmail()!= null && checkUserEmail.isBanned() == false && !Objects.equals(userRequestDto.getId(), checkUserEmail.getId())) {
        result.setMessage("Error: Email sudah digunakan!");
        result.setSuccess(false);
        result.setCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity
                .badRequest()
                .body(result);
      }

      Role role = roleRepository.findById(userRequestDto.getRole()).orElse(null);
      if (role == null) {
        result.setMessage("Error: Role tidak ditemukan!");
        result.setSuccess(false);
        result.setCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity
                .badRequest()
                .body(result);
      }

      /* Validasi ketika noHp sudah digunakan oleh user lain */
      Integer checkUserNoHp = userRepository.existsByNoHp(userRequestDto.getNoHp(), userRequestDto.getId());
      if (checkUserNoHp != null && checkUserNoHp > 0) {
        result.setMessage("Error: Nomor telepon sudah digunakan!");
        result.setSuccess(false);
        result.setCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity
                .badRequest()
                .body(result);
      }

      String username = userRequestDto.getEmail().split("@")[0];
      // Variable tampungan username
      String tempUsername = username;
      // Lakukan perulangan jika username sudah ada
      while (userRepository.existsByUsername(username) > 0) {
        Random rnd = new Random();
        String newUsername = tempUsername + rnd.nextInt(100);
        username = newUsername;
      }
      
      if (userRequestDto.isIsActive() == null) {
        userRequestDto.setIsActive(userLama.get().isIsActive());
      }

      User user = userLama.get();

      user.setUsername(username);
      user.setNamaLengkap(userRequestDto.getNamaLengkap());
      user.setEmail(userRequestDto.getEmail());
      user.setNoHp(userRequestDto.getNoHp());
      user.setRole(role);
      user.setIsActive(userRequestDto.isIsActive());
      if (userRequestDto.isIsActive() == true) {
        user.setVerified(userRequestDto.isIsActive());
      }

      userRepository.save(user);

      result.setMessage("User berhasil di-update.");
      result.setCode(HttpStatus.OK.value());
    } catch (Exception e) {
      logger.error(stringUtil.getError(e));
      result.setSuccess(false);
      result.setMessage(e.getMessage());
      result.setCode(HttpStatus.BAD_REQUEST.value());
      return ResponseEntity.badRequest().body(result);
    }

    return ResponseEntity.ok(result);
  }

  @Override
  public ResponseEntity<?> updateProfilePictureBlob(long id, MultipartFile profilePicture, String uri) {
    result = new Result();
    try {
      userRepository.updateProfilePicture(IOUtils.toByteArray(profilePicture.getInputStream()), id);
    } catch (IOException e) {
      logger.error(stringUtil.getError(e));
    }

    return ResponseEntity.ok(result);
  }

  @Override
  public ResponseEntity<?> deleteUser(boolean banned, long id, String uri) {
    result = new Result();
    try {
      int response = userRepository.deleteUser(banned, id);
      if (response == 1) {
        result.setCode(HttpStatus.OK.value());
        result.setSuccess(true);
        if (banned == true) {
          result.setMessage("User berhasil di-banned.");
        } else {
          result.setMessage("User berhasil di-unbanned.");
        }
      } else {
        result.setSuccess(false);
        result.setMessage("Id User tidak ditemukan!");
        result.setCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.badRequest().body(result);
      }
    } catch (Exception e) {
      logger.error(stringUtil.getError(e));
      result.setSuccess(false);
      result.setMessage(e.getMessage());
      result.setCode(HttpStatus.BAD_REQUEST.value());
      return ResponseEntity.badRequest().body(result);
    }

    return ResponseEntity.ok(result);
  }

  @Override
  public ResponseEntity<?> updateProfilePictureFolder(long id, MultipartFile profilePicture, String uri) {
    result = new Result();
    try {
      String filename = String.valueOf("profie_picture_" + id).concat(".")
              .concat(globalUtil.getExtensionByStringHandling(profilePicture.getOriginalFilename()).orElse(""));
      String filenameResult = "";
      userRepository.setProfilePicturePath(filenameResult, id);
      result.setMessage("succes to save file ".concat(profilePicture.getOriginalFilename()));
    } catch (Exception e) {
      logger.error(stringUtil.getError(e));
    }

    return ResponseEntity.status(HttpStatus.OK).body(result);
  }

  @Override
  public ResponseEntity<?> forgotPassword(String email, String uri) throws IOException {
    result = new Result();

    User checkUserEmail = userRepository.findByEmail(email).orElse(new User());
    if (checkUserEmail.getUsername() == null) {
      result.setMessage("Error: Email has not been registered!");
      result.setCode(HttpStatus.BAD_REQUEST.value());
      return ResponseEntity
              .badRequest()
              .body(result);
    }
    EmailDetails emailDetails = new EmailDetails();
    emailDetails.setSubject("Forgot Password");

    Date dateNow = new Date();
    // 10 menit
    String tokenForgotPassword = jwtUtils.generateTokenForgotPassword(checkUserEmail.getUsername(),jwtExpiredTokenForgotPassword);
    long idUser = checkUserEmail.getId();
    String password = checkUserEmail.getPassword();
    return ResponseEntity.ok(new Result());
  }

  @Override
  public ResponseEntity<?> changePasswordForgot(ChangePasswordRequestDto param) throws JsonProcessingException {
    result = new Result();
    if(!jwtUtils.validateJwtToken(param.getToken()) || param.getToken() == null){
      result.setCode(400);
      result.setMessage("Token Is Invalid");
      return ResponseEntity.badRequest().body(result);
    }

    Map<String,Object> tokenDecode = jwtUtils.decode(param.getToken());

    // password validation
//    if(!validator.isPasswordValid(param.getPassword())){
//      result.setMessage("Password must be longer than 8 characters,use at least 1 uppercase letter,spesial characters and not contain spaces!!");
//      result.setCode(400);
//      result.setSuccess(false);
//      return ResponseEntity.badRequest().body(result);
//    }

    long userId = Long.parseLong(tokenDecode.get("userId").toString());
    int resultModel = userRepository.changePassword(encoder.encode(param.getPassword()),userId);
    if (resultModel == 1) {
      result.setCode(200);
      result.setMessage("Password Succesfully Updated");
      return ResponseEntity.ok(result);
    } else {
      result.setCode(200);
      result.setMessage("Password Failed Updated");
      return ResponseEntity.badRequest().body(result);
    }
  }
//
//  private String getJwtActiveEmail() {
//    jwtToken = JWT.create()
//        .withIssuer(issuer)
//        .withSubject(subject)
//        .withClaim("u_id", id)
//        .withClaim("u_email", email)
//        .withClaim("city_code", location)
//        .withIssuedAt(date)
//        .withExpiresAt(expTime)
//        .sign(algorithm);
//  }

}

class SubUser {
  private Long id;
  private String namaLengkap;
  private String email;
  private String noHp;
  private Role role;
  private Boolean status;

  public SubUser() {}

  public SubUser(Long id, String namaLengkap, String email, String noHp, Role role, Boolean status) {
    this.id = id;
    this.namaLengkap = namaLengkap;
    this.email = email;
    this.noHp = noHp;
    this.role = role;
    this.status = status;
  }

  public Long getId() {
      return id;
  }

  public String getNamaLengkap() {
      return namaLengkap;
  }

  public String getEmail() {
      return email;
  }
  
  public String getNoHp() {
      return noHp;
  }

  public Role getRole() {
      return role;
  }

  public Boolean isStatus() {
      return status;
  }
  
}
