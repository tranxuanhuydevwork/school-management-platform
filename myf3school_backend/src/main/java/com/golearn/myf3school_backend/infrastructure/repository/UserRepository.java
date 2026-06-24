package com.golearn.myf3school_backend.infrastructure.repository;
import com.golearn.myf3school_backend.infrastructure.entity.User;
import com.golearn.myf3school_backend.contract.enums.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.username = :value OR u.email = :value OR u.phone = :value")
    Optional<User> findByUsernameOrEmail(@Param("value") String value);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Lọc theo role qua quan hệ ManyToMany (JOIN roles)
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :role")
    Page<User> findByRole(@Param("role") RoleType role, Pageable pageable);

    @Query("""
        SELECT u FROM User u
        WHERE (:role IS NULL OR EXISTS (
                SELECT r FROM u.roles r WHERE r.name = :role
              ))
          AND (:search IS NULL
               OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<User> search(@Param("role") RoleType role, @Param("search") String search, Pageable pageable);

    Optional<Object> findByPhone(String phoneNumber);
}
