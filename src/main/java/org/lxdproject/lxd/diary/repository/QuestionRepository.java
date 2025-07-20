package org.lxdproject.lxd.diary.repository;

import org.lxdproject.lxd.diary.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = "SELECT * FROM 질문 WHERE language = :language ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Question findRandomQuestionByLanguage(@Param("language") String language);
}