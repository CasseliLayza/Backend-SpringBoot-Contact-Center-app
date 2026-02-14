package com.backend.clients.repository;

import com.backend.clients.model.Call;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CallRepository  extends JpaRepository<Call, Long> {

}
