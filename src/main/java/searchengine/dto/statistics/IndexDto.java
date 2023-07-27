package searchengine.dto.statistics;

import lombok.Value;

@Value
public class IndexDto {
    long pageID;
    long lemmaID;
    float rank;
}

