package ir.navaco.cart.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController("/wallet")
public class WalletController {
    @Autowired
    private WalletService walletService;

    @GetMapping("/preAuthorize")
    public ResponseEntity<PreAuthorizeResponse> preAuthorize(@RequestParam Long walletId,@RequestParam BigDecimal amount){
      return   ResponseEntity.ok(walletService.preAuthorize(walletId, amount));
    }

    @PostMapping("/process")
    public ResponseEntity<String> process(@RequestBody String internalTxId, @RequestParam  String pspRef, @RequestParam  boolean success){
        return   ResponseEntity.ok(walletService.processCallback(internalTxId,pspRef,success));
    }

}
