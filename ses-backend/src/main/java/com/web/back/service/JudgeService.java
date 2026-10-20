package com.web.back.service;

import com.web.back.dto.SubmitDto;
import com.web.back.model.Testcase;
import java.util.List;

public interface JudgeService {
    SubmitDto grade(Integer allowedLanguage, SubmitDto submission, List<Testcase> testcases, String noTestsMessage);
}
