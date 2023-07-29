package searchengine.dto.statistics.responce;

import lombok.Value;

@Value
public class BadResponse {
    boolean result;
    String err;
}
