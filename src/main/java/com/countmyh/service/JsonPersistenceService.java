package com.countmyh.service;

import com.countmyh.model.WorkPeriodTracker;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class JsonPersistenceService {

    private static final String DEFAULT_DIR = System.getProperty("user.home") + "/.countmyhours";
    private static final String DEFAULT_FILE = "data.json";

    private final ObjectMapper mapper;
    private final Path dataFilePath;

    public JsonPersistenceService() {
        this(Path.of(DEFAULT_DIR, DEFAULT_FILE));
    }

    public JsonPersistenceService(Path dataFilePath) {
        this.dataFilePath = dataFilePath;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void save(WorkPeriodTracker data) throws IOException {
        Files.createDirectories(dataFilePath.getParent());

        Path tempFile = dataFilePath.resolveSibling(dataFilePath.getFileName() + ".tmp");
        mapper.writeValue(tempFile.toFile(), data);
        try {
            Files.move(tempFile, dataFilePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.FileSystemException e) {
            Files.move(tempFile, dataFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public WorkPeriodTracker load() throws IOException {
        if (!dataFileExists()) {
            return new WorkPeriodTracker();
        }
        return mapper.readValue(dataFilePath.toFile(), WorkPeriodTracker.class);
    }

    public boolean dataFileExists() {
        return Files.exists(dataFilePath);
    }

    public File getDataFile() {
        return dataFilePath.toFile();
    }
}
