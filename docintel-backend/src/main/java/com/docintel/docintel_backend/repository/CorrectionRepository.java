package com.docintel.docintel_backend.repository;

import com.docintel.docintel_backend.entity.Correction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CorrectionRepository extends JpaRepository<Correction, UUID> {}