package MindStore.controllers;

import MindStore.command.personDto.AdminDto;
import MindStore.command.personDto.AdminUpdateDto;
import MindStore.command.personDto.UserDto;
import MindStore.command.personDto.UserUpdateDto;
import MindStore.command.productDto.ProductDto;
import MindStore.command.productDto.ProductUpdateDto;
import MindStore.services.AdminServiceI;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("api/v1/admins")
@AllArgsConstructor

public class AdminController {
    private AdminServiceI adminService;

    @GetMapping("products")
    public List<ProductDto> getAllProducts(@RequestParam(value = "direction") String direction,
                                           @RequestParam(value = "field") String field,
                                           @RequestParam(value = "page") int page,
                                           @RequestParam(value = "pagesize") int pageSize) {
        return this.adminService.getAllProducts(direction, field, page, pageSize);
    }
    //Done

    @GetMapping("products/price")
    public List<ProductDto> getAllProductsByPrice(@RequestParam(value = "direction") String direction,
                                                  @RequestParam(value = "page") int page,
                                                  @RequestParam(value = "pagesize") int pageSize,
                                                  @RequestParam(value = "min") int minPrice,
                                                  @RequestParam(value = "max") int maxPrice) {
        return this.adminService.getAllProductsByPrice(direction, page, pageSize, minPrice, maxPrice);
    }
    //Done

    @GetMapping("products/{id}")
    public ProductDto getProductById(@PathVariable("id") Long id) {
        return this.adminService.getProductById(id);
    }
    //Done

    @GetMapping("products/title/{title}")
    public List<ProductDto> getProductsByName(@PathVariable("title") String title) {
        return this.adminService.getProductsByName(title);
    }
    //Done

    @GetMapping("users")
    public List<UserDto> getAllUsers(@RequestParam(value = "direction") String direction,
                                     @RequestParam(value = "field") String field,
                                     @RequestParam(value = "page") int page,
                                     @RequestParam(value = "pagesize") int pageSize) {
        return this.adminService.getAllUsers(direction, field, page, pageSize);
    }
    //Done

    @GetMapping("users/{id}")
    public UserDto getUserById(@PathVariable("id") Long id) {
        return this.adminService.getUserById(id);
    }
    //Done

    @GetMapping("users/name/{name}")
    public List<UserDto> getUsersByName(@PathVariable("name") String name) {
        return this.adminService.getUsersByName(name);
    }
    //Done

    @PostMapping
    public AdminDto addAdmin(@Valid @RequestBody AdminDto adminDto) {
        return this.adminService.addAdmin(adminDto);
    }
    //Done

    @PostMapping("products")
    public ProductDto addProduct(@Valid @RequestBody ProductDto productDto) {
        return this.adminService.addProduct(productDto);
    }
    //Done

    @PostMapping("users")
    public UserDto addUser(@Valid @RequestBody UserDto userDto) {
        return this.adminService.addUser(userDto);
    }
    //Done

    @PatchMapping("{id}")
    public AdminDto updateAdmin(@PathVariable("id") Long id, @Valid @RequestBody AdminUpdateDto adminUpdateDto) {
        return this.adminService.updateAdmin(id, adminUpdateDto);
    }
    //Done

    @PatchMapping("products/{id}")
    public ProductDto updateProduct(@PathVariable("id") Long id, @Valid @RequestBody ProductUpdateDto productUpdateDto) {
        return this.adminService.updateProduct(id, productUpdateDto);
    }
    //Done

    @PatchMapping("users/{id}")
    public UserDto updateUser(@PathVariable("id") Long id, @Valid @RequestBody UserUpdateDto userUpdateDto) {
        return this.adminService.updateUser(id, userUpdateDto);
    }
    //Done

    @DeleteMapping("products/{id}")
    public void deleteProduct(@PathVariable("id") Long id) {
        this.adminService.deleteProduct(id);
    }
    //Done

    @DeleteMapping("products/title/{title}")
    public void deleteProduct(@PathVariable("title") String title) {
        this.adminService.deleteProductByTitle(title);
    }
    //Done
}
