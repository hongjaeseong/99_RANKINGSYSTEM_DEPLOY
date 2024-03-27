package com.creator.imageAndMusic.controller;


import com.creator.imageAndMusic.config.auth.jwt.JwtTokenProvider;
import com.creator.imageAndMusic.config.auth.jwt.TokenInfo;
import com.creator.imageAndMusic.domain.dto.AlbumDto;
import com.creator.imageAndMusic.domain.dto.UserDto;
import com.creator.imageAndMusic.domain.entity.Images;
import com.creator.imageAndMusic.domain.entity.ImagesFileInfo;
import com.creator.imageAndMusic.domain.service.UserService;
import com.creator.imageAndMusic.properties.AUTH;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender javaMailSender;


    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @GetMapping("/join")
    public void join(){
        log.info("GET /user/join...");

    }


    @ExceptionHandler(Exception.class)
    public void ExceptionHandler(Exception e){
        log.info("User Exception.." + e);
    }


    //ID찾기
    @GetMapping("/confirmId")
    public void confirmId(){
        log.info("GET /user/confirmId..");
    }
    @PostMapping("/confirmId")
    public @ResponseBody void confirmId_post(@RequestBody UserDto dto){
        log.info("POST /user/confirmId.." + dto);

    }

    @PostMapping("/join")
    public String join_post(@Valid UserDto dto, BindingResult bindingResult, Model model, HttpServletRequest request) throws Exception {
        UserController.log.info("POST /join...dto " + dto);
        //파라미터 받기
            //
        //입력값 검증(유효성체크)
        //System.out.println(bindingResult);
        if(bindingResult.hasFieldErrors()){
            for(FieldError error :bindingResult.getFieldErrors()){
                log.info(error.getField() +" : " + error.getDefaultMessage());
                model.addAttribute(error.getField(),error.getDefaultMessage());
            }
            return "user/join";
        }

        //서비스 실행

        boolean isJoin =  userService.memberJoin(dto,model,request);
        //View로 속성등등 전달
        if(isJoin)
            return "redirect:login?msg=MemberJoin Success!";
        else
            return "user/join";
        //+a 예외처리

    }


    @GetMapping("/sendMail/{email}")
    @ResponseBody
    public ResponseEntity<JSONObject> sendmailFunc(@PathVariable("email") String email, HttpServletResponse response){
        UserController.log.info("GET /user/sendMail.." + email);
        //넣을 값 지정
        //메일 메시지 만들기
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[EMAIL AUTHENTICATION] CODE ");
        //난수코드 전달로 변경
        Random rand =new Random();
        int value = (int)(rand.nextDouble()*100000) ;

        message.setText(value+"");
        javaMailSender.send(message);


        //Token에 난수Value전달
        TokenInfo tokenInfo =  jwtTokenProvider.generateToken(AUTH.EMAIL_COOKIE_NAME,value+"",false);
        Cookie cookie  = new Cookie(AUTH.EMAIL_COOKIE_NAME,tokenInfo.getAccessToken());
        cookie.setPath("/");
        cookie.setMaxAge(60*15);
        response.addCookie(cookie);

        return new ResponseEntity(new JSONObject().put("success", true) , HttpStatus.OK);
    }

    @GetMapping("/emailConfirm")
    public @ResponseBody JSONObject emailConfirmFunc(@RequestParam(value = "emailCode" ,defaultValue = "0") String emailCode,HttpServletRequest request , HttpServletResponse response){
        UserController.log.info("GET /user/emailConfirm... code : " + emailCode);




        //JWT 토큰 쿠키중에 Email인증 토큰 쿠키 찾기
        Cookie c =  Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().startsWith(AUTH.EMAIL_COOKIE_NAME)).findFirst().orElse(null);

        if(c==null)
            return null;


        System.out.println(c.getName() + " | " + c.getValue());

        //Claims 꺼내기
        Claims claims = jwtTokenProvider.parseClaims(c.getValue());
        String idValue = (String) claims.get("id");
        boolean isAuth = (Boolean) claims.get(AUTH.EMAIL_COOKIE_NAME);


        JSONObject obj = new JSONObject();

        if(!isAuth) //email 전송은 완료 ,But 코드 입력 아직 안함
        {

            if(idValue.equals(emailCode)){

                //토큰 쿠키를 true로 만들어야함(아직)

                //기존 쿠키 만료
                c.setMaxAge(0);
                response.addCookie(c);

                //true 값 가지는 쿠키 다시 만들어서 전달
                TokenInfo tokenInfo =  jwtTokenProvider.generateToken(AUTH.EMAIL_COOKIE_NAME,"",true);
                Cookie cookie  = new Cookie(AUTH.EMAIL_COOKIE_NAME,tokenInfo.getAccessToken());
                cookie.setPath("/");
                cookie.setMaxAge(60*15);
                response.addCookie(cookie);

                obj.put("success",true);
                obj.put("message","이메일 인증을 성공하셨습니다.");
                return obj;
            }
            else {

                //받은 이메일코드랑 다르면
                obj.put("success",false);
                obj.put("message","이메일 인증을 실패했습니다.");
                return obj;

            }

        }
        else //코드 입력 완료
        {
            obj.put("success",true);
            obj.put("message","이메일 인증을 성공하셨습니다.");
            return obj;

        }



    }



    @GetMapping("/myinfo")
    public void func1(){
          log.info("GET /user/myinfo..");

    }


    @GetMapping("/album/main")
    public void func2(Model model) throws Exception {

        log.info("GET /user/album/main...");
        List<ImagesFileInfo> list =  userService.getUserItems();



        model.addAttribute("list",list);

    }


    @GetMapping("/album/add")
    public void func3(){
        log.info("GET /album/add");
    }

    @PostMapping("/album/add")
    public  @ResponseBody void add_image(AlbumDto dto) throws IOException {
        log.info("POST /album/add : " + dto+" file count : " + dto.getFiles().length);
        //유효성 검사
//        if(bindingResult.hasFieldErrors()){
//            for(FieldError error :bindingResult.getFieldErrors()){
//                log.info(error.getField() +" : " + error.getDefaultMessage());
//                //model.addAttribute(error.getField(),error.getDefaultMessage());
//            }
//        }

        //서비스 실행
        boolean isUploaded =  userService.uploadAlbum(dto);
    }


    @GetMapping("/album/read")
    public void read_album(@RequestParam(name = "iamgeid") Long iamgeid,Model model) throws Exception {

        log.info("GET /user/album/read...iamgeid " + iamgeid);

        List<ImagesFileInfo> filelist =  userService.getUserItem(iamgeid);
        Images images =  filelist.get(0).getImages();

        model.addAttribute("filelist",filelist);
        model.addAttribute("images",images);


    }


}
