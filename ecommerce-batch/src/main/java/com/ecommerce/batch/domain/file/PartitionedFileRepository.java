package com.ecommerce.batch.domain.file;


import com.ecommerce.batch.util.FileUtils;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class PartitionedFileRepository {

    private final ConcurrentMap<String, File> fileMap = new ConcurrentHashMap<>();

    public File putFile(String partition, String filePrefix, String fileSuffix) throws IOException {
        File file = FileUtils.createTempFile(filePrefix, fileSuffix);
        fileMap.put(partition, file);
        return file;
    }

    public List<File> getFiles() {
        return fileMap.values().stream()
                .sorted(Comparator.comparing(File::getName))
                .toList();
    }
}