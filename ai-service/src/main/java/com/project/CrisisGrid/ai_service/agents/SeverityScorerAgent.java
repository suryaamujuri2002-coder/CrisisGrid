package com.project.CrisisGrid.ai_service.agents;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeverityScorerAgent {

    private final ChatModel chatModel;

    private static final String SEVERITY_PROMPT = """
            You are a crisis severity assessment expert. Rate the severity of this crisis on a scale of 1-10:

            1-3: Minor (no immediate danger to life)
            4-6: Moderate (potential danger, requires attention)
            7-9: High (significant danger, urgent response needed)
            10: Critical (extreme life-threatening emergency)

            Crisis Type: {crisisType}

            Title: {title}

            Description: {description}

            Consider factors like:
            - Immediate danger to human life
            - Number of people potentially affected
            - Urgency of response required
            - Potential for escalation

            Respond with ONLY a number between 1 and 10, followed by a brief one-sentence explanation.

            Format:
            <score> | <explanation>

            Example:
            8 | Multiple people trapped in burning building with rapid fire spread

            Assessment:
            """;

    public SeverityResult scoreSeverity(
            String crisisType,
            String title,
            String description) {

        try {

            PromptTemplate promptTemplate =
                    new PromptTemplate(SEVERITY_PROMPT);

            Prompt prompt = promptTemplate.create(Map.of(
                    "crisisType", crisisType,
                    "title", title,
                    "description", description
            ));

            String response = chatModel.call(prompt)
                    .getResult()
                    .getOutput()
                    .getText()
                    .trim();

            log.info("AI severity assessment: {}", response);

            return parseSeverityResponse(response);

        } catch (Exception e) {

            log.error("Error scoring severity, defaulting to 5", e);

            return new SeverityResult(
                    5,
                    "Unable to assess severity automatically"
            );
        }
    }

    private SeverityResult parseSeverityResponse(String response) {

        try {

            String[] parts = response.split("\\|", 2);

            int score = Integer.parseInt(parts[0].trim());

            String explanation = parts.length > 1
                    ? parts[1].trim()
                    : "No explanation provided";

            // Clamp score between 1 and 10
            score = Math.max(1, Math.min(10, score));

            return new SeverityResult(score, explanation);

        } catch (Exception e) {

            log.warn(
                    "Could not parse severity response, defaulting to 5",
                    e
            );

            return new SeverityResult(
                    5,
                    "Unable to parse AI response"
            );
        }
    }

    public record SeverityResult(
            int score,
            String explanation
    ) {
    }
}
