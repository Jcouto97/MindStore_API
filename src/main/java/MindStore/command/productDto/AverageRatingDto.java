package MindStore.command.productDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AverageRatingDto implements Serializable {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int id;

    @Min(0)
    @Max(5)
    private double rate;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int count;
}
