package com.moneygang.finfarm.domain.member.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.moneygang.finfarm.domain.member.dto.request.MemberJoinRequest;
import com.moneygang.finfarm.domain.member.dto.response.MemberJoinResponse;
import com.moneygang.finfarm.domain.member.dto.response.MemberLoginResponse;
import com.moneygang.finfarm.domain.member.entity.Member;
import com.moneygang.finfarm.domain.member.repository.MemberRepository;
import com.moneygang.finfarm.global.base.JwtTokenProvider;
import com.moneygang.finfarm.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    public ResponseEntity<List<Member>> selcetAll() {
        List<Member> findAll = memberRepository.findAll();

        return ResponseEntity.ok(findAll);
    }

    public ResponseEntity<MemberJoinResponse> join(MemberJoinRequest request) {
        //유저 이메일 중복 확인
        Optional<Member> optionalMember = memberRepository.findByMemberEmail(request.getMemberEmail());
        if(optionalMember.isPresent())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MemberJoinResponse.create("회원가입 실패"));

        //유저 등록
        Member member = Member.builder()
                .memberEmail(request.getMemberEmail())
                .memberNickname(request.getMemberNickname())
                .memberAccountPassword(request.getMemberAccountPassword())
                .memberImageUrl(request.getMemberImageUrl())
                .memberSolveQuiz(false)
                .memberCurPoint((long) 0.0)
                .memberCreateDate(LocalDate.now())
                .farmLevel(1)
                .build();

        memberRepository.save(member);

        return ResponseEntity.ok(MemberJoinResponse.create("회원가입 성공"));
    }

    public ResponseEntity<MemberLoginResponse> autoLogin() {
        //토큰에서 이메일 추출
        String email = "";

        //이메일로 회원 조회
        Optional<Member> optionalMember = memberRepository.findByMemberEmail(email);
//        if(optionalMember.isEmpty())
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(MemberLoginResponse.createMemberLoginResponse("자동 로그인 실패"));

        return ResponseEntity.ok(null);
    }

    public ResponseEntity<MemberLoginResponse> login(String memberEmail) {
        log.info("member login: " + memberEmail);
        Optional<Member> optionalMember = memberRepository.findByMemberEmail(memberEmail);

        if(optionalMember.isPresent()) {
            Member member = optionalMember.get();

            String accessToken = jwtTokenProvider.createAccessToken(jwtTokenProvider, memberEmail);
            String refreshToken = jwtTokenProvider.createRefreshToken(jwtTokenProvider, memberEmail);
            return ResponseEntity.ok(MemberLoginResponse.create(accessToken, refreshToken, member.getMemberNickname(), member.isMemberSolveQuiz(), member.getMemberCurPoint(), member.getMemberImageUrl()));
        }
        else throw new GlobalException(HttpStatus.NOT_FOUND, "Member Not Found");
    }

    public String getKakaoAccessToken(String authorize_code) {
        String access_Token = "";
        String refresh_Token = "";
        String reqURL = "https://kauth.kakao.com/oauth/token";

        try {
            // HttpClient 인스턴스 생성
            HttpClient httpClient = HttpClient.newHttpClient();

            // 요청할 URL 설정
            URI uri = URI.create(reqURL);

            // 요청 파라미터 설정
            Map<Object, Object> data = new HashMap<>();
            data.put("grant_type", "authorization_code");
            data.put("client_id", "434c09e04423ad80d97eb8f45f3bc229");
            data.put("redirect_uri", "http://localhost:8080/oauth/callback/kakao");
            data.put("code", authorize_code);

            // 요청 본문 생성
            String requestBody = data.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .reduce((a, b) -> a + "&" + b)
                    .orElse("");

            // HTTP 요청 구성
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // 요청 보내기
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 응답 코드가 200이면 성공
            if (response.statusCode() == 200) {
                log.info("responseCode : " + response.statusCode());
                log.info("response body : " + response.body());

                // JSON 파싱
                JsonElement element = JsonParser.parseString(response.body());
                access_Token = element.getAsJsonObject().get("access_token").getAsString();
                refresh_Token = element.getAsJsonObject().get("refresh_token").getAsString();

                log.info("access_token : " + access_Token);
                log.info("refresh_token : " + refresh_Token);
            } else {
                log.info("HTTP 요청 실패: " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return access_Token;
    }

    public HashMap<String, Object> getUserKakaoInfo(String access_Token) {

        // 요청하는 클라이언트마다 가진 정보가 다를 수 있기에 HashMap타입으로 선언
        HashMap<String, Object> userInfo = new HashMap<String, Object>();
        String reqURL = "https://kapi.kakao.com/v2/user/me";
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(reqURL))
                    .header("Authorization", "Bearer " + access_Token)
                    .GET()
                    .build();

            // 토큰으로 카카오 유저 정보 조회
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("responseCode : " + response.statusCode());

            String result = response.body();
            log.info("response body : " + result);

            // JSON 파싱
            JsonElement element = JsonParser.parseString(result);

            // 유저 id 저장
            String id = element.getAsJsonObject().get("id").getAsString();
            userInfo.put("id", id);

            // 유저 email 저장
            JsonObject kakao_account = element.getAsJsonObject().get("kakao_account").getAsJsonObject();
            if(kakao_account.getAsJsonObject().get("email") != null){
                String email = kakao_account.getAsJsonObject().get("email").getAsString();
                userInfo.put("email", email);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return userInfo;
    }
}