# 고속도로 2차 사고 방지 어플리케이션 

### 개요
본 프로젝트는 2020-1학기에 Team.SBS에서 진행한 팀프로젝트의 결과물을 리팩토링한 프로젝트이다. 차량이 고속도로에서 주행하다가 일정 수준 이상의 충격 혹은 급감속이 발생하면 서버에게
사고 발생 가능성을 알린다. 그리고 해당 사고에 관련된 내용을 주변 차량에게 전송하여 전송받은 차량들은 해당 사고와 밀접한 관계가 있는지 확인한다. 사고와 관련이 있다면 유저에게 알림을 울리게된다.


#### 리팩토링 내용
 프로젝트 도중에 발견했지만 해결하지 못한 문제점이나 발견하지 못한 문제점을 찾아 개선했다.
그리고 아키텍쳐 패턴 변경 및 Jetpack에 공개된 라이브러리를 채택하여 사용하였다. 또한, 기존의 Java로 작성된 프로젝트를 Kotlin으로 변경하였다.

- Java에서 Kotlin으로 변환
- MVVM 패턴 사용
- SQLite를 Room으로 대체
- AsyncTask는 Coroutine으로 대체
- OkHttp를 Retrofit으로 대체
- Databinding(BindingAdapter) 사용
- 포그라운드 상태에서만 알림이 발생하게 변경(Context-registered BroadcastReceiver)
- MapView와 DrawerLayout 사이의 버그 픽스


### UI
아래 이미지는 실제 타 기기의 차량에서 사고가 발생할 경우 다른 유저들에게 보여주는 알람이다.

<img src=https://user-images.githubusercontent.com/30337408/93011093-30433380-f5ce-11ea-9498-15db1974633c.gif width=22% height=22%>

아래 이미지는 알림 설정 및 로그아웃을 위한 DrawerLayout의 NavigationView가 펼쳐진 모습이다.

<img src=https://user-images.githubusercontent.com/30337408/93011096-3507e780-f5ce-11ea-9802-2bf312549771.png width=20% height=20%>

아래 이미지는 로그인을 진행하는 화면이다.

<img src=https://user-images.githubusercontent.com/30337408/93011097-3507e780-f5ce-11ea-8849-b00dde988144.png width=20% height=20%>

# License
                    GNU GENERAL PUBLIC LICENSE
                       Version 3, 29 June 2007

 Copyright (C) 2007 Free Software Foundation, Inc. <https://fsf.org/>
 Everyone is permitted to copy and distribute verbatim copies
 of this license document, but changing it is not allowed.

