package searchengine.dto.statistics.responce;
import lombok.Getter;
import lombok.Value;

@Value
public class IndexingResponse {
    boolean responseResult;
    String errorMsg;
}
