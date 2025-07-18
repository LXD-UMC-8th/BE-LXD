package org.lxdproject.lxd.diary.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.lxdproject.lxd.diary.dto.QuestionRequestDTO;
import org.lxdproject.lxd.diary.dto.QuestionResponseDTO;
import org.lxdproject.lxd.diary.entity.Question;
import org.lxdproject.lxd.diary.repository.QuestionRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;

    @Transactional(readOnly = true)
    public QuestionResponseDTO getRandomQuestion(QuestionRequestDTO request) {
        Question question = questionRepository.findRandomQuestionByLanguage(request.getLanguage().name());
        return QuestionResponseDTO.builder()
                .id(question.getId())
                .content(question.getContent())
                .language(question.getLanguage())
                .build();
    }
}
