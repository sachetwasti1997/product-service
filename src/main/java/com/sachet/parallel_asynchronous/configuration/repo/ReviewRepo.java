package com.sachet.parallel_asynchronous.configuration.repo;

import com.sachet.parallel_asynchronous.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepo extends JpaRepository<Review, Long> {
}
