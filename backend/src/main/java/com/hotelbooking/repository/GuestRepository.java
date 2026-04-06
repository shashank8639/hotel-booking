package com.hotelbooking.repository;

import com.hotelbooking.entity.Guest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {

    Optional<Guest> findByEmail(String email);

    Optional<Guest> findByEmailIgnoreCase(String email);

    Optional<Guest> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    List<Guest> findByLastNameContainingIgnoreCase(String lastName);

    Page<Guest> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName,
            String lastName,
            Pageable pageable
    );

    @Query("""
            SELECT g FROM Guest g
            WHERE LOWER(g.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
               OR LOWER(g.lastName) LIKE LOWER(CONCAT('%', :name, '%'))
               OR LOWER(CONCAT(g.firstName, ' ', g.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))
            """)
    List<Guest> searchByName(@Param("name") String name);
}
