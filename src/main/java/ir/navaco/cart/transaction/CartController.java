package ir.navaco.cart.transaction;


import ir.navaco.cart.transaction.serviceImpl.BusinessImpl;
import ir.navaco.cart.transaction.serviceImpl.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController("/Cart")
public class CartController {
    @Autowired
    private WalletService walletService;

    @Autowired
    private CartService cartService;

    @Autowired
    private BusinessImpl business;

    @GetMapping("/preAuthorize")
    public ResponseEntity<PreAuthorizeResponse> preAuthorize(@RequestParam Long CartId, @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(walletService.preAuthorize(CartId, amount));
    }

/*    @PostMapping("/process")
    public ResponseEntity<String> process(@RequestBody String internalTxId,@RequestParam  String pspRef,@RequestParam  boolean success){
        return   ResponseEntity.ok(CartService.processCallback(internalTxId,pspRef,success));
    }*/


    @PostMapping("/process")
    public ResponseEntity<PreAuthorizeResponse> processTransaction(@RequestBody ProcessRequest processRequest) {
        //  return   ResponseEntity.ok(cartService.processTransaction(processRequest));
        cartService.processTransaction(processRequest);
        return null;
    }


    @PostMapping("/deposit")
    public ResponseEntity<PreAuthorizeResponse> deposit(@RequestBody ProcessRequest request) {
        //  return   ResponseEntity.ok(cartService.processTransaction(processRequest));
        business.performDeposit(request);
        return null;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(@RequestParam String text) {
        //  return   ResponseEntity.ok(cartService.processTransaction(processRequest));

        return ResponseEntity.ok(text);
    }

    @PostMapping("/post")
    public ResponseEntity<String> postTest(@RequestBody ProcessRequest processRequest) {
        //  return   ResponseEntity.ok(cartService.processTransaction(processRequest));

        return ResponseEntity.ok(processRequest.currency());
    }


}
