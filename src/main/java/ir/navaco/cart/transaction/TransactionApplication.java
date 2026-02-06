package ir.navaco.cart.transaction;

//import dev.peyman.framework.JwtService;
import ir.navaco.cart.transaction.serviceImpl.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
public class TransactionApplication {

/*    @Autowired
    private JwtService jwtService;*/
    public static void main(String[] args) {
        SpringApplication.run(TransactionApplication.class, args);
    }

/*
    @GetMapping("auth/generate-test-token")
    public String generate(@RequestParam String name) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", List.of("ROLE_USER"));

        return jwtService.generateToken(name, claims);
    }*/
}
