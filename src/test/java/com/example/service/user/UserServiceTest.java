package com.example.service.user;

import MindStore.command.personDto.UserDto;
import MindStore.command.productDto.ProductDto;
import MindStore.config.CheckAuth;
import MindStore.converters.MainConverter;
import MindStore.persistence.models.Product.Product;
import MindStore.persistence.repositories.Person.RoleRepository;
import MindStore.persistence.repositories.Person.UserRepository;
import MindStore.persistence.repositories.Product.AverageRatingRepository;
import MindStore.persistence.repositories.Product.CategoryRepository;
import MindStore.persistence.repositories.Product.IndividualRatingRepository;
import MindStore.persistence.repositories.Product.ProductRepository;
import MindStore.services.UserServiceI;
import MindStore.services.UserServiceImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.example.persistence.model.product.ProductPojo.PRODUCT_DTO_EXAMPLE;
import static com.example.persistence.model.product.ProductPojo.PRODUCT_EXAMPLE;
import static com.example.persistence.model.user.UserPojo.USER_DTO_EXAMPLE;
import static com.example.persistence.model.user.UserPojo.USER_EXAMPLE;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    UserServiceI userServiceI;

    @Mock
    UserRepository userRepository;
    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    RoleRepository roleRepository;
    AverageRatingRepository averageRatingRepository;
    IndividualRatingRepository individualRatingRepository;

    @Mock
    PasswordEncoder encoder;

    @Mock
    CheckAuth checkAuth;

    @BeforeEach
    public void setup() {
        this.userServiceI = new UserServiceImp(
                productRepository,
                categoryRepository,
                userRepository,
                roleRepository,
                averageRatingRepository,
                individualRatingRepository,
                new MainConverter(new ModelMapper()),
                encoder,
                checkAuth
        );
    }

    @Test
    void test_getUserById() {
        when(productRepository.findById(any()))
                .thenReturn(Optional.of(PRODUCT_EXAMPLE));

        ProductDto result = userServiceI.getProductById(any());

        assertEquals(PRODUCT_DTO_EXAMPLE, result);
    }
}
