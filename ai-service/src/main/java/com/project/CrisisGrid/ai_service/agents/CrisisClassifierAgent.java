package com.project.CrisisGrid.ai_service.agents;




import com.project.CrisisGrid.ai_service.enums.CrisisType;
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
public class CrisisClassifierAgent {


    private final ChatModel chatModel;

    private static final String CLASSIFICATION_PROMPT = """
        You are a crisis classification expert. Analyze the following crisis report and classify it into ONE of these categories:
        
        FIRE, FLOOD, MEDICAL, EARTHQUAKE, ACCIDENT, CHEMICAL, INFRASTRUCTURE, OTHER
        
        Crisis Title:
        {title}
        
        Crisis Description:
        {description}
        
        Respond with ONLY the category name, nothing else.
        
        Examples:
        "House on fire" -> FIRE
        "Person having chest pain" -> MEDICAL
        "Car crash on highway" -> ACCIDENT
        "Road collapsed" -> INFRASTRUCTURE
        
        Classification:
        """;

    public CrisisType classifyCrisis(String title, String description) {

        try {

            PromptTemplate promptTemplate = new PromptTemplate(CLASSIFICATION_PROMPT);

            Prompt prompt = promptTemplate.create(
                    Map.of(
                            "title", title,
                            "description", description
                    )
            );

            String response = chatModel.call(prompt)
                    .getResult()
                    .getOutput()
                    .getText()
                    .trim()
                    .toUpperCase();

            log.info("AI classified crisis as: {}", response);

            return parseClassification(response);

        } catch (Exception e) {

            log.error("Error classifying crisis, defaulting to OTHER", e);
            return CrisisType.OTHER;
        }
    }

    private CrisisType parseClassification(String response) {

        try {

            return CrisisType.valueOf(response);

        } catch (IllegalArgumentException e) {

            log.warn("Invalid classification '{}', defaulting to OTHER", response);

            return CrisisType.OTHER;
        }
    }
}