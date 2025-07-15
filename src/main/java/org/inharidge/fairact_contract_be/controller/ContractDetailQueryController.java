package org.inharidge.fairact_contract_be.controller;

import org.inharidge.fairact_contract_be.config.SseEmitterManager;
import org.inharidge.fairact_contract_be.dto.ContractDetailDTO;
import org.inharidge.fairact_contract_be.exception.JwtAuthenticationException;
import org.inharidge.fairact_contract_be.service.ContractQueryService;
import org.inharidge.fairact_contract_be.service.JwtTokenService;
import org.inharidge.fairact_contract_be.util.AuthorizationHeaderUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/contract")
public class ContractDetailQueryController {
    private final JwtTokenService jwtTokenService;
    private final SseEmitterManager sseEmitterManager;
    private final ContractQueryService contractQueryService;

    public ContractDetailQueryController(JwtTokenService jwtTokenService, SseEmitterManager sseEmitterManager, ContractQueryService contractQueryService) {
        this.jwtTokenService = jwtTokenService;
        this.sseEmitterManager = sseEmitterManager;
        this.contractQueryService = contractQueryService;
    }

    @GetMapping("/{contractId}/detail")
    public ResponseEntity<?> findContractDetail(
            @PathVariable("contractId") String contractId,
            @RequestHeader("Authorization") String authHeader) {

        try {
            String token = AuthorizationHeaderUtil.extractToken(authHeader);
            jwtTokenService.validateAccessToken(token);

            ContractDetailDTO contractDetailDTO =
                    contractQueryService.findContractDetailById(contractId);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(contractDetailDTO);

        } catch (JwtAuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid JWT: " + e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal error: " + e.getMessage());
        }
    }

    // 계약 상세 정보 API - SSE
    @GetMapping(value = "/sse/subscribe/detail", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter findContractDetail(
            @RequestHeader(name = "Authorization") String authHeader) {


        String token = AuthorizationHeaderUtil.extractToken(authHeader);
        Long userId = 0L;

        try {
            userId = jwtTokenService.extractUserId(token);

            return sseEmitterManager.addEmitter(userId, "contract-detail");
        } catch (JwtAuthenticationException e) {
            SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1시간 유지
            return emitter;

        } catch (Exception e) {
            SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1시간 유지
            sseEmitterManager.sendErrorToClient(userId, "error", "Internal error: " + e.getMessage());
            return emitter;
        }
    }

    // 독소조항 검사 결과 조회 API - SSE
    @GetMapping(value = "/sse/subscribe/toxic-clause", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter findContractToxicClause(
            @RequestHeader(name = "Authorization") String authHeader) {

        String token = AuthorizationHeaderUtil.extractToken(authHeader);
        Long userId = 0L;

        try {
            userId = jwtTokenService.extractUserId(token);

            return sseEmitterManager.addEmitter(userId, "toxic-clause");
        } catch (JwtAuthenticationException e) {
            return new SseEmitter(60 * 60 * 1000L);

        } catch (Exception e) {
            SseEmitter emitter = new SseEmitter(60 * 60 * 1000L); // 1시간 유지
            sseEmitterManager.sendErrorToClient(userId, "error", "Internal error: " + e.getMessage());
            return emitter;
        }
    }
}
