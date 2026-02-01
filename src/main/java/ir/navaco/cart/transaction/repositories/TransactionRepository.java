package ir.navaco.cart.transaction.repositories;

import ir.navaco.cart.transaction.CartTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<CartTransaction,String> {

    @Query("select t from CartTransaction t where t.rrn=:rrn")
    Optional<CartTransaction> findByRrn(String rrn);
}
