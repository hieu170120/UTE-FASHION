package com.example.demo.repository;

import com.example.demo.entity.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarrierRepository extends JpaRepository<Carrier, Integer> {
    List<Carrier> findByIsActiveTrue();
}
