package com.xloop.resourceloop.authenticationservice.Controller;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.xloop.resourceloop.authenticationservice.Classes.Auth;
import com.xloop.resourceloop.authenticationservice.Classes.OTPGeneration;
import com.xloop.resourceloop.authenticationservice.JPARepository.ForgetPasswordRepository;
import com.xloop.resourceloop.authenticationservice.JPARepository.UserRepository;
import com.xloop.resourceloop.authenticationservice.Model.ForgetPassword;
import com.xloop.resourceloop.authenticationservice.Model.User;
import com.xloop.resourceloop.authenticationservice.mail.EmailService;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ForgetPasswordRepository forgetPasswordRepo;


    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(10, new SecureRandom());

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user){
        if(user.getFirst_name() != null && user.getEmail() != null && user.getPassword() != null){
            User userExists = userRepo.findByEmail(user.getEmail());
            if(userExists != null){
                return ResponseEntity.status(409).body("User Already Exist");
            }
            String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
            user.setPassword(encodedPassword);
            userRepo.save(user);
            return ResponseEntity.ok("User Registered");
        }
        else{
            return ResponseEntity.status(400).body("All feilds Required");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody Auth auth){
        User user = userRepo.findByEmail(auth.getEmail());
        if(user == null){
            return ResponseEntity.status(403).body(null);
        }
        else{
            if(bCryptPasswordEncoder.matches(auth.getPassword(),user.getPassword())){
                return ResponseEntity.status(200).body(user);
            }
        }
        return ResponseEntity.status(403).body(null);
    }

    @PostMapping("/forgetpassword/{id}")
    public ResponseEntity<String> resetPassword(@PathVariable Long id ,@RequestBody String new_password){
        User user = userRepo.findById(id).orElse(null);
        user.setPassword(new_password);
        userRepo.save(user);
        return ResponseEntity.status(200).body("Password Updated Successfully");
    }
    
    @PostMapping("/forgetpassword-link")
    public ResponseEntity<String> resetPasswordLink(@RequestBody String email){
        User user = userRepo.findByEmail(email);
        if(user == null){
            return ResponseEntity.status(403).body("Email Not Found");
        }
        String otp = OTPGeneration.OTP(4);
        ForgetPassword forgetPassword = new ForgetPassword(email,otp);
        forgetPasswordRepo.save(forgetPassword);
        emailService.sendSimpleMessage(email, "Reset Password Email", "Hi, User.... This is your otp "+otp);
        return ResponseEntity.status(200).body("Reset Pin Send Successfully");
    }

    @PostMapping("/otp-verification")
    public ResponseEntity<String> otpVerification(@RequestBody ForgetPassword forgetPassword){

        ForgetPassword data = forgetPasswordRepo.findByEmailAndOtp(forgetPassword.getEmail(), forgetPassword.getOtp());
        if(data == null){
            return ResponseEntity.status(403).body("OTP Not Found");
        }
        return ResponseEntity.status(200).body("OTP Verified");
    }
}
