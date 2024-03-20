package com.creator.imageAndMusic.domain.service;

import com.creator.imageAndMusic.config.auth.jwt.JwtTokenProvider;
import com.creator.imageAndMusic.domain.dto.UserDto;
import com.creator.imageAndMusic.domain.entity.User;
import com.creator.imageAndMusic.domain.repository.ImagesRepository;
import com.creator.imageAndMusic.domain.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.Arrays;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImagesRepository imagesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Transactional(rollbackFor = Exception.class)
    public boolean memberJoin(UserDto dto, Model model, HttpServletRequest request) throws Exception{

        //비지니스 Validation Check

        //password vs repassword 일치여부
        if(!dto.getPassword().equals(dto.getRepassword()) ){
            model.addAttribute("password","패스워드 입력이 상이합니다 다시 입력하세요");
            return false;
        }

        //동일 계정이 있는지 여부 확인
        if(userRepository.existsById(dto.getUsername())){
            model.addAttribute("username","동일한 계정명이 존재합니다.");
            return false;
        }



        //이메일인증이 되었는지 확인(JWT EmailAuth쿠키 true확인)
//        Cookie[] cookies =  request.getCookies();
//        String jwtAccessToken = Arrays.stream(cookies).filter(co -> co.getName().equals("EmailAuth")).findFirst()
//                .map(co -> co.getValue())
//                .orElse(null);

//        //---
//        // JWT토큰의 만료여부 확인
//        //---
//        if( !jwtTokenProvider.validateToken(jwtAccessToken)){
//
//            model.addAttribute("username","이메일 인증 유효시간을 초과했습니다");
//            return false;
//        }
//        else{
//            //EmailAuth Claim Value값 꺼내서 true 확인
//            Claims claims = jwtTokenProvider.parseClaims(jwtAccessToken);
//            Boolean isEmailAuth = (Boolean)claims.get(EmailAuthProperties.EMAIL_JWT_COOKIE_NAME);
//            String id = (String)claims.get("id");
//            if(isEmailAuth==null && isEmailAuth!=true){
//                //이메일인증실패!!
//                model.addAttribute("username","해당 계정의 이메일 인증이 되어있지 않습니다.");
//                return false;
//            }
//            if(!id.equals(dto.getUsername())){
//                System.out.println("!!!!!!!!!!!!!!");
//                model.addAttribute("username","해당 이메일 재인증이 필요합니다.");
//                return false;
//            }
//
//        }

        dto.setPassword(passwordEncoder.encode(dto.getPassword()));
        //Dto->Entity
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setRole(dto.getRole());
        user.setRole("ROLE_USER");

        //Db Saved...
        userRepository.save(user);

        return userRepository.existsById(user.getUsername());
    }


    @Transactional(rollbackFor = Exception.class)
    public void uploadImage(){
        //참고
        //https://github.com/TMP-SPRINGBOOT/SPRINBBOOT_PROJECTS/blob/main/07%20IMAGEBOARD/src/main/java/com/example/demo/domain/service/ImageBoardService.java

    }



}
