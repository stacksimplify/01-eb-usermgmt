package com.utc.nda.authorizationserver.users.repositories;
import org.springframework.data.jpa.repository.JpaRepository;

import com.utc.nda.authorizationserver.users.entities.Verification;

public interface VerificationRepository extends JpaRepository<Verification, String> {

}