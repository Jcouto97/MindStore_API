package MindStore.services;

import MindStore.command.*;
import MindStore.config.CheckAuth;
import MindStore.converters.MainConverterI;
import MindStore.enums.DirectionEnum;
import MindStore.enums.ProductFieldsEnum;
import MindStore.enums.RoleEnum;
import MindStore.exceptions.ConflictException;
import MindStore.exceptions.NotAllowedValueException;
import MindStore.exceptions.NotFoundException;
import MindStore.persistence.models.Person.Admin;
import MindStore.persistence.models.Person.Role;
import MindStore.persistence.models.Product.Category;
import MindStore.persistence.models.Product.Product;
import MindStore.persistence.models.Product.Rating;
import MindStore.persistence.models.Person.User;
import MindStore.persistence.repositories.Person.AdminRepository;
import MindStore.persistence.repositories.Person.PersonRepository;
import MindStore.persistence.repositories.Person.RoleRepository;
import MindStore.persistence.repositories.Product.CategoryRepository;
import MindStore.persistence.repositories.Product.ProductRepository;
import MindStore.persistence.repositories.Person.UserRepository;
import MindStore.persistence.repositories.Product.RatingRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static MindStore.helpers.ValidateParams.validatePages;

@Service
@AllArgsConstructor
public class AdminService implements AdminServiceI {
    private PersonRepository personRepository;
    private AdminRepository adminRepository;
    private UserRepository userRepository;
    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private RatingRepository ratingRepository;
    private RoleRepository roleRepository;
    private CacheManager cacheManager;
    private MainConverterI converter;
    private PasswordEncoder encoder;
    private final CheckAuth checkAuth;

    @Cacheable("products")
    @Override
    public List<ProductDto> getAllProducts(String direction, String field, int page, int pageSize) {
        validatePages(page, pageSize);

        ProductFieldsEnum.FIELDS.stream()
                .filter(elm -> elm.equalsIgnoreCase(field))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Field not found"));

        List<Product> products;
        switch (direction) {
            case DirectionEnum.ASC -> products = findProducts(Sort.Direction.ASC, field, page, pageSize)
                    .stream().toList();
            case DirectionEnum.DESC -> products = findProducts(Sort.Direction.DESC, field, page, pageSize)
                    .stream().toList();
            default -> throw new NotAllowedValueException("Direction not allowed");
        }

        return this.converter.listConverter(products, ProductDto.class);
    }

    //CACHE? demasiado especifico
    @Override
    public List<ProductDto> getAllProductsByPrice(String direction, int page, int pageSize, int minPrice, int maxPrice) {
        validatePages(page, pageSize);

        if (minPrice < 0 || maxPrice > 1000)
            throw new NotAllowedValueException("Price must be between 0 and 1000");

        List<Product> products;
        switch (direction) {
            case DirectionEnum.ASC -> products = findProducts(Sort.Direction.ASC, "price", page, pageSize)
                    .stream().filter(prod -> prod.getPrice() >= minPrice && prod.getPrice() <= maxPrice)
                    .toList();
            case DirectionEnum.DESC -> products = findProducts(Sort.Direction.DESC, "price", page, pageSize)
                    .stream().filter(prod -> prod.getPrice() >= minPrice && prod.getPrice() <= maxPrice)
                    .toList();
            default -> throw new NotAllowedValueException("Direction not allowed");
        }

        return this.converter.listConverter(products, ProductDto.class);
    }

    //Enums do field
    private Page<Product> findProducts(Sort.Direction direction, String field, int page, int pageSize) {
        return this.productRepository.findAll(
                PageRequest.of(page - 1, pageSize)
                        .withSort(Sort.by(direction, field))
        );
    }

    @Cacheable(key = "#id", value = "products")
    @Override
    public ProductDto getProductById(Long id) {
        Product product = this.productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return this.converter.converter(product, ProductDto.class);
    }

    @Cacheable(key = "#title", value = "products")
    @Override
    public List<ProductDto> getProductsByName(String title) {
        List<Product> products = this.productRepository.findByTitleLike(title);
        if (products.isEmpty()) throw new NotFoundException("Product not found");
        return this.converter.listConverter(products, ProductDto.class);
    }

    @Cacheable("users")
    @Override
    public List<UserDto> getAllUsers(String direction, String field, int page, int pageSize) {
        validatePages(page, pageSize);

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

    @Cacheable(key = "#id", value = "users")
    @Override
    public UserDto getUserById(Long id) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return this.converter.converter(user, UserDto.class);
    }

    @Cacheable(key = "#name", value = "users")
    @Override
    public List<UserDto> getUsersByName(String name) {
        List<User> user = this.userRepository.findByName(name);
        if (user.isEmpty()) throw new NotFoundException("User not found");
        return this.converter.listConverter(user, UserDto.class);
    }

    @Override
    public AdminDto addAdmin(AdminDto adminDto) {
        this.adminRepository.findByEmail(adminDto.getEmail())
                .ifPresent(x -> {
                    throw new ConflictException("Email is already being used");
                });

        Role role = this.roleRepository.findById(RoleEnum.ADMIN).
                orElseThrow(() -> new NotFoundException("Role not found"));

        Admin admin = this.converter.converter(adminDto, Admin.class);
        admin.setRoleId(role);

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

        Rating rating = Rating.builder()
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

        Role role = this.roleRepository.findById(RoleEnum.USER).
                orElseThrow(() -> new NotFoundException("Role not found"));

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

        Admin admin = this.adminRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Admin not found"));

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
        Product product = this.productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        this.productRepository.findByTitle(productUpdateDto.getTitle())
                .ifPresent(x -> {
                    throw new ConflictException("Title already exists");
                });

        product = this.converter.updateConverter(productUpdateDto, product);

        clearProductCache();

        return this.converter.converter(
                this.productRepository.save(product), ProductDto.class
        );
    }

    @Override
    public UserDto updateUser(Long id, UserUpdateDto userUpdateDto) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

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
    public void deleteProduct(String title) {
        Product product = this.productRepository.findByTitle(title)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        clearProductCache();

        this.productRepository.delete(product);
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
