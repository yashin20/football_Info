package project.footballinfo;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public ResponseEntity<?> handleTooManyRequestsException(HttpClientErrorException.TooManyRequests ex, WebRequest request) {
        // 서버로부터의 응답 본문 추출
        String responseBody = ex.getResponseBodyAsString();

        try {
            // JSON 객체로 변환
            JSONObject json = new JSONObject(responseBody);
            // 에러 메시지에서 대기 시간 추출
            String message = json.getString("message");
            // 대기 시간을 분석하여 사용자에게 보여줄 메시지 생성
            String userFriendlyMessage = message.replaceAll("Wait (\\d+) seconds", "$1초 이후에 시도해 주십시오.");

            // 사용자에게 보낼 응답 구성
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(userFriendlyMessage);
        } catch (Exception e) {
            // JSON 파싱에 실패한 경우, 기본 메시지 반환
            return ResponseEntity
                    .status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("현재 요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.");
        }
    }
}