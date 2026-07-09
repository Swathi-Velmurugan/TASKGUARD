package com.example.taskguard.repository;

import java.util.List;
import java.util.Optional;
import com.example.taskguard.entity.enums.DomainRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taskguard.entity.SystemAccount;


@Repository
public interface SystemAccountRepository extends JpaRepository<SystemAccount,Long>{
    Optional<SystemAccount> findByEmail(String email);
    boolean existsByEmail(String email);
    List<SystemAccount> findByDomainRole(DomainRole domainRole);
}

