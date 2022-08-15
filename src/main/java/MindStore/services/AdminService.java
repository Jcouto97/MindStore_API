package MindStore.services;

import MindStore.command.personDto.AdminDto;
import MindStore.command.personDto.AdminUpdateDto;
import MindStore.command.personDto.UserDto;
import MindStore.command.personDto.UserUpdateDto;
import MindStore.command.productDto.ProductDto;
import MindStore.command.productDto.ProductUpdateDto;
import MindStore.config.CheckAuth;
import MindStore.converters.MainConverterI;
import MindStore.enums.DirectionEnum;
import MindStore.enums.ProductFieldsEnum;
import MindStore.enums.RoleEnum;
import MindStore.enums.UserFieldsEnum;
import MindStore.exceptions.ConflictException;
import MindStore.exceptions.NotAllowedValueException;
import MindStore.exceptions.NotFoundException;
import MindStore.persistence.models.Person.Admin;
import MindStore.persistence.models.Person.Role;
import MindStore.persistence.models.Product.Category;
import MindStore.persistence.models.Product.Product;
import MindStore.persistence.models.Product.AverageRating;
import MindStore.persistence.models.Person.User;
import MindStore.persistence.repositories.Person.AdminRepository;
import MindStore.persistence.repositories.Person.PersonRepository;
import MindStore.persistence.repositories.Person.RoleRepository;
import MindStore.persistence.repositories.Product.CategoryRepository;
import MindStore.persistence.repositories.Product.ProductRepository;
import MindStore.persistence.repositories.Person.UserRepository;
import MindStore.persistence.repositories.Product.AverageRatingRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static MindStore.helpers.FindBy.*;
import static MindStore.helpers.ValidateParams.validatePages;

@Service
@AllArgsConstructor
public class AdminService implements AdminServiceI {
    private PersonRepository personRepository;
    private AdminRepository adminRepository;
    private UserRepository userRepository;
    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private AverageRatingRepository ratingRepository;
    private RoleRepository roleRepository;
    private MainConverterI converter;
    private PasswordEncoder encoder;
    private final CheckAuth checkAuth;
    private CacheManager cacheManager;

    @Override
    @Cacheable("products")
    public List<ProductDto> getAllProducts(String direction, String field, int page, int pageSize) {
        System.out.println("Fetching from Database");
        validatePages(page, pageSize);

        List<Product> products;
        switch (direction) {
            case DirectionEnum.ASC -> products = findProducts(Sort.Direction.ASC, field, page, pageSize);
            case DirectionEnum.DESC -> products = findProducts(Sort.Direction.DESC, field, page, pageSize);
            default -> throw new NotAllowedValueException("Direction not allowed");
        }

        return this.converter.listConverter(products, ProductDto.class);
    }

