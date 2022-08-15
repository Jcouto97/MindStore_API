package MindStore.dataloader.productsFetch;

import lombok.*;

//tem que ter props todas iguais a do product model para podermos guardar na DB
//@RequiredArgsConstructor so inicia as variaveis final

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class ApiProduct {
    private String title;
    private double price;
    private String description;
    private String category;
    private String image;
    private ApiRating rating;
}
