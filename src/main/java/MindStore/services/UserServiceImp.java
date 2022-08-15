package MindStore.services;

import MindStore.command.personDto.UserDto;
import MindStore.command.personDto.UserUpdateDto;
import MindStore.command.productDto.CategoryDto;
import MindStore.command.productDto.IndividualRatingDto;
import MindStore.command.productDto.ProductDto;
import MindStore.command.productDto.AverageRatingDto;
import MindStore.config.CheckAuth;
import MindStore.converters.MainConverterI;
import MindStore.enums.DirectionEnum;
import MindStore.enums.ProductFieldsEnum;
import MindStore.enums.RoleEnum;
import MindStore.exceptions.ConflictException;
import MindStore.exceptions.NotAllowedValueException;
import MindStore.persistence.models.Person.Role;
import MindStore.persistence.models.Person.User;
import MindStore.persistence.models.Product.IndividualRating;
import MindStore.persistence.models.Product.AverageRating;
import MindStore.persistence.repositories.Person.RoleRepository;
import MindStore.persistence.repositories.Person.UserRepository;
import MindStore.exceptions.NotFoundException;
import MindStore.persistence.models.Product.Category;
import MindStore.persistence.models.Product.Product;
import MindStore.persistence.repositories.Product.CategoryRepository;
import MindStore.persistence.repositories.Product.IndividualRatingRepository;
import MindStore.persistence.repositories.Product.ProductRepository;
import MindStore.persistence.repositories.Product.AverageRatingRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import static MindStore.helpers.FindBy.*;
import static MindStore.helpers.ValidateParams.validatePages;

@Service
@AllArgsConstructor
public class UserServiceImp implements UserServiceI {

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private AverageRatingRepository ratingRepository;
    private IndividualRatingRepository indRatingRepository;
    private MainConverterI mainConverter;
    private PasswordEncoder encoder;
    private final CheckAuth checkAuth;

    @Override
    public List<ProductDto> getAllProducts(String direction, String field, int page, int pageSize) {
        System.out.println("Getting products from DB");

        validatePages(page, pageSize);

        List<Product> products;
        switch (direction) {
            case DirectionEnum.ASC -> products = findProducts(Sort.Direction.ASC, field, page, pageSize);
            case DirectionEnum.DESC -> products = findProducts(Sort.Direction.DESC, field, page, pageSize);
            default -> throw new NotAllowedValueException("Direction not allowed");
        }

        return this.mainConverter.listConverter(products, ProductDto.class);
    }

    private List<Product> findProducts(Sort.Direction direction, String field, int page, int pageSize) {

        if (!ProductFieldsEnum.FIELDS.contains(field))
            throw new NotFoundException("Field not found");

        //1 pagina nao saltas nenhum a frente, na segunda pagina saltas x * nr de elementos que tens por pagina
        //para ir apartir dai
        if (field.equals(ProductFieldsEnum.RATING)) {
            int offset = (page - 1) * pageSize;

            if (direction.equals(Sort.Direction.ASC))
                return this.productRepository.findAllByRatingASC(pageSize, offset);
            else
                return this.productRepository.findAllByRatingDESC(pageSize, offset);
        }

        return this.productRepository.findAll(
                PageRequest.of(page - 1, pageSize)
                        .withSort(Sort.by(direction, field))
        ).stream().toList();
    }

    @Override
    public List<ProductDto> filterByPrice(String direction, int page, int pageSize, int minPrice, int maxPrice) {

        validatePages(page, pageSize);

        if (minPrice < 0 || maxPrice > 5000)
            throw new NotAllowedValueException("Price must be between 0 and 5000");

        List<Product> products;
        int offset = (page - 1) * pageSize;
        switch (direction) {
            case DirectionEnum.ASC ->
                    products = this.productRepository.findAllByPriceASC(pageSize, offset, minPrice, maxPrice);
            case DirectionEnum.DESC ->
                    products = this.productRepository.findAllByPriceDESC(pageSize, offset, minPrice, maxPrice);
            default -> throw new NotAllowedValueException("Direction not allowed");
        }

        return this.mainConverter.listConverter(products, ProductDto.class);
    }

    @Override
    public List<ProductDto> getProductsByTitle(String title, int page, int pageSize) {

        validatePages(page, pageSize);

        int offset = (page - 1) * pageSize;
        List<Product> productsList = this.productRepository.findAllByTitle(title, pageSize, offset);

        if (productsList.isEmpty())
            throw new NotFoundException("No products found with such name");

        return this.mainConverter.listConverter(productsList, ProductDto.class);
    }

