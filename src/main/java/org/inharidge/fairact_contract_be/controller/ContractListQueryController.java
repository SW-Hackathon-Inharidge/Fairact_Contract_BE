package org.inharidge.fairact_contract_be.controller;

import org.inharidge.fairact_contract_be.dto.ContractSummaryDTO;
import org.inharidge.fairact_contract_be.exception.JwtAuthenticationException;
import org.inharidge.fairact_contract_be.service.ContractQueryService;
import org.inharidge.fairact_contract_be.service.JwtTokenService;
import org.inharidge.fairact_contract_be.util.AuthorizationHeaderUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/contract/list")
public class ContractListQueryController {
    private final JwtTokenService jwtTokenService;
    private final ContractQueryService contractQueryService;

    public ContractListQueryController(JwtTokenService jwtTokenService, ContractQueryService contractQueryService) {
        this.jwtTokenService = jwtTokenService;
        this.contractQueryService = contractQueryService;
    }

    // home
    // User의 최신 열람 계약 목록 3개 조회 API(근로자, 고용주 통합)
    @GetMapping("/recent")
    public ResponseEntity<?> findRecent3Contracts(
            @RequestHeader(name = "Authorization") String authHeader) {

        try {
            String token = AuthorizationHeaderUtil.extractToken(authHeader);
            Long userId = jwtTokenService.extractUserId(token);
            List<ContractSummaryDTO> contractSummaryDTOList =
                    contractQueryService.findRecentViewedContractByUserId(userId);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(contractSummaryDTOList);

        } catch (JwtAuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid JWT: " + e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal error: " + e.getMessage());
        }
    }

    // User의 내 서명 필요 계약 문서 목록 3개 조회 API(근로자, 고용주 통합)
    @GetMapping("/require/me")
    public ResponseEntity<?> findTop3ContractsRequiringUserSign(
            @RequestHeader(name = "Authorization") String authHeader) {

        try {
            String token = AuthorizationHeaderUtil.extractToken(authHeader);
            Long userId = jwtTokenService.extractUserId(token);
            List<ContractSummaryDTO> contractSummaryDTOList =
                    contractQueryService.findTop3ContractsRequiringUserSignByUserId(userId);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(contractSummaryDTOList);

        } catch (JwtAuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid JWT: " + e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal error: " + e.getMessage());
        }
    }

    // User의 상대방 서명 필요 계약 문서 목록 3개 조회 API(근로자, 고용주 통합)
    @GetMapping("/require/opponent")
    public ResponseEntity<?> findTop3ContractsRequiringOpponentSign(
            @RequestHeader(name = "Authorization") String authHeader) {

        try {
            String token = AuthorizationHeaderUtil.extractToken(authHeader);
            Long userId = jwtTokenService.extractUserId(token);
            List<ContractSummaryDTO> contractSummaryDTOList =
                    contractQueryService.findTop3ContractsRequiringOpponentSignByUserId(userId);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(contractSummaryDTOList);

        } catch (JwtAuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid JWT: " + e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal error: " + e.getMessage());
        }
    }

    // 나의 문서 탭
    //TODO:: 나의 문서 목록 10개씩 Pagination해서 조회 API(근로자 배열)

    //TODO:: 나의 문서 목록 10개씩 Pagination해서 조회 API(고용주 배열)

    // 초대 대기 계약
    //TODO:: 초대 수락 전 계약 목록 10개씩 Pagination해서 조회 API(근로자, 고용주 통합)

    // 내 서명 대기 계약
    //TODO:: 내 서명 대기 계약 목록 10개씩 Pagination해서 조회 API(근로자, 고용주 통합)

    // 상대 서명 대기 계약
    //TODO:: 상대 서명 대기 계약 목록 10개씩 Pagination해서 조회 API(근로자, 고용주 통합)

    // 완료 계약
    //TODO:: 완료 계약 목록 10개씩 Pagination해서 조회 API(근로자, 고용주 통합)
}
