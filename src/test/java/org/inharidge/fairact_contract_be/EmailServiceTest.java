package org.inharidge.fairact_contract_be;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.inharidge.fairact_contract_be.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.*;

@SpringBootTest
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @MockitoBean
    private JavaMailSender mailSender;

    @ParameterizedTest
    @CsvSource({
            "test1@example.com, 테스트 제목 1, <h1>테스트 본문1</h1>",
            "test2@example.com, 테스트 제목 2, <p>테스트 본문2</p>"
    })
    @DisplayName("HTML 이메일을 정상적으로 전송한다")
    void sendHtmlEmail_shouldSendEmail(String to, String subject, String htmlContent) throws MessagingException {
        // given
        MimeMessage mockMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mockMessage);

        // when
        emailService.sendHtmlEmail(to, subject, htmlContent);

        // then
        verify(mailSender, times(1)).send(mockMessage);
        verify(mailSender, times(1)).createMimeMessage();
    }
}
