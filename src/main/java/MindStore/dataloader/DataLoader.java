package MindStore.dataloader;

import MindStore.converters.MainConverterI;
import MindStore.persistence.models.Person.Role;
import MindStore.persistence.models.Person.User;
import MindStore.persistence.models.Product.Category;
import MindStore.persistence.models.Product.Product;
import MindStore.persistence.repositories.Person.AdminRepository;
import MindStore.persistence.repositories.Person.RoleRepository;
import MindStore.persistence.repositories.Person.UserRepository;
import MindStore.persistence.repositories.Product.CategoryRepository;
import MindStore.persistence.repositories.Product.IndividualRatingRepository;
import MindStore.persistence.repositories.Product.ProductRepository;
import MindStore.persistence.repositories.Product.AverageRatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static MindStore.dataloader.manual.AdminsFactory.generateAdmins;
import static MindStore.dataloader.manual.MensClothingFactory.manClothingGenarator;
import static MindStore.dataloader.manual.UsersFactory.generateUsers;
import static MindStore.dataloader.manual.WomensClothingFactory.womanClothingGenerator;
import static MindStore.dataloader.productsFetch.ExternalApiFetch.externalApi;

//aqui chamamos as funçoes estaticas de todas as classes para popular a base de dados (as entidades a mao e da API externa)

@RequiredArgsConstructor
@Component
public class DataLoader implements ApplicationRunner {
    private final RestTemplate restTemplate;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final MainConverterI mainConverter;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AdminRepository adminRepository;
    private final AverageRatingRepository avRatingRepository;
    private final IndividualRatingRepository indRatingRepository;
    private final PasswordEncoder encoder;

    @Override
    public void run(ApplicationArguments args) {

        //ver se o product rep ja tem 5 (nr aleatorio) produtos, se tiver é porque ja esta populado,
        //pq antes de ter o produto ja tem os ratings
        if (!this.productRepository.findAllByRatingASC(5, 0).isEmpty())
            return;

        //try catch caso haja colisoes na base de dados para app n partir
        try {
            //Roles
            Role userRole = Role.builder()
                    .roleType("USER")
                    .build();

            Role adminRole = Role.builder()
                    .roleType("ADMIN")
                    .build();

            List.of(userRole, adminRole)
                    .forEach(role -> {
                        //para ter a certeza que nao esta nada no rep para ter a certeza que data loader nao estoura
                        //qd commit 2x com update no yml
                        if (this.roleRepository.findByRoleType(role.getRoleType()).isEmpty())
                            this.roleRepository.saveAndFlush(role);
                    });

            //Admins
            generateAdmins(adminRole, encoder)
                    .forEach(admin -> {
                        if (this.adminRepository.findByEmail(admin.getEmail()).isEmpty())
                            this.adminRepository.saveAndFlush(admin);
                    });

            //Users
            List<User> users = generateUsers(userRole, encoder);

            users.forEach(user -> {
                if (this.userRepository.findByEmail(user.getEmail()).isEmpty())
                    this.userRepository.saveAndFlush(user);
            });

            //Categories
            Category mensClothing = Category.builder()
                    .category("men's clothing")
                    .build();

            Category womensClothing = Category.builder()
                    .category("women's clothing")
                    .build();

            List.of(mensClothing, womensClothing)
                    .forEach(category -> {
                        if (this.categoryRepository.findByCategory(category.getCategory()).isEmpty())
                            this.categoryRepository.saveAndFlush(category);
                    });

            //Ratings
            List<Product> mensClotingList = manClothingGenarator(users, mensClothing,
                    avRatingRepository, indRatingRepository);

            mensClotingList.forEach(prod -> {
                if (this.productRepository.findByTitle(prod.getTitle()).isEmpty())
                    this.productRepository.saveAndFlush(prod);
            });

            List<Product> womensClothingList = womanClothingGenerator(users, womensClothing,
                    avRatingRepository, indRatingRepository);

            womensClothingList.forEach(prod -> {
                if (this.productRepository.findByTitle(prod.getTitle()).isEmpty())
                    this.productRepository.saveAndFlush(prod);
            });

            //External API
            externalApi(users, restTemplate, mainConverter, categoryRepository,
                    avRatingRepository, indRatingRepository, productRepository);
        } catch (Exception ignored) {

        }
    }
}
