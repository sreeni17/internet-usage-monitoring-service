package iums.service;

import iums.domain.ParsedData;

import java.io.BufferedReader;
import java.io.IOException;

public interface IngestService {
    void ingest(String filePath);

    ParsedData parse(BufferedReader reader) throws IOException;
}
