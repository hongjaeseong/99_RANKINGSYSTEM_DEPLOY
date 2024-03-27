////  ----------------------------------
////  페이지 시작 이벤트
////  ----------------------------------
document.addEventListener("DOMContentLoaded", function() {
        // 페이지 렌더링 후 실행할 코드 작성
        // 예: DOM 요소에 접근하거나 초기화 작업 수행
        console.log("페이지가 로드되었습니다!");


        const searchCookieName = "email_auth_";
        let isAuth = false;
        let email_auth_cookie = null;

        var decodedCookie = decodeURIComponent(document.cookie);
        // 쿠키 이름을 검색할 문자열 생성
        var cookieString = searchCookieName + "=";
        // 현재 모든 쿠키 가져오기
        var decodedCookie = decodeURIComponent(document.cookie);
        // 쿠키를 ; 로 분할하여 배열로 만듭니다.
        var cookieArray = decodedCookie.split(';');
        // 모든 쿠키에 대해 반복
        for(var i = 0; i < cookieArray.length; i++) {
            var cookie = cookieArray[i];
            // 공백 제거
            while (cookie.charAt(0) == ' ') {
                cookie = cookie.substring(1);
            }
            // 현재 쿠키가 원하는 쿠키 이름으로 시작하는지 확인
            if (cookie.indexOf(cookieString) == 0) {
                // 해당 쿠키가 존재한다면 true 반환
               isAuth =  true;
               email_auth_cookie = cookie;

            }
        }



        if(isAuth&&email_auth_cookie !=null) //이메일 인증코드쿠키가 있다면
        {


                //email_auth_cookie 가 null이 아니라면 유효한지 요청 (axios)
                axios
                .get('/user/emailConfirm?emailCode=0')
                .then(res =>{
                              console.log(res);
                              if(res.data.success){
                                      //인증완료 -> 다음 입력을 할 준비~
                                      document.querySelector('.username_msg').style.color='green';
                                      document.joinform.username.value = "인증성공@example.com";
                                      document.joinform.username.readonly = true;
                                      document.querySelector('.username_msg').innerHTML=res.data.message;

                                      // 모달창 비활성화..
                                      document.querySelector('#exampleModal').classList.remove('show');
                                      document.querySelector('#exampleModal').classList.add('hide');
                                      document.querySelector('.modal-backdrop').remove();

                              }else{
                                      alert('기존 인증쿠키값을 제거합니다.');
                                      //해당 쿠키 제거
                                      document.cookie = email_auth_cookie + "=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
                                      location.reload();
                              }



                  })
                 .catch(err=>{console.log(err);});






        }else{
                //해당쿠키가 유효한지 서버로 체크
                console.log("isAuth",isAuth);
                console.log("isAuth",isAuth);
                const email_auth_btn = document.querySelector('.email_auth_btn');
                email_auth_btn.click();
        }


        //------------------------------
        //이메일 전송 버튼 클릭이벤트
        //------------------------------
        const  email_req = document.querySelector('.email_req');
        email_req.addEventListener('click', function(){

            console.log("EMail Send Btn Click");

            //------------------------------
            //서버 이메일 전송 요청 GET - 비동기 요청
            //------------------------------
            const email = document.sendEmailForm.email.value;
            axios.get('/user/sendMail/' + email)
                    .then(res=>{
                           console.log(res);

                            //------------------------------
                            //이메일인증코드 Form 표시
                            //------------------------------
                            const emailCodeForm = document.emailCodeForm;
                            emailCodeForm.classList.remove('hidden');


                       })
                    .catch(err=>{
                           console.log(err);
           });
           //------------------------------


        })
        //------------------------------


        //------------------------------
        //인증코드 버튼 클릭시
        //------------------------------
        const emailCodeForm_btn = document.querySelector('.emailCodeForm_btn');
        emailCodeForm_btn.addEventListener('click', function(){

               const username =  document.sendEmailForm.email.value;
               const emailCode = document.emailCodeForm.emailCode.value;

               console.log('emailCodeForm_btn clicked..');
                axios
                .get('/user/emailConfirm?emailCode='+emailCode+'&username='+username)
                .then(res =>{
                              console.log(res);
                              if(res.data.success){
                                      //인증완료 -> 다음 입력을 할 준비~
                                      document.querySelector('.username_msg').style.color='green';
                                      document.joinform.username.value = username;
                                      document.joinform.username.readonly = true;
                                      document.querySelector('.username_msg').innerHTML=res.data.message;

                                      // 모달창 비활성화..
                                      document.querySelector('#exampleModal').classList.remove('show');
                                      document.querySelector('#exampleModal').classList.add('hide');
                                      document.querySelector('.modal-backdrop').remove();

                              }else{
                                      //인증X -> 인증이 안된 내용을 표시
                                      alert('이메일 인증 실패!');
                                      location.href="/";

                              }



                  })
                 .catch(err=>{console.log(err);});

        });
        //------------------------------



});


//  ----------------------------------
//  주소창 띄우기
//  ----------------------------------
