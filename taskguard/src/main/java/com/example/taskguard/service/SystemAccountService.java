package com.example.taskguard.service;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.taskguard.entity.SystemAccount;
import com.example.taskguard.entity.enums.DomainRole;
import com.example.taskguard.exception.CapacityExceededException;
import com.example.taskguard.exception.ResourceNotFoundException;
import com.example.taskguard.repository.SystemAccountRepository;

import jakarta.transaction.Transactional;

@Service
public class SystemAccountService {

    final SystemAccountRepository repository;
    final PasswordEncoder passwordEncoder;

    public SystemAccountService(SystemAccountRepository repository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    // CREATE
    public SystemAccount provisionAccount(SystemAccount account) {
        if (repository.existsByEmail(account.getEmail())) {
            throw new CapacityExceededException("Email already registered: " + account.getEmail());
        }
        account.setPasswordHash(passwordEncoder.encode(account.getPasswordHash()));
        return repository.save(account);
    }

    // READ ALL with optional role filter
    public List<SystemAccount> listAccounts(DomainRole role) {
        if (role==null) {
            return repository.findAll();
        }
        return repository.findByDomainRole(role);
    }

    // GET BY ID
    public SystemAccount getAccountById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id " + id));
    }

    // GET BY EMAIL
    public SystemAccount getAccountByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with email " + email));
    }

    // UPDATE
    public SystemAccount updateAccount(Long id, SystemAccount updatedAccount) {
        SystemAccount existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id " + id));
        existing.setFullName(updatedAccount.getFullName());
        existing.setDomainRole(updatedAccount.getDomainRole());
        return repository.save(existing);
    }

    // TOGGLE ACTIVE/INACTIVE
    public SystemAccount toggleAccountStatus(Long id) {
        SystemAccount existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id " + id));
        existing.setActive(!existing.isActive());
        return repository.save(existing);
    }

    // DELETE
    @Transactional
    public void deleteAccount(Long id) {
        SystemAccount existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id " + id));
        repository.delete(existing);
    }
}
