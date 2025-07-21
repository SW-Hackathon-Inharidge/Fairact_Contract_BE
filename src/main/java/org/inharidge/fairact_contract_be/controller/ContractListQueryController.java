package org.inharidge.fairact_contract_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.inharidge.fairact_contract_be.dto.ContractPageListDTO;
import org.inharidge.fairact_contract_be.dto.ContractSummaryDTO;
import org.inharidge.fairact_contract_be.dto.request.ContractIdListRequestDTO;
import org.inharidge.fairact_contract_be.exception.JwtAuthenticationException;
import org.inharidge.fairact_contract_be.service.ContractQueryService;
import org.inharidge.fairact_contract_be.service.JwtTokenService;
import org.inharidge.fairact_contract_be.util.AuthorizationHeaderUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @Operation(summary = "최근 열람한 계약서 3건 조회", description = "사용자가 가장 최근 열람한 계약서 3건을 반환합니다. (근로자, 고용주 통합)")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "성공적으로 최근 계약 목록을 반환했습니다.",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ContractSummaryDTO.class)))
            ),
            @ApiResponse(responseCode = "401", description = "JWT 인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/recent")
    public ResponseEntity<?> findRecent3Contracts(
            @RequestHeader(name = "Authorization") String authHeader,
            @RequestBody ContractIdListRequestDTO contractIdListRequestDTO) {

        try {
            String token = AuthorizationHeaderUtil.extractToken(authHeader);
            jwtTokenService.validateAccessToken(token);

            List<ContractSummaryDTO> contractSummaryDTOList =
                    contractQueryService.findRecentViewedContractByContractIdList(
                            contractIdListRequestDTO.getContractIdList());

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
    @Operation(summary = "내 서명 필요 계약서 3건 조회", description = "사용자가 서명해야 하는 계약서 상위 3건을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "계약 요약 리스트 반환 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ContractSummaryDTO.class)))),
            @ApiResponse(responseCode = "401", description = "JWT 인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
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
    @Operation(summary = "상대방 서명 필요 계약서 3건 조회", description = "상대방이 서명해야 하는 계약서 상위 3건을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "계약 요약 리스트 반환 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ContractSummaryDTO.class)))),
            @ApiResponse(responseCode = "401", description = "JWT 인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
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
    @Operation(
            summary = "고용주의 계약서 목록 조회 (페이지네이션)",
            description = "현재 로그인된 고용주의 계약서들을 페이지 단위로 반환합니다. 페이지는 0부터 시작하며, 한 페이지에 10개씩 반환됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "계약 페이지 리스트 반환 성공",
                    content = @Content(schema = @Schema(implementation = ContractPageListDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "JWT 인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/me/owner/{page}")
    public ResponseEntity<?> findPagedOwnerContracts(
            @RequestHeader(name = "Authorization") String authHeader,
            @PathVariable Integer page) {

        try {
            String token = AuthorizationHeaderUtil.extractToken(authHeader);
            Long userId = jwtTokenService.extractUserId(token);
            ContractPageListDTO contractPageListDTO =
                    contractQueryService.findPagedOwnerContracts(userId, page);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(contractPageListDTO);

        } catch (JwtAuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid JWT: " + e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal error: " + e.getMessage());
        }
    }

    @Operation(
            summary = "근로자의 계약서 목록 조회 (페이지네이션)",
            description = "현재 로그인된 근로자의 계약서들을 페이지 단위로 반환합니다. 페이지는 0부터 시작하며, 한 페이지에 10개씩 반환됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "계약 페이지 리스트 반환 성공",
                    content = @Content(schema = @Schema(implementation = ContractPageListDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "JWT 인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/me/worker/{page}")
    public ResponseEntity<?> findPagedWorkerContracts(
            @RequestHeader(name = "Authorization") String authHeader,
            @PathVariable Integer page) {

        try {
            String token = AuthorizationHeaderUtil.extractToken(authHeader);
            Long userId = jwtTokenService.extractUserId(token);
            ContractPageListDTO contractPageListDTO =
                    contractQueryService.findPagedWorkerContracts(userId, page);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(contractPageListDTO);

        } catch (JwtAuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid JWT: " + e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal error: " + e.getMessage());
        }
    }

    // 초대 대기 계약
    //TODO:: 초대 수락 전 계약 목록 10개씩 Pagination해서 조회 API(근로자, 고용주 통합)

    // 내 서명 대기 계약
    //TODO:: 내 서명 대기 계약 목록 10개씩 Pagination해서 조회 API(근로자, 고용주 통합)

    // 상대 서명 대기 계약
    //TODO:: 상대 서명 대기 계약 목록 10개씩 Pagination해서 조회 API(근로자, 고용주 통합)

    // 완료 계약
    //TODO:: 완료 계약 목록 10개씩 Pagination해서 조회 API(근로자, 고용주 통합)
}
