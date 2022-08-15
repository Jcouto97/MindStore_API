package MindStore.dataloader.productsFetch;

import lombok.*;

//objeto dentro de api product

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ApiRating {
    private double rate;
    private int count;
}
