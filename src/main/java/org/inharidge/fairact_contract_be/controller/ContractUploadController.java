package org.inharidge.fairact_contract_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.inharidge.fairact_contract_be.dto.ContractDetailDTO;
import org.inharidge.fairact_contract_be.exception.JwtAuthenticationException;
import org.inharidge.fairact_contract_be.exception.NotFoundContractException;
import org.inharidge.fairact_contract_be.exception.UnAuthorizedContractAccessException;
import org.inharidge.fairact_contract_be.service.ContractCommandService;
import org.inharidge.fairact_contract_be.service.JwtTokenService;
import org.inharidge.fairact_contract_be.util.AuthorizationHeaderUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/contract")
public class ContractUploadController {

    private final ContractCommandService contractCommandService;
    private final JwtTokenService jwtTokenService;

    public ContractUploadController(ContractCommandService contractCommandService, JwtTokenService jwtTokenService) {
        this.contractCommandService = contractCommandService;
        this.jwtTokenService = jwtTokenService;
    }

    // 파일 업로드 및 계약 Entity 생성
    @Operation(
            summary = "계약서 업로드 및 계약 생성",
            description = "JWT 인증을 통해 사용자의 계약서를 업로드하고 계약을 생성합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "성공적으로 계약이 생성되었습니다.",
                    content = @Content(schema = @Schema(implementation = ContractDetailDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "JWT 인증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping(value = "/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadContractFileAndCreateContract(
            @RequestHeader(name = "Authorization") String authHeader,
            @RequestParam(name = "contract") MultipartFile contractFile) {

        try {
            String token = AuthorizationHeaderUtil.extractToken(authHeader);

            Long userId = jwtTokenService.extractUserId(token);
            ContractDetailDTO contractDetailDTO = contractCommandService.createContract(contractFile, userId);

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

    // 파일 재업로드 및 계약 Entity 수정
    @Operation(
            summary = "계약서 파일 재업로드 및 계약 수정",
            description = "기존 계약 ID에 해당하는 계약서를 새로 업로드하여 내용을 수정합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "성공적으로 계약이 수정되었습니다.",
                    content = @Content(schema = @Schema(implementation = ContractDetailDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "JWT 인증 실패"),
            @ApiResponse(responseCode = "404", description = "지정한 계약을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PatchMapping(value = "/{contractId}/file/reupload/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> reUploadContractFileAndUpdateContract(
            @RequestHeader(name = "Authorization") String authHeader,
            @RequestParam(name = "contract") MultipartFile newContractFile,
            @PathVariable Long contractId) {

        try {
            String token = AuthorizationHeaderUtil.extractToken(authHeader);
            Long userId = jwtTokenService.extractUserId(token);
            ContractDetailDTO contractDetailDTO = contractCommandService.updateContractFile(newContractFile, contractId, userId);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(contractDetailDTO);

        } catch (JwtAuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid JWT: " + e.getMessage());

        } catch (UnAuthorizedContractAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("UnAuthorized Access: " + e.getMessage());
        } catch (NotFoundContractException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Not Found Contract: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal error: " + e.getMessage());
        }
    }
}
