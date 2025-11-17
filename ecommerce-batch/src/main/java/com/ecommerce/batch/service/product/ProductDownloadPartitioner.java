package com.ecommerce.batch.service.product;

import com.ecommerce.batch.domain.file.PartitionedFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductDownloadPartitioner implements Partitioner {

    private final ProductService productService;
    private final PartitionedFileRepository partitionedFileRepository;

    /**
     * 전체 작업을 여러 개의 하위 작업으로 분할하는 핵심 메서드입니다.
     * @param gridSize 동시에 실행할 파티션(스레드)의 개수입니다.
     * @return 각 파티션의 이름(Key)과, 해당 파티션이 사용할 데이터(Value)를 담은 Map을 반환합니다.
     */
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        // 1. DB에서 모든 상품의 ID를 가져와서 오름차순으로 정렬합니다.
        List<String> productIds = productService.getProductIds().stream()
                .sorted()
                .toList();

        // 2. 전체 데이터의 시작 인덱스와 마지막 인덱스를 설정합니다.
        int minIdx = 0;
        int maxIdx = productIds.size() - 1;

        // 3. 파티션의 결과를 담을 Map 객체를 생성합니다
        Map<String, ExecutionContext> result = new HashMap<>();

        // 4. 각 파티션(스레드)이 담당할 데이터의 개수를 계산합니다.
        // (전체 데이터 개수 / 스레드 개수)를 올림한 값과 유사합니다.
        int targetSize = (maxIdx - minIdx) / gridSize + 1;

        // 5. 파티션을 나누기 위한 변수들을 초기화합니다.
        int number = 0; // 파티션 번호 (0, 1, 2, ...)
        int start = minIdx; // 현재 파티션이 담당할 데이터의 시작 인덱스
        int end = start + targetSize - 1; // 현재 파티션이 담당할 데이터의 끝 인덱스

        // 6. 모든 데이터가 파티션에 할당될 때까지 반복합니다.
        while (start <= maxIdx) {
            // 7. 현재 파티션을 위한 ExecutionContext를 생성합니다.
            // ExecutionContext는 각 파티션(워커 스텝)에 전달될 "작업 지시서" 역할을 합니다.
            ExecutionContext context = new ExecutionContext();

            // 8. 현재 파티션의 고유한 키(이름)를 생성합니다. (예: "partition0", "partition1")
            String partitionKey = "partition" + number;
            // 결과 Map에 파티션 키와 작업 지시서를 저장합니다.
            result.put(partitionKey, context);

            // 9. 계산된 끝 인덱스가 실제 마지막 인덱스를 넘어가지 않도록 조정합니다.
            if (end >= maxIdx) {
                end = maxIdx;
            }
            // 10. 작업 지시서(ExecutionContext)에 이 파티션이 처리할 productId의 최소값과 최대값을 저장합니다.
            // 이 값들은 나중에 각 워커 스텝의 ItemReader에서 "WHERE productId BETWEEN ? AND ?" 조건으로 사용됩니다
            context.putString("minId", productIds.get(start));
            context.putString("maxId", productIds.get(end));
            try {
                // 11. 각 파티션이 결과물을 저장할 개별 임시 CSV 파일을 생성합니다.
                // partitionedFileRepository는 중복되지 않는 임시 파일을 생성하고 관리하는 역할을 합니다.
                File partitionFile = partitionedFileRepository.putFile(partitionKey,
                        "partition" + number + "_", ".csv");
                // 작업 지시서에 생성된 임시 파일의 정보를 저장합니다.
                // 이 값은 각 워커 스텝의 ItemWriter에서 출력 파일 경로로 사용됩니다.
                context.put("file", partitionFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // 12. 다음 파티션의 시작/끝 인덱스를 계산하고, 파티션 번호를 증가시킵니다.
            start += targetSize;
            end += targetSize;
            number++;
        }
        // 이 Map은 PartitionHandler에게 전달되어 실제 스텝 실행에 사용됩니다.
        return result;
    }

}
