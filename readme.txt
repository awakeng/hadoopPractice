** 공영주차장API를 이용한 실시간 주차장 정보 프로젝트 **

1. jar 파일 생성 및 환경 설정
eclipse -> parking 프로젝트 폴더 Run as -> Maven build ... 선택 -> Goals : dependency:copy-dependencies 입력 후 Run 
	target\dependency directory에 해당 자바 클래스에서 사용한 .jar들이 생긴다.
	-> 이 .jar 들을 linux의 경로(MobaXterm에 드래그) /usr/lib/jvm/java-1.8.0-openjdk/jre/lib/ext/ 에 넣는다. 
	-> 이제 Run as -> Maven Install을 선택해서 jar 파일 생성 후 드래그를 통해 /home/hadoop/ 경로로 넣어준다.

2. 권한 설정 : root 계정에서 chown -R hadoop:hadoop /home/hadoop/
(해당 jar파일 linux 실행 명령어 : [hadoop@dn01 ~]$ java -cp parking.jar model.API /home/hadoop/ )

** 이제 crontab을 통해 해당 .jar 파일을 n분 주기로 실행하여 api에서 제공받은 정보가 담긴 hello.txt라는 파일을 /home/hadoop에 생성할 것이다.
	이때 생성된 파일은 hdfs dfs -put을 통해 원하는 경로로 옮긴다.
그전에.. 데이터를 넘겨 받았을 때 바로 table에 연결되어 정보가 담길 수 있도록 beeline에 테이블을 미리 생성해 주자 **

3. hive table 생성 :	
hadoop>beeline
beeline>!connect jdbc:hive2://
<
drop table parking;
create table parking(
name string,
total int,
available int,
time string,
lat string,
lng string,
address string
)
row format delimited fields terminated by '\t'
;
desc parking;
>

4. 이제 hadoop 계정에서 crontab을 설정해주자.
hadoop>crontab -e
비어 있는 설정 파일이 열리는데, 아래와 같이 내용을 채워넣어 주자.
<
*/3 * * * * java -cp parking.jar model.API /home/hadoop/
*/3 * * * * /opt/hadoop/current/bin/hdfs dfs -put -f hello.txt /user/hive/warehouse/encore.db/parking
>
	1번 라인은 jar파일을 실행시켜 /home/hadoop/에 hello.txt를 만들어 주는 것이고,
	2번 라인은 생성된 hello.txt를 하둡 웨어하우스>DB>parking 테이블로 옮겨 주는 것이다. (이때 -f 옵션으로 기존 테이블은 삭제 해준다.)
	
5. 설정해둔 3분이 지난 후 테이블에 적재된 정보들을 확인해 보자
0: jdbc:hive2://>>select * from parking;

6. 실습
a) 현재 가용 자동차 수가 50대 이상인 공영 주차장 검색
	select * from parking where available >= 50;

b) 현재 가용 자동차 수가 총 가용 자동차 수의 30% 이상인 곳 검색
	select * from parking where available/total >= 0.3;


cf)실험 결과 crontab이 명령을 반복 수행하면서 실시간 정보가 바뀜에 따라 테이블 정보도 성공적으로 오버라이트 되는 것을 확인 함.