package MindStore.dataloader.manual;

import MindStore.persistence.models.Person.User;
import MindStore.persistence.models.Product.AverageRating;
import MindStore.persistence.models.Product.Category;
import MindStore.persistence.models.Product.IndividualRating;
import MindStore.persistence.models.Product.Product;
import MindStore.persistence.repositories.Product.AverageRatingRepository;
import MindStore.persistence.repositories.Product.IndividualRatingRepository;

import java.util.ArrayList;
import java.util.List;

public class MensClothingFactory {
    public static List<Product> manClothingGenarator(List<User> users, Category mensClothing,
                                                     AverageRatingRepository avRatingRepository,
                                                     IndividualRatingRepository indRatingRepository) {
        //Rating 1

        //para receber ratings random dos users
        List<Product> products = new ArrayList<>();
        int count = (int) (Math.random() * 8);
        List<IndividualRating> userRatings = new ArrayList<>();
        String title = "VLogo short-sleeved T-shirt";

        for (int i = 0; i < count; i++) {
            IndividualRating userRating = IndividualRating.builder()
                    .rate((int) (Math.random() * 4) + 2)
                    .productTitle(title)
                    .userId(users.get(i))
                    .build();

            userRatings.add(userRating);
        }
        double average = userRatings.stream().mapToDouble(IndividualRating::getRate).average().orElse(0);

        AverageRating averageRating1 = AverageRating.builder()
                .rate(average)
                .count(count)
                .build();

        //Product 1
        Product product = Product.builder()
                .title(title)
                .price(349.99)
                .description("An homage to Valentino Garavani's VLogo Signature, " +
                        "this black short-sleeved T-shirt features the house's " +
                        "iconic branding in contrast white to the chest.")
                .category(mensClothing)
                .image("https://cdn-images.farfetch-contents.com/18/36/38/25/18363825_40033351_1000.jpg")
                .stock((int) (Math.random() * 15) + 2)
                .ratingId(averageRating1)
                .build();

        products.add(product);

        //guarda ratings no rep
        //produtos depois guardam-se no rep no data loader (é igual)
        if (avRatingRepository.findByProductTitle(product.getTitle()).isEmpty()) {
            avRatingRepository.saveAndFlush(averageRating1);
            userRatings.forEach(x -> x.setAverageRatingId(averageRating1));
            indRatingRepository.saveAllAndFlush(userRatings);
        }

        //-----------------------------
        //Rating 2
        count = (int) (Math.random() * 8);
        userRatings = new ArrayList<>();
        title = "Monogram-pattern knee-length shorts";

        for (int i = 0; i < count; i++) {
            IndividualRating userRating = IndividualRating.builder()
                    .rate((int) (Math.random() * 5) + 1)
                    .productTitle(title)
                    .userId(users.get(i))
                    .build();

            userRatings.add(userRating);
        }
        average = userRatings.stream().mapToDouble(IndividualRating::getRate).average().orElse(0);

        AverageRating averageRating2 = AverageRating.builder()
                .rate(average)
                .count(count)
                .build();

        //Product 2
        product = Product.builder()
                .title(title)
                .price(719.99)
                .description("Taken from the house's '70s archives, the GG monogram; " +
                        "Guccio Gucci's initials, are a key feature of Gucci designs. " +
                        "Taking on many different iterations, it is showcased on " +
                        "these knee-length shorts in a patterned jacquard material.")
                .category(mensClothing)
                .image("https://cdn-images.farfetch-contents.com/18/05/10/41/18051041_40936990_1000.jpg")
                .stock((int) (Math.random() * 15) + 2)
                .ratingId(averageRating2)
                .build();

        products.add(product);

        if (avRatingRepository.findByProductTitle(product.getTitle()).isEmpty()) {
            avRatingRepository.saveAndFlush(averageRating2);
            userRatings.forEach(x -> x.setAverageRatingId(averageRating2));
            indRatingRepository.saveAllAndFlush(userRatings);
        }

        //-----------------------------
        //Rating 3
        count = (int) (Math.random() * 8);
        userRatings = new ArrayList<>();
        title = "VLogo embroidered cap";

        for (int i = 0; i < count; i++) {
            IndividualRating userRating = IndividualRating.builder()
                    .rate((int) (Math.random() * 4) + 2)
                    .productTitle(title)
                    .userId(users.get(i))
                    .build();

            userRatings.add(userRating);
        }
        average = userRatings.stream().mapToDouble(IndividualRating::getRate).average().orElse(0);

        AverageRating averageRating3 = AverageRating.builder()
                .rate(average)
                .count(count)
                .build();

        //Product 3
        product = Product.builder()
                .title(title)
                .price(349.99)
                .description("Valentino Garavani first started marking his creations in 1968, " +
                        "for his iconic show at The Sala Bianca in Florence. The house has since " +
                        "reimagined the iconic logo and applied it to their SS22 collection, adorning " +
                        "the front of this cotton cap with its signature VLogo.")
                .category(mensClothing)
                .image("https://cdn-images.farfetch-contents.com/17/53/00/51/17530051_40729716_1000.jpg")
                .stock((int) (Math.random() * 15) + 2)
                .ratingId(averageRating3)
                .build();

        products.add(product);

        if (avRatingRepository.findByProductTitle(product.getTitle()).isEmpty()) {
            avRatingRepository.saveAndFlush(averageRating3);
            userRatings.forEach(x -> x.setAverageRatingId(averageRating3));
            indRatingRepository.saveAllAndFlush(userRatings);
        }

        //-----------------------------
        //Rating 4
        count = (int) (Math.random() * 8);
        userRatings = new ArrayList<>();
        title = "High-waisted tailored trousers";

        for (int i = 0; i < count; i++) {
            IndividualRating userRating = IndividualRating.builder()
                    .rate((int) (Math.random() * 3) + 3)
                    .productTitle(title)
                    .userId(users.get(i))
                    .build();

            userRatings.add(userRating);
        }
        average = userRatings.stream().mapToDouble(IndividualRating::getRate).average().orElse(0);

        AverageRating averageRating4 = AverageRating.builder()
                .rate(average)
                .count(count)
                .build();

        //Product 4
        product = Product.builder()
                .title(title)
                .price(749.99)
                .description("A testimony to Valentino's impeccable tailoring, these trousers are " +
                        "all about sophistication. Crafted from virgin wool with a bit of stretch, " +
                        "the chocolate brown hue and pressed crease add a refined touch to the pair.")
                .category(mensClothing)
                .image("https://cdn-images.farfetch-contents.com/17/88/75/93/17887593_37953720_1000.jpg")
                .stock((int) (Math.random() * 15) + 2)
                .ratingId(averageRating4)
                .build();

        products.add(product);

        if (avRatingRepository.findByProductTitle(product.getTitle()).isEmpty()) {
            avRatingRepository.saveAndFlush(averageRating4);
            userRatings.forEach(x -> x.setAverageRatingId(averageRating4));
            indRatingRepository.saveAllAndFlush(userRatings);
        }

        //-----------------------------
        //Rating 5
        count = (int) (Math.random() * 8);
        userRatings = new ArrayList<>();
        title = "Logo bomber jacket";

        for (int i = 0; i < count; i++) {
            IndividualRating userRating = IndividualRating.builder()
                    .rate((int) (Math.random() * 2) + 4)
                    .productTitle(title)
                    .userId(users.get(i))
                    .build();

            userRatings.add(userRating);
        }
        average = userRatings.stream().mapToDouble(IndividualRating::getRate).average().orElse(0);

        AverageRating averageRating5 = AverageRating.builder()
                .rate(average)
                .count(count)
                .build();

        //Product 5
        product = Product.builder()
                .title(title)
                .price(1389.99)
                .description("Logo bomber jacket from VALENTINO.")
                .category(mensClothing)
                .image("https://cdn-images.farfetch-contents.com/15/28/33/98/15283398_28202420_1000.jpg")
                .stock((int) (Math.random() * 15) + 2)
                .ratingId(averageRating5)
                .build();

        products.add(product);

        if (avRatingRepository.findByProductTitle(product.getTitle()).isEmpty()) {
            avRatingRepository.saveAndFlush(averageRating5);
            userRatings.forEach(x -> x.setAverageRatingId(averageRating5));
            indRatingRepository.saveAllAndFlush(userRatings);
        }

        //-----------------------------
        //Rating 6
        count = (int) (Math.random() * 8);
        userRatings = new ArrayList<>();
        title = "Long-sleeve striped polo shirt";

        for (int i = 0; i < count; i++) {
            IndividualRating userRating = IndividualRating.builder()
                    .rate((int) (Math.random() * 2) + 4)
                    .productTitle(title)
                    .userId(users.get(i))
                    .build();

            userRatings.add(userRating);
        }
        average = userRatings.stream().mapToDouble(IndividualRating::getRate).average().orElse(0);

        AverageRating averageRating6 = AverageRating.builder()
                .rate(average)
                .count(count)
                .build();

        //Product 6
        product = Product.builder()
                .title(title)
                .price(1149.99)
                .description("White/blue cotton/silk long-sleeve striped polo shirt from PRADA " +
                        "featuring horizontal stripe pattern, triangle logo, polo collar, front " +
                        "button placket and long sleeves.")
                .category(mensClothing)
                .image("https://cdn-images.farfetch-contents.com/18/64/76/63/18647663_40499710_1000.jpg")
                .stock((int) (Math.random() * 15) + 2)
                .ratingId(averageRating6)
                .build();

        products.add(product);

        if (avRatingRepository.findByProductTitle(product.getTitle()).isEmpty()) {
            avRatingRepository.saveAndFlush(averageRating6);
            userRatings.forEach(x -> x.setAverageRatingId(averageRating6));
            indRatingRepository.saveAllAndFlush(userRatings);
        }

        //-----------------------------
        //Rating 7
        count = (int) (Math.random() * 8);
        userRatings = new ArrayList<>();
        title = "Short-sleeved cotton shirt";

        for (int i = 0; i < count; i++) {
            IndividualRating userRating = IndividualRating.builder()
                    .rate((int) (Math.random() * 4) + 2)
                    .productTitle(title)
                    .userId(users.get(i))
                    .build();

            userRatings.add(userRating);
        }
        average = userRatings.stream().mapToDouble(IndividualRating::getRate).average().orElse(0);

        AverageRating averageRating7 = AverageRating.builder()
                .rate(average)
                .count(count)
                .build();

        //Product 7
        product = Product.builder()
                .title(title)
                .price(759.99)
                .description("Prada keeps it minimal with this cotton shirt, presented in a " +
                        "sophisticated two-tonal design in shades of blue. The brand's iconic " +
                        "triangle logo appears in a stripped-down design on the chest pocket, in " +
                        "tune with the house's new understated approach to branding.")
                .category(mensClothing)
                .image("https://cdn-images.farfetch-contents.com/18/42/63/93/18426393_39564934_1000.jpg")
                .stock((int) (Math.random() * 15) + 2)
                .ratingId(averageRating7)
                .build();

        products.add(product);

        if (avRatingRepository.findByProductTitle(product.getTitle()).isEmpty()) {
            avRatingRepository.saveAndFlush(averageRating7);
            userRatings.forEach(x -> x.setAverageRatingId(averageRating7));
            indRatingRepository.saveAllAndFlush(userRatings);
        }

        //-----------------------------
        //Rating 8
        count = (int) (Math.random() * 8);
        userRatings = new ArrayList<>();
        title = "Twisted Seam denim jeans";

        for (int i = 0; i < count; i++) {
            IndividualRating userRating = IndividualRating.builder()
                    .rate((int) (Math.random() * 4) + 2)
                    .productTitle(title)
                    .userId(users.get(i))
                    .build();

            userRatings.add(userRating);
        }
        average = userRatings.stream().mapToDouble(IndividualRating::getRate).average().orElse(0);

        AverageRating averageRating8 = AverageRating.builder()
                .rate(average)
                .count(count)
                .build();

        //Product 8
        product = Product.builder()
                .title(title)
                .price(694.99)
                .description("Versace offers up a new take on denim with these straight-leg jeans " +
                        "that feature a Twisted Seam detail to each outer edge. Fabricated in Italy, " +
                        "they are detailed with a signature Medusa logo-embossed button to the front.")
                .category(mensClothing)
                .image("https://cdn-images.farfetch-contents.com/17/97/10/74/17971074_40010139_1000.jpg")
                .stock((int) (Math.random() * 15) + 2)
                .ratingId(averageRating8)
                .build();

        products.add(product);

        if (avRatingRepository.findByProductTitle(product.getTitle()).isEmpty()) {
            avRatingRepository.saveAndFlush(averageRating8);
            userRatings.forEach(x -> x.setAverageRatingId(averageRating8));
            indRatingRepository.saveAllAndFlush(userRatings);
        }

        //-----------------------------
        //Rating 9
        count = (int) (Math.random() * 8);
        userRatings = new ArrayList<>();
        title = "Paint-splatter denim jacket";

        for (int i = 0; i < count; i++) {
            IndividualRating userRating = IndividualRating.builder()
                    .rate((int) (Math.random() * 4) + 2)
                    .productTitle(title)
                    .userId(users.get(i))
                    .build();

            userRatings.add(userRating);
        }
        average = userRatings.stream().mapToDouble(IndividualRating::getRate).average().orElse(0);

        AverageRating averageRating9 = AverageRating.builder()
                .rate(average)
                .count(count)
                .build();

        //Product 9
        product = Product.builder()
                .title(title)
                .price(644.99)
                .description("Paint-splatter denim jacket from OFF-WHITE featuring black/white, cotton, " +
                        "paint splatter detail, signature Diag-stripe print, front button fastening, long " +
                        "sleeves, two patch pockets and two side slit pockets.")
                .category(mensClothing)
                .image("https://cdn-images.farfetch-contents.com/18/71/70/76/18717076_40458141_1000.jpg")
                .stock((int) (Math.random() * 15) + 2)
                .ratingId(averageRating9)
                .build();

        products.add(product);

        if (avRatingRepository.findByProductTitle(product.getTitle()).isEmpty()) {
            avRatingRepository.saveAndFlush(averageRating9);
            userRatings.forEach(x -> x.setAverageRatingId(averageRating9));
            indRatingRepository.saveAllAndFlush(userRatings);
        }

        //-----------------------------
        //Rating 10
        count = (int) (Math.random() * 8);
        userRatings = new ArrayList<>();
        title = "Logo plaque belt buckle";

        for (int i = 0; i < count; i++) {
            IndividualRating userRating = IndividualRating.builder()
                    .rate((int) (Math.random() * 4) + 2)
                    .productTitle(title)
                    .userId(users.get(i))
                    .build();

            userRatings.add(userRating);
        }
        average = userRatings.stream().mapToDouble(IndividualRating::getRate).average().orElse(0);

        AverageRating averageRating10 = AverageRating.builder()
                .rate(average)
                .count(count)
                .build();

        //Product 10
        product = Product.builder()
                .title(title)
                .price(429.99)
                .description("Amiri's bold take on accessories for AW22 is shown by this black " +
                        "statement belt. Crafted from soft buttery leather, it boasts silver-tone " +
                        "hardware for an industrial-inspired look whilst showcasing the brand's logo at the buckle.")
                .category(mensClothing)
                .image("https://cdn-images.farfetch-contents.com/17/92/31/93/17923193_40383125_1000.jpg")
                .stock((int) (Math.random() * 15) + 2)
                .ratingId(averageRating10)
                .build();

        products.add(product);

        if (avRatingRepository.findByProductTitle(product.getTitle()).isEmpty()) {
            avRatingRepository.saveAndFlush(averageRating10);
            userRatings.forEach(x -> x.setAverageRatingId(averageRating10));
            indRatingRepository.saveAllAndFlush(userRatings);
        }

        return products;
    }
}
