package ir.navaco.cart.transaction;


import java.io.Serializable;

public record ProcessRequest(Long stan, String authCode, String rrn, boolean success) implements Serializable {
}
