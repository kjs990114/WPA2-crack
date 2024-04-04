# WPA2-crack



## 소개

이 코드는 WPA2 passphrase를 cracking하기위한 코드이며, offline guessing 방식을 사용합니다.  
학습이나 보안의 취약점을 파악하는데 사용하세요.

사전에 캡쳐한 패킷의 정보를 이용해 이 코드에 입력하시면 password cracking을 할 수 있습니다.  
지원하는 모드는 brute-force mode , dictionary mode가 존재합니다.  


## 사용 방식
먼저, 4-way handshake를 캡쳐합니다.(WireShark나 리눅스의 airmon-ng등 이용)  
캡쳐한 패킷에서 추출한 ssid(ap의 이름), apmac(무선랜의 mac address), stationmac(무선랜이 연결되는 station mac address), anonce , snonce, mic, message
를 저장해 소스코드에 입력합니다.  
crack_dictionary(), crack_bruteforce()를 통해 크래킹을 시작합니다.  



## crack_dictionary()
![스크린샷 2024-01-10 오후 7 29 01](https://github.com/kjs990114/WPA2-crack/assets/50402527/44512dfd-05a9-42fe-9841-e5d2dafa88ba)  
일반적인 dictionary 포맷을 전달받습니다.

## crack_bruteforce()

모든 문자열과, 길이제한을 하지않으면 엄청난 시간이 소요되게 됩니다.   
따라서 매개변수로 password의 길이, 허용하는 charset을 받게 하였습니다.  
![스크린샷 2024-01-10 오후 7 33 29](https://github.com/kjs990114/WPA2-crack/assets/50402527/c9834849-87e6-47f9-b17d-998dbb11f025)  
예시로 charset은 다음과같이 선언되며, 패스워드에 포함되는 character를 제한할수있습니다.


## reference

모든 구현방식은 IEEE 공식문서의 암호화 방식을 그대로 따랐습니다.
참고 문서 : IEEE 802.11i-2004 Standard Amendment 6: Medium Access Control (MAC) Security Enhancements" (PDF).
https://paginas.fe.up.pt/~jaime/0506/SSR/802.11i-2004.pdf




