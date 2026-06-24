package com.golearn.myf3school_backend.infrastructure.repository;

import com.golearn.myf3school_backend.infrastructure.entity.ParentStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParentStudentRepository extends JpaRepository<ParentStudent, Long> {
    @Query("""
        SELECT ps FROM ParentStudent ps
        JOIN FETCH ps.student s
        JOIN FETCH s.schoolClass
        JOIN FETCH s.user
        WHERE ps.parent.id = :parentId
    """)
    List<ParentStudent> findByParentId(@Param("parentId") Long parentId);
}