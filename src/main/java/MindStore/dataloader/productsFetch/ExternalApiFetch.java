package MindStore.dataloader.productsFetch;

import MindStore.converters.MainConverterI;
import MindStore.persistence.models.Person.User;
import MindStore.persistence.models.Product.AverageRating;
import MindStore.persistence.models.Product.Category;
import MindStore.persistence.models.Product.IndividualRating;
import MindStore.persistence.models.Product.Product;
import MindStore.persistence.repositories.Product.AverageRatingRepository;
import MindStore.persistence.repositories.Product.CategoryRepository;
import MindStore.persistence.repositories.Product.IndividualRatingRepository;
import MindStore.persistence.repositories.Product.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class ExternalApiFetch {
    //implementação + facil no user branch
    public static void externalApi(List<User> users, RestTemplate restTemplate, MainConverterI mainConverter,
                                   CategoryRepository categoryRepository, AverageRatingRepository avRatingRepository,
                                   IndividualRatingRepository indRatingRepository, ProductRepository productRepository) {
        //se codigo partir aqui vai para o catch (se ja houver produtos iguais na DB por ex. ou se API externa deixar de funcionar)
        //para o server nao partir caso isso aconteça e a app continuar a rodar sem a external api
        try {
            ResponseEntity<ApiProduct[]> response = restTemplate.getForEntity("https://fakestoreapi.com/products", ApiProduct[].class);
            ApiProduct[] products = response.getBody();

            //se nao houver products quer dizer que o vetor nao iniciou e é nulo
            if (products != null) {
                for (ApiProduct product : products) {
                    //converter objetos todos:
                    //passar products e ratings para os objetos que fizemos pra mapear
                    Product productEntity = mainConverter.converter(product, Product.class);
                    //AverageRating ratingEntity = this.mainConverter.converter(product.getRating(), AverageRating.class);


                    //para converter categoria (do json) de string para objeto:
                    //scope

                    Category category;

                    //findbycategory query do categoryjpa
                    //se n houver categorias no repositorio construimos categoria, se nao vamos buscar a que existe

                    if (categoryRepository.findByCategory(product.getCategory()).isEmpty()) {
                        category = Category.builder()
                                .category(product.getCategory())
                                .build();

                        categoryRepository.saveAndFlush(category);
                    } else
                        category = categoryRepository.findByCategory(product.getCategory()).get();
                    //get para ir buscar valor do optional (como fizemos o if else)


                    //0 e 8 para o loop abaixo
                    //para nao ser sempre o mesmo nr de avaliaçoes no produtos
                    int count = (int) (Math.random() * 8);
                    List<IndividualRating> userRatings = new ArrayList<>();

                    //buscar os usuarios pela ordem onde estao (index no vetor) ate ao nr random (count)
                    for (int i = 0; i < count; i++) {
                        //criar individual ratings random
                        IndividualRating userRating = IndividualRating.builder()
                                .rate((int) (Math.random() * 4) + 2)
                                .productTitle(productEntity.getTitle())
                                .userId(users.get(i))
                                .build();

                        userRatings.add(userRating);
                    }

                    //rate
                    double average = userRatings.stream().mapToDouble(IndividualRating::getRate).average().orElse(0);

                    //constroi a average dos ratings individuais
                    AverageRating averageRating = AverageRating.builder()
                            .rate(average)
                            .count(count)
                            .build();

                    //se n houver este avg rating no repositorio (vemos pelos titulos) das save and flush (para n partir data loader)
                    if (avRatingRepository.findByProductTitle(productEntity.getTitle()).isEmpty()) {
                        avRatingRepository.saveAndFlush(averageRating);
                        //para estabelecer ja os user ratings na relaçao com avg
                        userRatings.forEach(x -> x.setAverageRatingId(averageRating));
                        indRatingRepository.saveAllAndFlush(userRatings);
                    }

                    //ver se o produto existe senao guarda e depois associa as tabelas de relaçoes
                    if (productRepository.findByTitle(productEntity.getTitle()).isEmpty()) {
                        productEntity.setRatingId(averageRating);
                        productEntity.setCategory(category);
                        productEntity.setStock((int) (Math.random() * 15) + 2);

                        productRepository.saveAndFlush(productEntity);
                    }
                }
            }
            //exceção ignored generica so para continuar a funcionar o server se partir aqui
        } catch (Exception ignored) {

        }
    }
}
