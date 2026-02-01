package ir.navaco.cart.transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record PreAuthorizeResponse(
        Long stan,
        String authCode,
        BigDecimal amount,
        String currency,
        String issuerId,
        String rrn,

        //  String responseMessage,
        boolean partialCapturedAllowed,
        Instant authorizationTime
) {
}

/*
import lombok.*;

import java.math.BigDecimal;
import java.security.Timestamp;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PreAuthorizeResponse {
    private Long stan;
    private String authCode;
    private BigDecimal amount;
    private String status;
    private String currency;
    private String issuerId;
    private String responseCode;
    private String responseMessage;
    private boolean partialCapturedAllowed;
    @Builder.Default
    private Instant authorizationTime =Instant.now();
}
*/
