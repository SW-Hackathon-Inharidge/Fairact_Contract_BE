package org.inharidge.fairact_contract_be.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.inharidge.fairact_contract_be.dto.ContractDetailDTO;
import org.inharidge.fairact_contract_be.dto.request.ContractDigitalSignRequestDTO;
import org.inharidge.fairact_contract_be.dto.request.ContractEmailSendingRequestDTO;
import org.inharidge.fairact_contract_be.exception.*;
import org.inharidge.fairact_contract_be.service.ContractCommandService;
import org.inharidge.fairact_contract_be.service.EmailService;
import org.inharidge.fairact_contract_be.service.JwtTokenService;
import org.inharidge.fairact_contract_be.service.RedisService;
import org.inharidge.fairact_contract_be.util.AuthorizationHeaderUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/contract")
public class ContractCommandController {
    private final JwtTokenService jwtTokenService;
    private final ContractCommandService contractCommandService;
    private final EmailService emailService;
    private final RedisService redisService;

    public ContractCommandController(JwtTokenService jwtTokenService, ContractCommandService contractCommandService, EmailService emailService, RedisService redisService) {
        this.jwtTokenService = jwtTokenService;
        this.contractCommandService = contractCommandService;
        this.emailService = emailService;
        this.redisService = redisService;
    }

    // 본인인증 이후 서명 시, 계약 Entity의 넘어온 해당 서명 위치 값, 상태값을 업데이트 API
    // 들어온 user가 고용주인지 근로자인지 판단하고 서명값 업데이트 하기
    @Operation(
            summary = "계약 디지털 서명 및 상태 업데이트",
            description = "사용자가 서명 버튼을 누르면 계약서 내 해당 위치에 서명하고, 계약 상태를 업데이트합니다. 고용주/근로자 구분 자동 처리됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "서명 및 상태 업데이트 성공",
                    content = @Content(schema = @Schema(implementation = ContractDetailDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 또는 권한 없음"),
            @ApiResponse(responseCode = "404", description = "계약을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PatchMapping("/{contractId}/sign")
    public ResponseEntity<?> updateDigitalSignatureAndContractState(
            @RequestHeader(name = "Authorization") String authHeader,
            @PathVariable(name = "contractId") String contractId,
            @RequestBody ContractDigitalSignRequestDTO contractDigitalSignRequestDTO
            ) {

        try {
            String token = AuthorizationHeaderUtil.extractToken(authHeader);
            Long userId = jwtTokenService.extractUserId(token);

            ContractDetailDTO contractDetailDTO =
                    contractCommandService.updateContractStateAndDigitalSign(contractId, userId, contractDigitalSignRequestDTO);

            return ResponseEntity.ok()
                    .body(contractDetailDTO);

        } catch (JwtAuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid JWT: " + e.getMessage());
        } catch (UnAuthorizedContractAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("UnAuthorized Access: " + e.getMessage());
        } catch (NotFoundContractException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Contract Not Found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal error: " + e.getMessage());
        }
    }

    // 초대 이메일 보낼 시 이메일 보내기, Redis 이메일 값 생성
    @Operation(
            summary = "계약 초대 이메일 전송 및 Redis 기록",
            description = "계약 상대에게 초대 이메일을 전송하고, Redis에 전송 이력을 저장합니다. 중복 전송을 방지합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이메일 전송 성공"),
            @ApiResponse(responseCode = "400", description = "이미 이메일이 전송됨"),
            @ApiResponse(responseCode = "401", description = "인증 실패 또는 권한 없음"),
            @ApiResponse(responseCode = "404", description = "계약을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/{contractId}/email")
    public ResponseEntity<?> sendEmailAndUpdateContractState(
            @RequestHeader(name = "Authorization") String authHeader,
            @PathVariable(name = "contractId") String contractId,
            @RequestBody ContractEmailSendingRequestDTO contractEmailSendingRequestDTO) {

        try {
            String token = AuthorizationHeaderUtil.extractToken(authHeader);
            Long userId = jwtTokenService.extractUserId(token);

            contractCommandService.updateContractEmailByContractId(
                    contractId, contractEmailSendingRequestDTO.getOpponent_email());

            contractCommandService.createEmailInfoInRedis(
                    contractId,
                    userId,
                    contractEmailSendingRequestDTO.getOpponent_email());

            emailService.sendHtmlEmail(
                    contractEmailSendingRequestDTO.getOpponent_email(),
                    contractEmailSendingRequestDTO.getSubject(),
                    contractEmailSendingRequestDTO.getHtml_content());

            return ResponseEntity.ok()
                    .build();

        } catch (JwtAuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid JWT: " + e.getMessage());
        } catch (UnAuthorizedContractAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("UnAuthorized Access: " + e.getMessage());
        } catch (NotFoundContractException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Contract Not Found: " + e.getMessage());
        } catch (AlreadySendEmailException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Bad Request: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal error: " + e.getMessage());
        }
    }

    // 이메일 수락시, 상태값 업데이트 및 Redis 초대 이메일 값 삭제
    @Operation(
            summary = "계약 초대 이메일 수락 및 계약 상태 업데이트",
            description = "수신자가 초대 이메일을 수락하면 계약 상태를 업데이트하고, Redis에 저장된 이메일 기록을 삭제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이메일 수락 성공 및 상태 업데이트",
                    content = @Content(schema = @Schema(implementation = ContractDetailDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 또는 권한 없음"),
            @ApiResponse(responseCode = "404", description = "Redis에 이메일 기록 없음 또는 계약 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PatchMapping("/{contractId}/email")
    public ResponseEntity<?> acceptEmailAndUpdateContractState(
            @RequestHeader(name = "Authorization") String authHeader,
            @PathVariable(name = "contractId") String contractId) {

        try {
            String token = AuthorizationHeaderUtil.extractToken(authHeader);
            Long userId = jwtTokenService.extractUserId(token);
            String email = jwtTokenService.extractEmail(token);
            String name = jwtTokenService.extractName(token);

            if (!redisService.exists("email:" + contractId + ":" + email))
                throw new NotFoundEmailException("Email Not Found");
            redisService.delete("email:" + contractId + ":" + email);

            ContractDetailDTO contractDetailDTO =
                    contractCommandService.updateContractStateByInviteAccepted(contractId, userId, name);

            return ResponseEntity.ok()
                    .body(contractDetailDTO);

        } catch (JwtAuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid JWT: " + e.getMessage());
        } catch (UnAuthorizedContractAccessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("UnAuthorized Access: " + e.getMessage());
        } catch (NotFoundContractException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Contract Not Found: " + e.getMessage());
        } catch (NotFoundEmailException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal error: " + e.getMessage());
        }
    }
}
