package com.xloop.resourceloop.authenticationservice.JPARepository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xloop.resourceloop.authenticationservice.Model.User;

public interface UserRepository extends JpaRepository<User,Long> {
    
}
