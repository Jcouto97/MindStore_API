package MindStore.controllers;

import MindStore.command.personDto.UserDto;
import MindStore.command.personDto.UserUpdateDto;
import MindStore.command.productDto.CategoryDto;
import MindStore.command.productDto.IndividualRatingDto;
import MindStore.command.productDto.ProductDto;
import MindStore.command.productDto.AverageRatingDto;
import MindStore.services.UserServiceI;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/users")

public class UserController {
    private UserServiceI userServiceI;

    @GetMapping("/products")
    public List<ProductDto> getAllProducts(@RequestParam(value = "direction") String direction,
                                           @RequestParam(value = "field") String field,
                                           @RequestParam(value = "page") int page,
                                           @RequestParam(value = "pagesize") int pageSize) {
        return this.userServiceI.getAllProducts(direction, field, page, pageSize);
    }
    //Done

    @GetMapping("/products/byname")
    public List<ProductDto> getProductByTitle(@RequestParam(value = "title") String title,
                                              @RequestParam(value = "page") int page,
                                              @RequestParam(value = "pagesize") int pageSize) {
        return this.userServiceI.getProductsByTitle(title, page, pageSize);
    }
    //Done

    @GetMapping("/products/bycategory")
    public List<ProductDto> getProductByCategory(@RequestParam("category") String category,
                                                 @RequestParam(value = "page") int page,
                                                 @RequestParam(value = "pagesize") int pageSize) {
        return this.userServiceI.getProductByCategory(category, page, pageSize);
    }
    //Done

    @GetMapping("/products/{id}")
    public ProductDto getProductById(@PathVariable("id") Long id) {
        return this.userServiceI.getProductById(id);
    }
    //Done

    @GetMapping("/categories/{id}")
    public CategoryDto getCategoryById(@PathVariable("id") int id) {
        return this.userServiceI.getCategoryById(id);
    }
    //Done

    @GetMapping("/shoppingcart/{userid}")
    public List<ProductDto> getShoppingCart(@PathVariable("userid") Long userId) {
        return this.userServiceI.getShoppingCart(userId);
    }
    //Done

    @GetMapping("/shoppingcart/price/{userid}")
    public String getCartTotalPrice(@PathVariable("userid") Long userId) {
        return this.userServiceI.getCartTotalPrice(userId) + "€";
    }
    //Done

    @PostMapping
    public UserDto signUp(@Valid @RequestBody UserDto userDto) {
        return this.userServiceI.signUp(userDto);
    }
    //Done

    @DeleteMapping("/delete/{id}")
    public UserDto deleteUser(@PathVariable("id") Long id) {
        return this.userServiceI.deleteUser(id);
    }


    @PatchMapping("/{id}")
    public UserDto updateUser(@PathVariable("id") Long id, @Valid @RequestBody UserUpdateDto userUpdateDto) {
        return this.userServiceI.updateUser(id, userUpdateDto);
    }
    //Done

    @PatchMapping("addtocart")
    public List<ProductDto> addProductToCart(@RequestParam(value = "userid") Long userId,
                                             @RequestParam(value = "productid") Long productId) {
        return this.userServiceI.addProductToCart(userId, productId);
    }
    //Done

    @PatchMapping("removefromcart")
    public List<ProductDto> removeProductFromCart(@RequestParam(value = "userid") Long userId,
                                                  @RequestParam(value = "productid") Long productId) {
        return this.userServiceI.removeProductFromCart(userId, productId);
    }
    //Done

    @PostMapping("buy/{id}")
    public ResponseEntity<String> buyProducts(@PathVariable("id") Long id,
                                              @Valid @RequestBody int payment) {
        return this.userServiceI.buyProducts(id, payment);
    }
    //Done

    @GetMapping("/rating/{userid}")
    public List<IndividualRatingDto> getRatingList(@PathVariable("userid") Long userId) {
        return this.userServiceI.getRatingList(userId);
    }

    @PostMapping("/rating")
    public AverageRatingDto rateProduct(@RequestParam(value = "userid") Long userId,
                                        @RequestParam(value = "productid") Long productId,
                                        @RequestParam(value = "rating") int rating) {
        return this.userServiceI.rateProduct(userId, productId, rating);
    }
    //Done

    @DeleteMapping("/rating")
    public void deleteRate(@RequestParam(value = "userid") Long userId,
                           @RequestParam(value = "ratingid") Long ratingId) {
        this.userServiceI.deleteRate(userId, ratingId);
    }
    //Done

    @GetMapping("/price")
    public List<ProductDto> filterByPrice(@RequestParam(value = "direction") String direction,
                                          @RequestParam(value = "page") int page,
                                          @RequestParam(value = "pagesize") int pageSize,
                                          @RequestParam(value = "min") int minPrice,
                                          @RequestParam(value = "max") int maxPrice) {
        return this.userServiceI.filterByPrice(direction, page, pageSize, minPrice, maxPrice);
    }
    //Done
}
