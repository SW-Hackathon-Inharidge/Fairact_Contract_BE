package org.inharidge.fairact_contract_be.controller;

import org.inharidge.fairact_contract_be.config.SseEmitterManager;
import org.inharidge.fairact_contract_be.exception.JwtAuthenticationException;
import org.inharidge.fairact_contract_be.service.JwtTokenService;
import org.inharidge.fairact_contract_be.util.AuthorizationHeaderUtil;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/contract/sse")
public class ContractDetailQueryController {
    private final JwtTokenService jwtTokenService;
    private final SseEmitterManager sseEmitterManager;

    public ContractDetailQueryController(JwtTokenService jwtTokenService, SseEmitterManager sseEmitterManager) {
        this.jwtTokenService = jwtTokenService;
        this.sseEmitterManager = sseEmitterManager;
    }

    // 계약 상세 정보 API - SSE
    @GetMapping(value = "/subscribe/detail", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter findContractDetail(
            @RequestHeader(name = "Authorization") String authHeader) {

        try {
            String token = AuthorizationHeaderUtil.extractToken(authHeader);
            Long userId = jwtTokenService.extractUserId(token);

            return sseEmitterManager.addEmitter(userId);
        } catch (JwtAuthenticationException e) {
            SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1시간 유지
            sseEmitterManager.sendErrorToClient(emitter, "Invalid JWT: " + e.getMessage());
            return emitter;

        } catch (Exception e) {
            SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1시간 유지
            sseEmitterManager.sendErrorToClient(emitter, "Internal error: " + e.getMessage());
            return emitter;
        }
    }

    //TODO:: 독소조항 검사 결과 조회 API - SSE
}
