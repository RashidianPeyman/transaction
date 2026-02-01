package ir.navaco.cart.transaction.repositories;

import ir.navaco.cart.transaction.Cart;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CartRepositoryJpa extends JpaRepository<Cart, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cart c where c.id=:id")
    Optional<Cart> findById(Long id);
}
