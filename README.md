# 몰입캠프 개발 1주차
3개의 탭(=> 연락처, 갤러리, 얼굴인식)으로 구성된 앱 <br>

## 팀원
[박정웅](https://github.com/yeolia327)
[이제호](https://github.com/jhl8109)

## 주요 기능
### 연락처
- 이름, 전화번호, 주소 그리고 사진을 저장할 수 있는 연락처 탭을 구현하였다.
- 연락처를 누르면 다이얼로그가 열려 전화 걸기, 메세지 보내기, 사진 설정, 연락처 수정 및 전화번호 삭제를 할 수 있다.
- 초기 연락처 데이터는 휴대폰에 저장된 Json파일로부터 받아오며, Room db를 사용하여 앱을 종료하더라도 연락처 데이터가 유지된다.

| <img src = "https://user-images.githubusercontent.com/77967396/147923683-42ad0323-a976-4675-ad6d-12a20926e477.gif"> |  <img src = "https://user-images.githubusercontent.com/77967396/147923980-682a7583-92e8-42c2-ae07-d95904272c86.gif"> |  <img src = "https://user-images.githubusercontent.com/77967396/147924183-8163ddf7-73fc-4c54-9ba4-67b449b164e4.gif"> |
|:--------|:--------:|--------:|
|<div align="center"> 연락처 연동 </div>| 사진 설정 및 연락처 수정 |<div align="center"> 연락처 삭제 및 데이터 유지 </div>|

### 갤러리
- 버튼을 클릭하면 카메라, 갤러리를 통해 이미지를 저장할 수 있다.
- 사진을 줌 인, 줌 아웃 할 수 있다.
- Room DB를 사용하여 앱을 끄더라도 저장한 이미지가 유지된다.
<img src = "https://user-images.githubusercontent.com/78259314/147910507-8b14c590-3d01-4581-86e2-0263ef5f51fd.gif">

### 얼굴 인식
- 카메라, 갤러리를 활용하여 예상 나이와 성별에 대하여 알 수 있다.
- 카카오 API를 활용하였다.
<img src = "https://user-images.githubusercontent.com/78259314/147924680-32587166-c91a-4497-919c-6cbb9a86fc95.gif">

## 구현

### 연락처
- Room DB 사용 
### 갤러리
- Room DB 사용 , 확대-축소가 가능한 PhotoView 활용, 동적으로 이미지 뷰 할당
### 얼굴 인식 API
- 카카오 API 사용, MPAndroidChart 라이브러리 활용
<img src = "https://user-images.githubusercontent.com/78259314/148014471-7eab1430-9c09-403f-b5ef-eedf3d736bf2.png">
<img src = "https://user-images.githubusercontent.com/78259314/148014481-21591d51-16ea-4769-b230-1fd7835d62ff.png">
<pre><code>
  fun serverResult(file:MultipartBody.Part){
        textView.text = "분석중"
        textView.visibility = View.VISIBLE
        Glide.with(this).load(R.drawable.loading).into(loadingImage)        //로딩 GIF이미지를 넣음
        val retrofit = Retrofit.Builder().baseUrl("https://dapi.kakao.com/") //Retrofit2를 사용하여 카카오에 POST메소드 통신
            .addConverterFactory(GsonConverterFactory.create()).build()
        val service = retrofit.create(RetrofitService::class.java)                  // 헤더와 보내는 형식(Multipart)을 지정함
        service.getOnlineChannel("KakaoAK 16b9d5d1f68577d49a3ddcdae9f7c5ca",    // 개인 키 설정
            file)?.enqueue(object : Callback<Image>{
            override fun onResponse(call: Call<Image>, response: Response<Image>) { // Retrofit2에서 지원하는 방식에 의해서
                var result = response.body()                                        // 미리 만들어 놓은 Image 클래스로 파싱됨
                if(response.isSuccessful) {
                    Log.e("success", result!!.toString())
                    val mainActivity = context as MainActivity
                    if(result.result.faces.isNotEmpty()) {                          //intent를 통해 fragment간 response결과값을 공유함
                        activity!!.intent.putExtra("age",result.result.faces[0].faceAttr.age)
                        activity!!.intent.putExtra("gender",result.result.faces[0].faceAttr.gender.male)
                        mainActivity.changeFragmentFour()
                    }else{
                        textView.text = "사진 인식에 실패했어요"
                        textView.visibility = View.VISIBLE
                        loadingImage.visibility = View.INVISIBLE
                    }
                    Log.e("failed", response.code().toString())
                    Log.e("failed",response.errorBody()?.string()!!)
                } else {
                }
            }
            override fun onFailure(call: Call<Image>, t: Throwable) {
                Log.e("실패2","실패2")
                t.printStackTrace()
            }
        })
    }
  </code></pre>  

## 시행착오와 개선방안

- 권한 설정 및 카메라,갤러리의 이미지 경로 설정에서 어려움을 겪었습니다. 각 uri파일의 영원한 권한을 부여하여 해결하였습니다.
- Camera API 1 썼는데 더 좋은 최신 버전의 Camera API를 사용하는 방법이 더 좋을 것이라고 생각됩니다.

