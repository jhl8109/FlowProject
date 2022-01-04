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

