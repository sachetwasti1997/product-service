package com.sachet.parallel_asynchronous.utils;

import com.sachet.parallel_asynchronous.model.CacheCount;
import com.sachet.parallel_asynchronous.model.PaginationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.File;

public class ProductUtils {

    private static final String pathToCacheFile = "src/main/resources/cacheCount.json";
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductUtils.class);

    public static CacheCount readCacheCount() {
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            return objectMapper.readValue(new File(pathToCacheFile), CacheCount.class);
        }catch (JacksonException e){
            LOGGER.error("Caught an exception while trying to read cache {}", e.getMessage());
        }
        return null;
    }

    public static void writeCacheCount(CacheCount cacheCount) {
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            objectMapper.writeValue(new File(pathToCacheFile), cacheCount);
            LOGGER.info("Updated cache count with the value {}", cacheCount);
        }catch (JacksonException e){
            LOGGER.error("Caught an exception while trying to read cache {}", e.getMessage());
        }
    }

    public static Pageable getPageable(PaginationRequest request) {
        return PageRequest.of(request.getPage(), request.getSize(), request.getDirection(), request.getSortField());
    }

}
