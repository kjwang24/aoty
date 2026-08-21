package com.kjwang24.aoty.repository;

import com.kjwang24.aoty.entity.User;
import com.kjwang24.aoty.entity.SpotifyCredential;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpotifyCredentialRepository extends JpaRepository<SpotifyCredential, Long>{
    
    Optional<SpotifyCredential> findByUser(User user);

}
