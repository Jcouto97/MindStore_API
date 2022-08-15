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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
    private CacheManager cacheManager;


    @Cacheable("products")
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

    @Cacheable(key = "#field", value = "products")
    private List<Product> findProducts(Sort.Direction direction, String field, int page, int pageSize) {
        if (!ProductFieldsEnum.FIELDS.contains(field))
            throw new NotFoundException("Field not found");

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
    @Cacheable(key = "#title", value = "products")
    public List<ProductDto> getProductsByTitle(String title, int page, int pageSize) {
        validatePages(page, pageSize);

        int offset = (page - 1) * pageSize;
        List<Product> productsList = this.productRepository.findAllByTitle(title, pageSize, offset);

        if (productsList.isEmpty())
            throw new NotFoundException("No products found with such name");

        return this.mainConverter.listConverter(productsList, ProductDto.class);
    }

    @Override
    @Cacheable(key = "#category", value = "products")
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
    @Cacheable(key = "#id", value = "products")
    public ProductDto getProductById(Long id) {
        Product product = findProductById(id, this.productRepository);
        return this.mainConverter.converter(product, ProductDto.class);
    }

    @Override
    @Cacheable(key = "#id", value = "categories")
    public CategoryDto getCategoryById(int id) {
        Category category = findCategoryById(id, this.categoryRepository);
        return this.mainConverter.converter(category, CategoryDto.class);
    }

    @Override
    @Cacheable(key = "#userId", value = "shoppingcart")
    public List<ProductDto> getShoppingCart(Long userId) {
        this.checkAuth.checkUserId(userId);

        List<Product> productList = findUserById(userId, this.userRepository).getShoppingCart();
        return this.mainConverter.listConverter(productList, ProductDto.class);
    }

    @Override
    public List<ProductDto> addProductToCart(Long userId, Long productId) {
        this.checkAuth.checkUserId(userId);

        User user = findUserById(userId, this.userRepository);
        Product product = findProductById(productId, this.productRepository);

        if (product.getStock() == 0)
            throw new NotFoundException("This product is unavailable");

        clearShoppingCartCache();
        clearShoppingCartPriceCache();

        user.addProductToCart(product);
        this.userRepository.save(user);

        return this.mainConverter.listConverter(user.getShoppingCart(), ProductDto.class);
    }

    @Override
    public List<ProductDto> removeProductFromCart(Long userId, Long productId) {
        this.checkAuth.checkUserId(userId);

        User user = findUserById(userId, this.userRepository);
        Product product = findProductById(productId, this.productRepository);

        if (!user.getShoppingCart().contains(product))
            throw new NotFoundException("Product not found on the shopping cart");

        clearShoppingCartCache();
        clearShoppingCartPriceCache();

        user.removeProductFromCart(product);
        this.userRepository.save(user);

        return this.mainConverter.listConverter(user.getShoppingCart(), ProductDto.class);
    }

    @Override
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

        //limpar as caches
        clearShoppingCartCache();
        clearShoppingCartPriceCache();
        clearProductCache();

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
    @Cacheable(key = "#userId", value = "shoppingcartprice")
    public Double getCartTotalPrice(Long userId) {
        this.checkAuth.checkUserId(userId);

        User user = findUserById(userId, this.userRepository);

        return user.getShoppingCart().stream()
                .mapToDouble(Product::getPrice)
                .sum();
    }

    @Override
    public UserDto updateUser(Long id, UserUpdateDto userUpdateDto) {
        this.checkAuth.checkUserId(id);

        User user = findUserById(id, this.userRepository);
        User updatedUser = this.mainConverter.updateConverter(userUpdateDto, user);

        if (userUpdateDto.getPassword() != null)
            updatedUser.setPassword(this.encoder.encode(userUpdateDto.getPassword()));

        clearUserCache();

        this.userRepository.save(updatedUser);
        return this.mainConverter.converter(updatedUser, UserDto.class);
    }

    @Override
    public void deleteUser(Long id) {
        this.checkAuth.checkUserId(id);
        User user = findUserById(id, this.userRepository);

        user.getIndividualRatings()
                .forEach(rating -> rating.setUserId(null));

        List<IndividualRating> userRatings = user.getIndividualRatings();

        clearUserCache();

        this.indRatingRepository.saveAll(userRatings);
        this.userRepository.delete(user);
    }

    @Override
    @Cacheable(key = "#userId", value = "ratings")
    public List<IndividualRatingDto> getRatingList(Long userId) {
        List<IndividualRating> userRatings = findUserById(userId, this.userRepository)
                .getIndividualRatings();
        return this.mainConverter.listConverter(userRatings, IndividualRatingDto.class);
    }

    @Override
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

        clearRatingCache();

        this.ratingRepository.save(averageRating);
        this.productRepository.save(product);
        this.userRepository.save(user);

        return this.mainConverter.converter(averageRating, AverageRatingDto.class);
    }

    @Cacheable(key = "#rating", value = "ratings")
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

        clearRatingCache();

        this.ratingRepository.save(averageRating);
    }

    @Override
    public UserDto signUp(UserDto userDto) {
        this.userRepository.findByEmail(userDto.getEmail())
                .ifPresent((student) -> {
                    throw new ConflictException("Email already beeing used");
                });

        User userToSave = this.mainConverter.converter(userDto, User.class);
        Role role = findRoleById(RoleEnum.USER, this.roleRepository);

        userToSave.setRoleId(role);
        userToSave.setPassword(this.encoder.encode(userToSave.getPassword()));

        clearUserCache();

        this.userRepository.save(userToSave);

        return this.mainConverter.converter(userToSave, UserDto.class);
    }

    private void clearUserCache() {
        Cache userCache = this.cacheManager.getCache("users");
        if(userCache!=null)userCache.clear();
    }

    private void clearShoppingCartCache() {
        Cache shoppingCartCache = this.cacheManager.getCache("shoppingcart");
        if(shoppingCartCache!=null)shoppingCartCache.clear();
    }

    private void clearShoppingCartPriceCache() {
        Cache shoppingCartPriceCache = this.cacheManager.getCache("shoppingcartprice");
        if(shoppingCartPriceCache!=null)shoppingCartPriceCache.clear();
    }

    private void clearProductCache() {
        Cache productCache = this.cacheManager.getCache("products");
        if(productCache!=null)productCache.clear();
    }

    private void clearRatingCache() {
        Cache ratingCache = this.cacheManager.getCache("ratings");
        if(ratingCache!=null)ratingCache.clear();
    }
}
