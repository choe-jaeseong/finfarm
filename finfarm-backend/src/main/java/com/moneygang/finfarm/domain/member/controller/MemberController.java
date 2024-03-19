package com.moneygang.finfarm.domain.member.controller;

import com.moneygang.finfarm.domain.member.dto.request.MemberLoginRequest;
import com.moneygang.finfarm.domain.member.dto.response.MemberLoginResponse;
import com.moneygang.finfarm.domain.member.entity.Member;
import com.moneygang.finfarm.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<List<Member>> selectAll() {
        return memberService.selcetAll();
    }

    @PostMapping("/login")
    public ResponseEntity<MemberLoginResponse> kakaologin(@RequestBody MemberLoginRequest request) {
        String accessToken = memberService.getKakaoAccessToken(request.getAccessToken());
        log.info("accessToken: " + accessToken);
        HashMap<String, Object> userInfo = memberService.getUserKakaoInfo(accessToken);

        return memberService.login((String) userInfo.get("email"));
    }


}