    @Override
    @Cacheable(key = "#page", value = "products")
    public List<ProductDto> getAllProductsByPrice(String direction, int page, int pageSize, int minPrice, int maxPrice) {
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

        return this.converter.listConverter(products, ProductDto.class);
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
    @Cacheable(key = "#id", value = "products")
    public ProductDto getProductById(Long id) {
        Product product = findProductById(id, this.productRepository);
        return this.converter.converter(product, ProductDto.class);
    }

    @Override
    @Cacheable(key = "#title", value = "products")
    public List<ProductDto> getProductsByName(String title) {
        List<Product> products = this.productRepository.findByTitleLike(title);
        if (products.isEmpty()) throw new NotFoundException("Product not found");
        return this.converter.listConverter(products, ProductDto.class);
    }


    @Override
    @Cacheable("users")
    public List<UserDto> getAllUsers(String direction, String field, int page, int pageSize) {
        validatePages(page, pageSize);

        if (!UserFieldsEnum.FIELDS.contains(field))
            throw new NotFoundException("Field not found");

        List<User> users;
        switch (direction) {
            case DirectionEnum.ASC -> users = findUsers(Sort.Direction.ASC, field, page, pageSize);
            case DirectionEnum.DESC -> users = findUsers(Sort.Direction.DESC, field, page, pageSize);
            default -> throw new NotAllowedValueException("Direction not allowed");
        }

        return this.converter.listConverter(users, UserDto.class);
    }

    private List<User> findUsers(Sort.Direction direction, String field, int page, int pageSize) {
        return this.userRepository.findAll(
                PageRequest.of(page - 1, pageSize)
                        .withSort(Sort.by(direction, field))
        ).stream().toList();
    }

    @Override
    @Cacheable(key = "#id", value = "users")
    public UserDto getUserById(Long id) {
        User user = findUserById(id, this.userRepository);
        return this.converter.converter(user, UserDto.class);
    }

    @Override
    @Cacheable(key = "#name", value = "users")
    public List<UserDto> getUsersByName(String name) {
        List<User> user = this.userRepository.findAllByName(name);
        if (user.isEmpty()) throw new NotFoundException("User not found");
        return this.converter.listConverter(user, UserDto.class);
    }

    @Override
    public AdminDto addAdmin(AdminDto adminDto) {
        this.adminRepository.findByEmail(adminDto.getEmail())
                .ifPresent(x -> {
                    throw new ConflictException("Email is already being used");
                });

        Role role = findRoleById(RoleEnum.ADMIN, this.roleRepository);

        Admin admin = this.converter.converter(adminDto, Admin.class);
        admin.setRoleId(role);
        admin.setPassword(this.encoder.encode(adminDto.getPassword()));

        clearAdminCache();

        return this.converter.converter(
                this.adminRepository.save(admin), AdminDto.class
        );
    }

    @Override
    public ProductDto addProduct(ProductDto productDto) {
        this.productRepository
                .findByTitle(productDto.getTitle())
                .ifPresent((x) -> {
                    throw new ConflictException("Product already exists");
                });

        Category category = this.categoryRepository
                .findByCategory(productDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        AverageRating rating = AverageRating.builder()
                .rate(0)
                .count(0)
                .build();

        Product product = this.converter.converter(productDto, Product.class);

        clearProductCache();

        this.ratingRepository.save(rating);
        product.setCategory(category);
        product.setRatingId(rating);

        return this.converter.converter(
                this.productRepository.save(product), ProductDto.class
        );
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        this.userRepository.findByEmail(userDto.getEmail())
                .ifPresent(x -> {
                    throw new ConflictException("Email is already being used");
                });

        Role role = findRoleById(RoleEnum.USER, this.roleRepository);

        User user = this.converter.converter(userDto, User.class);
        user.setRoleId(role);
        user.setPassword(this.encoder.encode(userDto.getPassword()));

        clearUserCache();

        return this.converter.converter(
                this.userRepository.save(user), UserDto.class
        );
    }

    @Override
    public AdminDto updateAdmin(Long id, AdminUpdateDto adminUpdateDto) {
        this.checkAuth.checkUserId(id);

        Admin admin = findAdminById(id, this.adminRepository);

        this.personRepository.findByEmail(adminUpdateDto.getEmail())
                .ifPresent(x -> {
                    throw new ConflictException("Email is already being used");
                });

        admin = this.converter.updateConverter(adminUpdateDto, admin);

        if (adminUpdateDto.getPassword() != null)
            admin.setPassword(this.encoder.encode(adminUpdateDto.getPassword()));

        clearAdminCache();

        return this.converter.converter(
                this.adminRepository.save(admin), AdminDto.class
        );
    }

    @Override
    public ProductDto updateProduct(Long id, ProductUpdateDto productUpdateDto) {
        Product product = findProductById(id, this.productRepository);
        String title = product.getTitle();

        Category category = this.categoryRepository
                .findByCategory(productUpdateDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        this.productRepository.findByTitle(productUpdateDto.getTitle())
                .ifPresent(prod -> {
                    if (!title.equals(prod.getTitle()))
                        throw new ConflictException("Title already exists");
                });

        productUpdateDto.setCategory(null);
        product = this.converter.updateConverter(productUpdateDto, product);
        product.setCategory(category);

        clearProductCache();

        return this.converter.converter(
                this.productRepository.save(product), ProductDto.class
        );
    }

    @Override
    public UserDto updateUser(Long id, UserUpdateDto userUpdateDto) {
        User user = findUserById(id, this.userRepository);

        this.personRepository.findByEmail(userUpdateDto.getEmail())
                .ifPresent(x -> {
                    throw new ConflictException("Email is already being used");
                });

        user = this.converter.updateConverter(userUpdateDto, user);

        if (userUpdateDto.getPassword() != null)
            user.setPassword(this.encoder.encode(userUpdateDto.getPassword()));

        clearUserCache();

        return this.converter.converter(
                this.userRepository.save(user), UserDto.class
        );
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = findProductById(id, this.productRepository);
        product.getUsers()
                .forEach(user -> user.removeProductFromCart(product));

        clearProductCache();

        this.productRepository.delete(product);
        this.ratingRepository.delete(product.getRatingId());
    }

    @Override
    public void deleteProductByTitle(String title) {
        Product product = this.productRepository.findByTitle(title)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        clearProductCache();

        this.productRepository.delete(product);
        this.ratingRepository.delete(product.getRatingId());
    }

    private void clearUserCache() {
        Cache userCache = this.cacheManager.getCache("users");
        if(userCache!=null)userCache.clear();
    }

    private void clearAdminCache() {
        Cache adminCache = this.cacheManager.getCache("admin");
        if(adminCache!=null)adminCache.clear();
    }
    //admin e product

    private void clearProductCache() {
        Cache productCache = this.cacheManager.getCache("products");
        if(productCache!=null)productCache.clear();
    }
}
