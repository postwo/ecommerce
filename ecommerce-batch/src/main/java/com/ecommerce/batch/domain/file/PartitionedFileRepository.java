package com.ecommerce.batch.domain.file;


import com.ecommerce.batch.util.FileUtils;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class PartitionedFileRepository {

    //HashMap이 아닌 ConcurrentHashMap을 사용하는 이유 멀티 스레드 환경에서 사용될 것을 가정하고 스레드 안전성(Thread-Safety)과 동시성(Concurrency)을 확보하기 위함
    //HashMap의 문제점: 스레드 비안전성
    //ConcurrentHashMap의 역할: 스레드 안전성 및 동시성 보장
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