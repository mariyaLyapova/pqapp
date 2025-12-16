package com.promptquest.service;

import com.google.cloud.bigquery.*;
import com.promptquest.entity.Question;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * BigQuery service for managing questions
 * Used when running on Google Cloud Run
 */
@Service
@Profile("bigquery")
public class BigQueryQuestionService {

    private final BigQuery bigQuery;
    private final String datasetName;
    private final String tableName;

    public BigQueryQuestionService(
            @Value("${gcp.project.id}") String projectId,
            @Value("${bigquery.dataset:promptquest_db}") String datasetName,
            @Value("${bigquery.table:questions}") String tableName) {
        
        this.bigQuery = BigQueryOptions.newBuilder()
                .setProjectId(projectId)
                .build()
                .getService();
        this.datasetName = datasetName;
        this.tableName = tableName;
        
        // Initialize table if it doesn't exist
        initializeTableIfNeeded();
    }

    /**
     * Create the questions table if it doesn't exist
     */
    private void initializeTableIfNeeded() {
        try {
            TableId tableId = TableId.of(datasetName, tableName);
            Table table = bigQuery.getTable(tableId);
            
            if (table == null) {
                // Define schema
                Schema schema = Schema.of(
                    Field.of("id", StandardSQLTypeName.STRING),
                    Field.of("question", StandardSQLTypeName.STRING),
                    Field.of("option_a", StandardSQLTypeName.STRING),
                    Field.of("option_b", StandardSQLTypeName.STRING),
                    Field.of("option_c", StandardSQLTypeName.STRING),
                    Field.of("option_d", StandardSQLTypeName.STRING),
                    Field.of("correct_answer", StandardSQLTypeName.STRING),
                    Field.of("explanation", StandardSQLTypeName.STRING),
                    Field.of("difficulty", StandardSQLTypeName.INT64),
                    Field.of("area", StandardSQLTypeName.STRING),
                    Field.of("skill", StandardSQLTypeName.STRING),
                    Field.of("degree", StandardSQLTypeName.STRING)
                );

                TableDefinition tableDefinition = StandardTableDefinition.of(schema);
                TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build();
                bigQuery.create(tableInfo);
                System.out.println("Table created successfully: " + tableName);
            }
        } catch (Exception e) {
            System.err.println("Error initializing table: " + e.getMessage());
        }
    }

    /**
     * Save a question to BigQuery
     */
    public void saveQuestion(Question question) {
        try {
            TableId tableId = TableId.of(datasetName, tableName);
            
            Map<String, Object> rowContent = new HashMap<>();
            rowContent.put("id", question.getId() != null ? question.getId().toString() : UUID.randomUUID().toString());
            rowContent.put("question", question.getQuestion());
            rowContent.put("option_a", question.getOptionA());
            rowContent.put("option_b", question.getOptionB());
            rowContent.put("option_c", question.getOptionC());
            rowContent.put("option_d", question.getOptionD());
            rowContent.put("correct_answer", question.getCorrectAnswer());
            rowContent.put("explanation", question.getExplanation());
            rowContent.put("difficulty", question.getDifficulty());
            rowContent.put("area", question.getArea());
            rowContent.put("skill", question.getSkill());
            rowContent.put("degree", question.getDegree());

            InsertAllRequest insertRequest = InsertAllRequest.newBuilder(tableId)
                    .addRow(rowContent)
                    .build();

            InsertAllResponse response = bigQuery.insertAll(insertRequest);
            
            if (response.hasErrors()) {
                throw new RuntimeException("Errors occurred while inserting rows: " + response.getInsertErrors());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error saving question to BigQuery", e);
        }
    }

    /**
     * Get all questions from BigQuery
     */
    public List<Question> getAllQuestions() {
        String query = String.format("SELECT * FROM `%s.%s.%s`", 
                bigQuery.getOptions().getProjectId(), datasetName, tableName);
        return executeQuery(query);
    }

    /**
     * Get questions by area
     */
    public List<Question> getQuestionsByArea(String area) {
        String query = String.format("SELECT * FROM `%s.%s.%s` WHERE area = @area", 
                bigQuery.getOptions().getProjectId(), datasetName, tableName);
        
        QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query)
                .addNamedParameter("area", QueryParameterValue.string(area))
                .build();
        
        return executeQuery(queryConfig);
    }

