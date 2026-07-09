package com.example.taskguard.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taskguard.entity.SystemAccount;
import com.example.taskguard.entity.enums.DomainRole;
import com.example.taskguard.service.SystemAccountService;

@RestController
@RequestMapping("/api/accounts")
public class SystemAccountController {

    final SystemAccountService service;

    public SystemAccountController(SystemAccountService service) {
        this.service = service;
    }

    // POST /api/accounts
    @PreAuthorize("hasRole('PROJECT_DIRECTOR')")
    @PostMapping
    public ResponseEntity<SystemAccount> provisionAccount(@RequestBody SystemAccount account) {
        SystemAccount saved = service.provisionAccount(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // GET /api/accounts?role=ALL
    @GetMapping
    @PreAuthorize("hasRole('PROJECT_DIRECTOR')")
    public ResponseEntity<List<SystemAccount>> listAccounts(
            @RequestParam(required = false) DomainRole role) {
        return ResponseEntity.ok(service.listAccounts(role));
    }

    // GET /api/accounts/1
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PROJECT_DIRECTOR')")
    public ResponseEntity<SystemAccount> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getAccountById(id));
    }

    // PUT /api/accounts/1
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROJECT_DIRECTOR')")
    public ResponseEntity<SystemAccount> updateAccount(
            @PathVariable Long id,
            @RequestBody SystemAccount account) {
        return ResponseEntity.ok(service.updateAccount(id, account));
    }

    // PATCH /api/accounts/1/toggle-status
    @PreAuthorize("hasRole('PROJECT_DIRECTOR')")
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<SystemAccount> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(service.toggleAccountStatus(id));
    }

    // DELETE /api/accounts/1
    @PreAuthorize("hasRole('PROJECT_DIRECTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAccount(@PathVariable Long id) {
        service.deleteAccount(id);
        return ResponseEntity.ok("Account deleted successfully");
    }
}