    @Override
    public List<ProductDto> getProductByCategory(String category, int page, int pageSize) {

        validatePages(page, pageSize);

        Category categoryEntity = this.categoryRepository.findByCategory(category)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        int offset = (page - 1) * pageSize;
        List<Product> productList = this.productRepository
                .findAllByCategory(categoryEntity.getId(), pageSize, offset);

        if (productList.isEmpty() && page == 1)
            throw new NotFoundException("No products found with that category");

        return this.mainConverter.listConverter(productList, ProductDto.class);
    }


    @Override
    @Cacheable(value = "products", key = "#id")
    public ProductDto getProductById(Long id) {
        System.out.println("Fetching product from the DB");

        Product product = findProductById(id, this.productRepository);
        return this.mainConverter.converter(product, ProductDto.class);
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public UserDto getUserById(Long id) {
        System.out.println("Fetching user from the DB");

        this.checkAuth.checkUserId(id);

        User user = findUserById(id, this.userRepository);
        return this.mainConverter.converter(user, UserDto.class);
    }

    @Override
    @Cacheable(value = "categories", key = "#id")
    public CategoryDto getCategoryById(int id) {
        System.out.println("Fetching category from the DB");

        Category category = findCategoryById(id, this.categoryRepository);
        return this.mainConverter.converter(category, CategoryDto.class);
    }

    @Override
    @Cacheable(value = "shoppingcart", key = "#userId")
    public List<ProductDto> getShoppingCart(Long userId) {
        System.out.println("Fetching shopping cart from the DB");

        this.checkAuth.checkUserId(userId);

        List<Product> productList = findUserById(userId, this.userRepository).getShoppingCart();
        return this.mainConverter.listConverter(productList, ProductDto.class);
    }

    @Override
    @CacheEvict(value = {"products", "shoppingcart", "shoppingcartprice", "users"}, allEntries = true)
    public List<ProductDto> addProductToCart(Long userId, Long productId) {
        this.checkAuth.checkUserId(userId);

        User user = findUserById(userId, this.userRepository);
        Product product = findProductById(productId, this.productRepository);

        if (product.getStock() == 0)
            throw new NotFoundException("This product is unavailable");

        user.addProductToCart(product);
        this.userRepository.save(user);

        return this.mainConverter.listConverter(user.getShoppingCart(), ProductDto.class);
    }

    @Override
    @CacheEvict(value = {"products", "shoppingcart", "shoppingcartprice", "users"}, allEntries = true)
    public List<ProductDto> removeProductFromCart(Long userId, Long productId) {
        this.checkAuth.checkUserId(userId);

        User user = findUserById(userId, this.userRepository);
        Product product = findProductById(productId, this.productRepository);

        if (!user.getShoppingCart().contains(product))
            throw new NotFoundException("Product not found on the shopping cart");

        user.removeProductFromCart(product);
        this.userRepository.save(user);

        return this.mainConverter.listConverter(user.getShoppingCart(), ProductDto.class);
    }

    @Override
    @CacheEvict(value = {"products", "shoppingcart", "shoppingcartprice", "users"}, allEntries = true)
    public ResponseEntity<String> buyProducts(Long id, int payment) {
        this.checkAuth.checkUserId(id);

        User user = findUserById(id, this.userRepository);

        List<Product> cart = user.getShoppingCart();
        cart.forEach(product -> {
            if (product.getStock() == 0) {
                throw new NotFoundException("Product " + product.getTitle() + "'s stock is empty");
            }
        });

        double totalPrice = getCartTotalPrice(id);

        if (cart.size() == 0) throw new NotFoundException("Your shopping cart is empty");
        if (payment < totalPrice)
            return new ResponseEntity<>("You don't have enough money", HttpStatus.EXPECTATION_FAILED);

        user.getShoppingCart().forEach(prod -> {
            if (prod.getStock() == 0)
                throw new NotFoundException("You requiered more " + prod.getTitle() + " items than are available in the stock");
            prod.decreaseStock();
        });

        this.productRepository.saveAll(user.getShoppingCart());
        user.setShoppingCart(new ArrayList<>());
        this.userRepository.save(user);
        return new ResponseEntity<>("Payment accepted!", HttpStatus.OK);
    }

    @Override
    @Cacheable(value = "shoppingcartprice", key = "#userId")
    public Double getCartTotalPrice(Long userId) {
        System.out.println("Fetching shopping cart price from the DB");

        this.checkAuth.checkUserId(userId);

        User user = findUserById(userId, this.userRepository);

        return user.getShoppingCart().stream()
                .mapToDouble(Product::getPrice)
                .sum();
    }

    @Override
    @CacheEvict(value = {"users", "average_ratings", "individual_ratings", "products"}, allEntries = true)
    public UserDto updateUser(Long id, UserUpdateDto userUpdateDto) {
        this.checkAuth.checkUserId(id);

        User user = findUserById(id, this.userRepository);
        User updatedUser = this.mainConverter.updateConverter(userUpdateDto, user);

        if (userUpdateDto.getPassword() != null)
            updatedUser.setPassword(this.encoder.encode(userUpdateDto.getPassword()));

        this.userRepository.save(updatedUser);
        return this.mainConverter.converter(updatedUser, UserDto.class);
    }

    @Override
    @CacheEvict(value = {"users", "ratings", "products"}, allEntries = true)
    public void deleteUser(Long id) {
        this.checkAuth.checkUserId(id);
        User user = findUserById(id, this.userRepository);

        user.getIndividualRatings()
                .forEach(rating -> rating.setUserId(null));

        List<IndividualRating> userRatings = user.getIndividualRatings();
        this.indRatingRepository.saveAll(userRatings);
        this.userRepository.delete(user);
    }

    @Override
    @Cacheable(value = "ratings", key = "#userId")
    public List<IndividualRatingDto> getRatingList(Long userId) {
        System.out.println("Fetching ratings from the DB");

        List<IndividualRating> userRatings = findUserById(userId, this.userRepository)
                .getIndividualRatings();
        return this.mainConverter.listConverter(userRatings, IndividualRatingDto.class);
    }

    @Override
    @CacheEvict(value = {"products", "ratings", "shoppingcart", "ratings"}, allEntries = true)
    public AverageRatingDto rateProduct(Long userId, Long productId, int rating) {
        this.checkAuth.checkUserId(userId);

        if (rating < 1 || rating > 5)
            throw new ConflictException("Rating value should be between 0 and 5");

        User user = findUserById(userId, this.userRepository);
        Product product = findProductById(productId, this.productRepository);
        AverageRating averageRating = product.getRatingId();

        IndividualRating userRating = user.getIndividualRatings()
                .stream()
                .filter(rate -> rate.getAverageRatingId().equals(averageRating))
                .findFirst().orElse(null);

        int ratingCount;
        ratingCount = getRatingCount(rating, user, product, averageRating, userRating);

        averageRating.setCount(ratingCount);
        averageRating.setAverageRate();

        this.ratingRepository.save(averageRating);
        this.productRepository.save(product);
        this.userRepository.save(user);

        return this.mainConverter.converter(averageRating, AverageRatingDto.class);
    }

    private int getRatingCount(int rating, User user, Product product,
                               AverageRating averageRating, IndividualRating userRating) {
        int ratingCount;
        if (userRating == null) {
            ratingCount = product.getRatingId().getCount() + 1;

            IndividualRating newUserRating = IndividualRating.builder()
                    .rate(rating)
                    .productTitle(product.getTitle())
                    .userId(user)
                    .averageRatingId(averageRating)
                    .build();

            this.indRatingRepository.save(newUserRating);
        } else {
            ratingCount = product.getRatingId().getCount();

            userRating.setRate(rating);
            this.indRatingRepository.save(userRating);
        }
        return ratingCount;
    }

    @Override
    @CacheEvict(value = {"products", "ratings", "shoppingcart", "average_ratings", "individual_ratings"}, allEntries = true)
    public void deleteRate(Long userId, Long ratingId) {
        this.checkAuth.checkUserId(userId);

        User user = findUserById(userId, this.userRepository);
        IndividualRating userRating = findRatingById(ratingId, this.indRatingRepository);

        if (!user.getIndividualRatings().contains(userRating))
            throw new NotFoundException("Rating not found on user's rating list");

        this.indRatingRepository.delete(userRating);

        AverageRating averageRating = userRating.getAverageRatingId();
        averageRating.decreaseCount();
        averageRating.setRate(
                averageRating.getIndividualRatings()
                        .stream()
                        .mapToDouble(IndividualRating::getRate)
                        .average().orElse(0)
        );

        this.ratingRepository.save(averageRating);
    }

    @Override
    @CacheEvict(value = {"users"}, allEntries = true)
    public UserDto signUp(UserDto userDto) {
        this.userRepository.findByEmail(userDto.getEmail())
                .ifPresent((student) -> {
                    throw new ConflictException("Email already beeing used");
                });

        User userToSave = this.mainConverter.converter(userDto, User.class);
        Role role = findRoleById(RoleEnum.USER, this.roleRepository);

        userToSave.setRoleId(role);
        userToSave.setPassword(this.encoder.encode(userToSave.getPassword()));
        this.userRepository.save(userToSave);

        return this.mainConverter.converter(userToSave, UserDto.class);
    }
}