    /**
     * Get random questions with optional filters
     */
    public List<Question> getRandomQuestions(int limit, String area, Integer difficulty) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(String.format("SELECT * FROM `%s.%s.%s` WHERE 1=1", 
                bigQuery.getOptions().getProjectId(), datasetName, tableName));
        
        QueryJobConfiguration.Builder configBuilder = QueryJobConfiguration.newBuilder("");
        
        if (area != null && !area.isEmpty()) {
            queryBuilder.append(" AND area = @area");
            configBuilder.addNamedParameter("area", QueryParameterValue.string(area));
        }
        
        if (difficulty != null) {
            queryBuilder.append(" AND difficulty = @difficulty");
            configBuilder.addNamedParameter("difficulty", QueryParameterValue.int64(difficulty));
        }
        
        queryBuilder.append(" ORDER BY RAND() LIMIT @limit");
        configBuilder.addNamedParameter("limit", QueryParameterValue.int64(limit));
        
        QueryJobConfiguration queryConfig = configBuilder.setQuery(queryBuilder.toString()).build();
        return executeQuery(queryConfig);
    }

    /**
     * Count total questions
     */
    public long countQuestions() {
        String query = String.format("SELECT COUNT(*) as total FROM `%s.%s.%s`", 
                bigQuery.getOptions().getProjectId(), datasetName, tableName);
        
        try {
            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
            TableResult result = bigQuery.query(queryConfig);
            
            for (FieldValueList row : result.iterateAll()) {
                return row.get("total").getLongValue();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error counting questions", e);
        }
        
        return 0;
    }

    /**
     * Delete all questions
     */
    public void deleteAllQuestions() {
        String query = String.format("DELETE FROM `%s.%s.%s` WHERE TRUE", 
                bigQuery.getOptions().getProjectId(), datasetName, tableName);
        
        try {
            QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
            bigQuery.query(queryConfig);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting questions", e);
        }
    }

    /**
     * Execute a query and convert results to Question objects
     */
    private List<Question> executeQuery(String query) {
        QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(query).build();
        return executeQuery(queryConfig);
    }

    /**
     * Execute a query with configuration and convert results to Question objects
     */
    private List<Question> executeQuery(QueryJobConfiguration queryConfig) {
        List<Question> questions = new ArrayList<>();
        
        try {
            TableResult result = bigQuery.query(queryConfig);
            
            for (FieldValueList row : result.iterateAll()) {
                Question question = new Question();
                question.setId(parseLong(row.get("id").getStringValue()));
                question.setQuestion(row.get("question").getStringValue());
                question.setOptionA(row.get("option_a").getStringValue());
                question.setOptionB(row.get("option_b").getStringValue());
                question.setOptionC(row.get("option_c").getStringValue());
                question.setOptionD(row.get("option_d").getStringValue());
                question.setCorrectAnswer(row.get("correct_answer").getStringValue());
                question.setExplanation(getStringValue(row, "explanation"));
                question.setDifficulty(getIntegerValue(row, "difficulty"));
                question.setArea(getStringValue(row, "area"));
                question.setSkill(getStringValue(row, "skill"));
                question.setDegree(getStringValue(row, "degree"));
                
                questions.add(question);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error executing query", e);
        }
        
        return questions;
    }

    private String getStringValue(FieldValueList row, String fieldName) {
        FieldValue value = row.get(fieldName);
        return (value != null && !value.isNull()) ? value.getStringValue() : null;
    }

    private Integer getIntegerValue(FieldValueList row, String fieldName) {
        FieldValue value = row.get(fieldName);
        return (value != null && !value.isNull()) ? (int) value.getLongValue() : null;
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
